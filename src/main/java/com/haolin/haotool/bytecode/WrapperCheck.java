
package com.haolin.haotool.bytecode;

import cn.hutool.core.util.StrUtil;
import com.haolin.dubbo.common.exce.NoSuchPropertyException;
import com.haolin.dubbo.common.util.ClassUtils;
import com.haolin.dubbo.common.util.ReflectUtils;
import javassist.ClassPool;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Wrapper.
 */
public abstract class WrapperCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(WrapperCheck.class);

    /**
     * Wrapper 对象缓存
     * key ：需要Wrapper 类。
     * value ：Proxy 对象
     */
    private static final Map<Class<?>, WrapperCheck> WRAPPER_MAP = new ConcurrentHashMap<>(); //class wrapper map
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String[] OBJECT_METHODS = new String[]{"getClass", "hashCode", "toString", "equals"};
    private static final WrapperCheck OBJECT_WRAPPER = new WrapperCheck() {
        @Override
        public String[] getMethodNames() {
            return OBJECT_METHODS;
        }

        @Override
        public String[] getDeclaredMethodNames() {
            return OBJECT_METHODS;
        }

        @Override
        public String[] getPropertyNames() {
            return EMPTY_STRING_ARRAY;
        }

        @Override
        public Class<?> getPropertyType(String pn) {
            return null;
        }

        @Override
        public Object getPropertyValue(Object instance, String pn) throws NoSuchPropertyException {
            throw new NoSuchPropertyException("Property [" + pn + "] not found.");
        }

        @Override
        public void setPropertyValue(Object instance, String pn, Object pv) throws NoSuchPropertyException {
            throw new NoSuchPropertyException("Property [" + pn + "] not found.");
        }

        @Override
        public boolean hasProperty(String name) {
            return false;
        }

        @Override
        public Object invokeMethod(Object instance, String mn, Class<?>[] types, Object[] args) throws NoSuchMethodException {
            if ("getClass".equals(mn)) {
                return instance.getClass();
            }
            if ("hashCode".equals(mn)) {
                return instance.hashCode();
            }
            if ("toString".equals(mn)) {
                return instance.toString();
            }
            if ("equals".equals(mn)) {
                if (args.length == 1) {
                    return instance.equals(args[0]);
                }
                throw new IllegalArgumentException("Invoke method [" + mn + "] argument number error.");
            }
            throw new NoSuchMethodException("Method [" + mn + "] not found.");
        }


        @Override
        public void clearParam(Object instance) throws NoSuchMethodException, InvocationTargetException {
        }
    };

    /**
     * Wrapper Class 计数，用于生成 Wrapper 类名自增。
     */
    private static AtomicLong WRAPPER_CLASS_COUNTER = new AtomicLong(0);

    /**
     * get wrapper. 根据指定类，获得 Wrapper 对象
     *
     * @param c Class instance. 指定类
     * @return Wrapper instance(not null).   Wrapper 对象
     */
    public static WrapperCheck getWrapper(Class<?> c) {
        // 判断是否继承 ClassGenerator.DC.class ，如果是，拿到父类，避免重复包装
        while (ClassGenerator.isDynamicClass(c)) // can not wrapper on dynamic class.
        {
            c = c.getSuperclass();
        }
        // 指定类为 Object.class
        if (c == Object.class) {
            return OBJECT_WRAPPER;
        }
        // 从缓存中获得 Wrapper 对象，如果没有 创建Wrapper对象，并添加到缓存
        return WRAPPER_MAP.computeIfAbsent(c, WrapperCheck::makeWrapper);
    }

    private static WrapperCheck makeWrapper(Class<?> c) {
        if (c.isPrimitive()) { // 非私有类
            throw new IllegalArgumentException("Can not create wrapper for primitive type: " + c);
        }

        String name = c.getName(); // 类名
        ClassLoader cl = ClassUtils.getClassLoader(c); // 类加载器

        // 设置属性方法 `#setPropertyValue(o, n, v)` 的开头的代码
        StringBuilder c1 = new StringBuilder("public void setPropertyValue(Object o, String n, Object v){ ");
        // 获得属性方法 `#getPropertyValue(o, n)` 的开头的代码
        StringBuilder c2 = new StringBuilder("public Object getPropertyValue(Object o, String n){ ");
        // 调用方法 `#invokeMethod(o, n, p, v)` 的开头的代码
        StringBuilder c3 = new StringBuilder("public Object invokeMethod(Object o, String n, Class[] p, Object[] v) throws " + InvocationTargetException.class.getName() + "{ ");
        // 调用方法 `#clearParam(o)` 的开头的代码
        StringBuilder c4 = new StringBuilder("public void clearParam(Object o) throws " + InvocationTargetException.class.getName() + "{ ");

        // 添加每个方法的，被调用对象的类型转换的代码
        c1.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");
        c2.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");
        c3.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");
        c4.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");

        // 属性名与属性类型的集合，用于 `#hasProperty(...)` `#setPropertyValue(...)` `getPropertyValue(...)` 方法。
        Map<String, Class<?>> pts = new HashMap<>(); // <property name, property types>
        // 方法签名与方法对象的集合，用于 `#invokeMethod(..)`、`#clearParam()` 方法。
        Map<String, Method> ms = new LinkedHashMap<>(); // <method desc, Method instance>
        // 方法名数组用于 `#getMethodNames()` 方法。
        List<String> mns = new ArrayList<>(); // method names.
        // 定义的方法名数组，用于 `#getDeclaredMethodNames()` 方法。
        List<String> dmns = new ArrayList<>(); // declaring method names.
        // 定义的所有属性数组(<属性名称,属性类型>)，用于`#clearParam()`方法
        Map<String,Class<?>> df = new LinkedHashMap<>(); // declaring field.


        /*************************************************************************/
        final ClassPool classPool = new ClassPool(ClassPool.getDefault());
        classPool.insertClassPath(new LoaderClassPath(cl));
        classPool.insertClassPath(new DubboLoaderClassPath());
        List<String> allMethod = new ArrayList<>();
        try {
            final CtMethod[] ctMethods = classPool.get(c.getName()).getMethods();
            for (CtMethod method : ctMethods) {
                allMethod.add(ReflectUtils.getDesc(method));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 循环 public 属性，添加每个属性的设置和获得分别到 `#setPropertyValue(o, n, v)` 和 `#getPropertyValue(o, n)` 的代码
        // get all public field.
        for (Field f : c.getFields()) {
            String fn = f.getName();
            Class<?> ft = f.getType();
            if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()) || Modifier.isFinal(f.getModifiers())) {
                continue;
            }
            c1.append(" if( $2.equals(\"").append(fn).append("\") ){ ((").append(f.getDeclaringClass().getName()).append(")w).").append(fn).append('=').append(arg(ft, "$3")).append("; return; }");
            c2.append(" if( $2.equals(\"").append(fn).append("\") ){ return ($w)((").append(f.getDeclaringClass().getName()).append(")w).").append(fn).append("; }");
            pts.put(fn, ft);
        }

        c4.append(" String var = null;");
        c4.append(" java.util.List data = null;");
        for (Field f : c.getDeclaredFields()) {
            String fn = f.getName();
            Class<?> ft = f.getType();
            if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()) ||
                    Modifier.isFinal(f.getModifiers()) || Modifier.isPublic(f.getModifiers())) {
                continue;
            }
            df.put(fn,ft);
        }

        Method[] methods = Arrays.stream(c.getMethods())
                                 .filter(method -> allMethod.contains(ReflectUtils.getDesc(method)))
                                 .collect(Collectors.toList())
                                 .toArray(new Method[] {});
        // get all public method.
        boolean hasMethod = ClassUtils.hasMethods(methods);
        if (hasMethod) {
            Map<String, Integer> sameNameMethodCount = new HashMap<>((int) (methods.length / 0.75f) + 1);
            for (Method m : methods) {
                sameNameMethodCount.compute(m.getName(),
                        (key, oldValue) -> oldValue == null ? 1 : oldValue + 1);
            }

            c3.append(" try{");
            for (Method m : methods) {
                // 跳过来自 Object 的内置方法
                if (m.getDeclaringClass() == Object.class) { //ignore Object's method.
                    continue;
                }

                String mn = m.getName(); // 方法名
                // 使用方法名 + 方法参数长度来判断
                c3.append(" if( \"").append(mn).append("\".equals( $2 ) ");
                int len = m.getParameterTypes().length;
                c3.append(" && ").append(" $3.length == ").append(len);

                // 若相同方法名存在多个，增加参数类型数组的比较判断
                boolean overload = sameNameMethodCount.get(m.getName()) > 1;
                if (overload) {
                    if (len > 0) {
                        for (int l = 0; l < len; l++) {
                            c3.append(" && ").append(" $3[").append(l).append("].getName().equals(\"")
                                    .append(m.getParameterTypes()[l].getName()).append("\")");
                        }
                    }
                }

                c3.append(" ) { ");

                // 添加调用对象的对应方法的代码
                if (m.getReturnType() == Void.TYPE) {
                    c3.append(" w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");").append(" return null;");
                } else {
                    c3.append(" return ($w)w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");");
                }

                c3.append(" }");

                // 添加到 `mns` 中
                mns.add(mn);
                if (m.getDeclaringClass() == c) {
                    dmns.add(mn); // 添加到 `dmns` 中
                }
                // 添加到 `ms` 中
                ms.put(ReflectUtils.getDesc(m), m);
            }
            // 如果有方法，添加 `#invokeMethod(o, n, p, v)` 的 catch 的代码
            c3.append(" } catch(Throwable e) { ");
            c3.append("     throw new java.lang.reflect.InvocationTargetException(e); ");
            c3.append(" }");
        }
        // 添加 `#invokeMethod(o, n, p, v)` 的未匹配到方法的代码
        c3.append(" throw new ").append(NoSuchMethodException.class.getName()).append("(\"Not found method \\\"\"+$2+\"\\\" in class ").append(c.getName()).append(".\"); }");

        // 循环 setting/getting 方法，添加每个属性的设置和获得分别到 `#setPropertyValue(o, n, v)` 和 `#getPropertyValue(o, n)` 的代码
        // deal with get/set method.
        Matcher matcher;
        for (Map.Entry<String, Method> entry : ms.entrySet()) {
            String md = entry.getKey();
            Method method = entry.getValue();
            if ((matcher = ReflectUtils.GETTER_METHOD_DESC_PATTERN.matcher(md)).matches()) {
                String pn = propertyName(matcher.group(1));
                c2.append(" if( $2.equals(\"").append(pn).append("\") ){ return ($w)w.").append(method.getName()).append("(); }");
                pts.put(pn, method.getReturnType());
            } else if ((matcher = ReflectUtils.IS_HAS_CAN_METHOD_DESC_PATTERN.matcher(md)).matches()) {
                String pn = propertyName(matcher.group(1));
                c2.append(" if( $2.equals(\"").append(pn).append("\") ){ return ($w)w.").append(method.getName()).append("(); }");
                pts.put(pn, method.getReturnType()); // 添加到 `pts` 中
            } else if ((matcher = ReflectUtils.SETTER_METHOD_DESC_PATTERN.matcher(md)).matches()) {
                Class<?> pt = method.getParameterTypes()[0];
                String pn = propertyName(matcher.group(1));
                c1.append(" if( $2.equals(\"").append(pn).append("\") ){ w.").append(method.getName()).append('(').append(arg(pt, "$3")).append("); return; }");
                pts.put(pn, pt); // 添加到 `pts` 中
            }
        }
        c1.append(" throw new ").append(NoSuchPropertyException.class.getName()).append("(\"Not found property \\\"\"+$2+\"\\\" field or setter method in class ").append(c.getName()).append(".\"); }");
        c2.append(" throw new ").append(NoSuchPropertyException.class.getName()).append("(\"Not found property \\\"\"+$2+\"\\\" field or getter method in class ").append(c.getName()).append(".\"); }");

        // 循环计算属性 对应的方法是否正确
        df.forEach((fn, ft) -> {
            boolean isString = String.class.isAssignableFrom(ft); // 是否 字段类型对应 String
            boolean isList = List.class.isAssignableFrom(ft);  // 是否 字段类型对应 List
            if (!isString && !isList) return;

            String getMethodName = StrUtil.upperFirstAndAddPre(fn, "get"); // getXXX  方法名称
            String setMethodName = StrUtil.upperFirstAndAddPre(fn, "set"); // setXXX  方法名称
            String wrapperGetMethodDesc = wrapperMethodDesc(getMethodName, null, ft); // 返回get方法其描述信息
            String wrapperSetMethodDesc = wrapperMethodDesc(setMethodName, new Class[]{ft}, null); // 返回set方法其描述信息

            // 判断是否满足 描述信息的规则校验
            if (!ReflectUtils.GETTER_METHOD_DESC_PATTERN.matcher(wrapperGetMethodDesc).matches()) return;
            if (!ReflectUtils.SETTER_METHOD_DESC_PATTERN.matcher(wrapperSetMethodDesc).matches()) return;
            if (LOGGER.isDebugEnabled()) LOGGER.debug("field:【{}】type[{}]  wrapperGetMethodDesc:[{}] wrapperSetMethodDesc:[{}]  \n", fn, ft, wrapperGetMethodDesc, wrapperSetMethodDesc);

            // 同时存在get、set方法
            if (ms.containsKey(wrapperGetMethodDesc) && ms.containsKey(wrapperSetMethodDesc)) {
                if (isString)  {
                    c4.append("var= w.").append(getMethodName).append("(); \n");
                    c4.append("if(cn.hutool.core.util.StrUtil.isBlank(var)) {\n").
                            append("w.").append(setMethodName).append("(null); \n}");
                }
                if (isList) {
                    c4.append("data= w.").append(getMethodName).append("(); \n");
                    c4.append("if(cn.hutool.core.collection.CollUtil.isEmpty(data)) {\n").
                            append("w.").append(setMethodName).append("(null); \n}");
                }
            }
        });
//        c4.append("\n LOGGER.info(\"test\");");
        c4.append("} // END \n");


        // make class
        long id = WRAPPER_CLASS_COUNTER.getAndIncrement();
        ClassGenerator cc = ClassGenerator.newInstance(cl); // 创建 ClassGenerator 对象
        cc.setClassName(c.getName() + "Wrap" + id); // 设置类名
        cc.setSuperClass(WrapperCheck.class); // 设置父类为 WrapperCheck.class

        cc.addDefaultConstructor(); // 添加构造方法，参数 空
        cc.addField("public static String[] pns;"); // property name array.  添加静态属性 `pns` 的代码
        cc.addField("public static " + Map.class.getName() + " pts;"); // property type map.
        cc.addField("public static String[] mns;"); // all method name array.
        cc.addField("public static String[] dmns;"); // declared method name array.
        cc.addField("private final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(this.getClass());");

        // 添加静态属性 `mts` 的代码。每个方法的参数数组。
        for (int i = 0, len = ms.size(); i < len; i++) {
            cc.addField("public static Class[] mts" + i + ";");
        }

        // ======= 添加抽象方法的实现，到 `cc` 中
        cc.addMethod("public String[] getPropertyNames(){ return pns; }"); // 添加 `#getPropertyNames()` 的代码到 `cc`
        cc.addMethod("public boolean hasProperty(String n){ return pts.containsKey($1); }"); // 添加 `#hasProperty(n)` 的代码到 `cc`
        cc.addMethod("public Class getPropertyType(String n){ return (Class)pts.get($1); }"); // 添加 `#getPropertyType(n)` 的代码到 `cc`
        cc.addMethod("public String[] getMethodNames(){ return mns; }"); // 添加 `#getMethodNames()` 的代码到 `cc`
        cc.addMethod("public String[] getDeclaredMethodNames(){ return dmns; }"); // 添加 `#getDeclaredMethodNames()` 的代码到 `cc`
        cc.addMethod(c1.toString()); // 添加 `#setPropertyValue(o, n, v)` 的代码到 `cc`
        cc.addMethod(c2.toString()); // 添加 `#getPropertyValue(o, n)` 的代码到 `cc`
        cc.addMethod(c3.toString()); // 添加 `#invokeMethod(o, n, p, v)` 的代码到 `cc`
        cc.addMethod(c4.toString()); // 添加 `#cleanParam(o)` 的代码到 `cc`

        try {
            Class<?> wc = cc.toClass(c); // 生成类
            // setup static field.
            // 反射，设置静态变量的值
            wc.getField("pts").set(null, pts);
            wc.getField("pns").set(null, pts.keySet().toArray(new String[0]));
            wc.getField("mns").set(null, mns.toArray(new String[0]));
            wc.getField("dmns").set(null, dmns.toArray(new String[0]));
            int ix = 0;
            for (Method m : ms.values()) {
                wc.getField("mts" + ix++).set(null, m.getParameterTypes());
            }
            return (WrapperCheck) wc.getDeclaredConstructor().newInstance(); // 创建对象
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            cc.release();
            ms.clear();
            mns.clear();
            dmns.clear();
        }
    }

    private static String arg(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (cl == Boolean.TYPE) {
                return "((Boolean)" + name + ").booleanValue()";
            }
            if (cl == Byte.TYPE) {
                return "((Byte)" + name + ").byteValue()";
            }
            if (cl == Character.TYPE) {
                return "((Character)" + name + ").charValue()";
            }
            if (cl == Double.TYPE) {
                return "((Number)" + name + ").doubleValue()";
            }
            if (cl == Float.TYPE) {
                return "((Number)" + name + ").floatValue()";
            }
            if (cl == Integer.TYPE) {
                return "((Number)" + name + ").intValue()";
            }
            if (cl == Long.TYPE) {
                return "((Number)" + name + ").longValue()";
            }
            if (cl == Short.TYPE) {
                return "((Number)" + name + ").shortValue()";
            }
            throw new RuntimeException("Unknown primitive type: " + cl.getName());
        }
        return "(" + ReflectUtils.getName(cl) + ")" + name;
    }

    /**
     * 包装方法的详情
     * @param methodName 方法名称 {@link Method#getName()}
     * @param parameterTypes 方法的参数列表
     * @param returnType 方法的返回参数
     * @return
     * @see ReflectUtils#getDesc(Method)
     */
    private static String wrapperMethodDesc(String methodName,Class<?>[] parameterTypes, Class<?> returnType){
        if (returnType == null) {
            returnType = void.class;
        }
        StringBuilder ret = new StringBuilder(methodName).append('(');
        if (parameterTypes != null && parameterTypes.length > 0) {
            for (int i = 0; i < parameterTypes.length; i++) {
                ret.append(ReflectUtils.getDesc(parameterTypes[i]));
            }
        }
        ret.append(')').append(ReflectUtils.getDesc(returnType));
        return ret.toString();

//        StringBuilder ret = new StringBuilder();
//        ret.append(ReflectUtils.getName(returnType)).append(' ');
//        ret.append(methodName).append('(');
//        if (parameterTypes != null && parameterTypes.length > 0) {
//            for (int i = 0; i < parameterTypes.length; i++) {
//                if (i > 0) {
//                    ret.append(',');
//                }
//                ret.append(ReflectUtils.getName(parameterTypes[i]));
//            }
//        }
//        ret.append(')');
//        return ret.toString();
    }

    private static String args(Class<?>[] cs, String name) {
        int len = cs.length;
        if (len == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(arg(cs[i], name + "[" + i + "]"));
        }
        return sb.toString();
    }

    private static String propertyName(String pn) {
        return pn.length() == 1 || Character.isLowerCase(pn.charAt(1)) ? Character.toLowerCase(pn.charAt(0)) + pn.substring(1) : pn;
    }

    /**
     * get property name array.
     *
     * @return property name array.
     */
    abstract public String[] getPropertyNames();

    /**
     * get property type.
     *
     * @param pn property name.
     * @return Property type or nul.
     */
    abstract public Class<?> getPropertyType(String pn);

    /**
     * has property.
     *
     * @param name property name.
     * @return has or has not.
     */
    abstract public boolean hasProperty(String name);

    /**
     * get property value.
     *
     * @param instance instance.
     * @param pn       property name.
     * @return value.
     */
    abstract public Object getPropertyValue(Object instance, String pn) throws NoSuchPropertyException, IllegalArgumentException;

    /**
     * set property value.
     *
     * @param instance instance.
     * @param pn       property name.
     * @param pv       property value.
     */
    abstract public void setPropertyValue(Object instance, String pn, Object pv) throws NoSuchPropertyException, IllegalArgumentException;

    /**
     * get property value.
     *
     * @param instance instance.
     * @param pns      property name array.
     * @return value array.
     */
    public Object[] getPropertyValues(Object instance, String[] pns) throws NoSuchPropertyException, IllegalArgumentException {
        Object[] ret = new Object[pns.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = getPropertyValue(instance, pns[i]);
        }
        return ret;
    }

    /**
     * set property value.
     *
     * @param instance instance.
     * @param pns      property name array.
     * @param pvs      property value array.
     */
    public void setPropertyValues(Object instance, String[] pns, Object[] pvs) throws NoSuchPropertyException, IllegalArgumentException {
        if (pns.length != pvs.length) {
            throw new IllegalArgumentException("pns.length != pvs.length");
        }

        for (int i = 0; i < pns.length; i++) {
            setPropertyValue(instance, pns[i], pvs[i]);
        }
    }

    /**
     * get method name array.
     *
     * @return method name array.
     */
    abstract public String[] getMethodNames();

    /**
     * get method name array.
     *
     * @return method name array.
     */
    abstract public String[] getDeclaredMethodNames();

    /**
     * has method.
     *
     * @param name method name.
     * @return has or has not.
     */
    public boolean hasMethod(String name) {
        for (String mn : getMethodNames()) {
            if (mn.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * invoke method. 调用方法
     *
     * @param instance instance.  被调用的对象
     * @param mn       method name. 方法名
     * @param types    参数类型数组
     * @param args     argument array. 参数数组
     * @return return value.返回值
     */
    abstract public Object invokeMethod(Object instance, String mn, Class<?>[] types, Object[] args) throws NoSuchMethodException, InvocationTargetException;

    /**
     * clean param valid
     * @param instance instance
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    abstract public void clearParam(Object instance) throws NoSuchMethodException, InvocationTargetException;

    public void clearParamNonThrows(Object instance, Logger logger) {
        try {
            clearParam(instance);
        } catch (NoSuchMethodException | InvocationTargetException e) {
            if (logger != null) {
                LOGGER.error("=== [{}] clean Param error!", instance.getClass().getSimpleName());
            }
        }
    }
}
