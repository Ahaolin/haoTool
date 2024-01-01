package com.haolin.haotool.util.context;

import cn.hutool.core.util.TypeUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 获取泛型对应的真实类型
 * @param <T>
 */
public interface TypeName<T> {

    /**
     * 对象模型
     * @return
     */
    default Class<T> type(){
        return (Class<T>) TypeUtil.getTypeArgument(getClass(),0);
    }


    /**
     * 对象名称
     * @return
     */
    String name();

    /**
     * 获取泛型参数【父类、父接口】
     * <pre>
     *     public class TypeNameImpl implements TypeName<T>{}
     *     public class TypeNameImpl extends ArrayList<E>{}
     *
     *     // 返回E
     * </pre>
     *
     *
     * @param clazz 子类
     * @return
     */
    public static Class<?> getActualTypeArgument(Class<?> clazz){
        Class<?> result = null;
        // 指定父类
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)){
            // 指定父接口的书接
            genericSuperclass = clazz.getGenericInterfaces()[0];
            if (!(genericSuperclass instanceof ParameterizedType)){
                return null;
            }
        }

        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        if (actualTypeArguments != null && actualTypeArguments.length > 0){
            result = ((Class<?>) actualTypeArguments[0]);
        }
        return result;
    }
}
