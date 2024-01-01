package com.haolin.haotool.util.context;

import com.haolin.haotool.util.tree.ITreeVO;
import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.MethodTypeSignature;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * 上下文泛型定义
 *
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface NameFunction<T, R> extends Function<T, R>, TypeName<R>, Serializable {

    R apply(T t);

    @Override
    default Class<R> type() {
        // 调用writeReplace()方法,返回一个SerializedLambda对象
        SerializedLambda lambda = this.lambda();
        SignatureParser parser = SignatureParser.make();
        MethodTypeSignature methodSig = parser.parseMethodSig(lambda.getImplMethodSignature());
        ClassTypeSignature signature = (ClassTypeSignature) methodSig.getReturnType();
        try {
            return (Class<R>) Class.forName(signature.getPath().get(0).getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 返回对应方法签名（包含所有类目+返回值）
     * <pre>
     *     {@link ITreeVO#covertTreeNode()}
     *     (Lcom/haolin/haotool/util/tree/ITreeVO;)Lcom/haolin/haotool/util/tree/TreeNode;##lambda$testBuild$e37e75d3$1
     * </pre>
     *
     * @return
     */
    @Override
    default String name() {
        SerializedLambda lambda = lambda();
        return lambda.getInstantiatedMethodType() + "##" + lambda.getImplMethodName();
    }


    default SerializedLambda lambda() {
        Method method;
        try {
            method = this.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            // 调用writeReplace()方法,返回一个SerializedLambda对象
            return (SerializedLambda) method.invoke(this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("[NameFunction] error! class=" + this.getClass().getName(), e);
        }
    }

}
