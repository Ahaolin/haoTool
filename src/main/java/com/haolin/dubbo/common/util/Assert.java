package com.haolin.dubbo.common.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import com.haolin.dubbo.common.constants.LocaleBizServiceException;

import java.util.Collection;
import java.util.function.Supplier;

public class Assert {
    private Assert() {
    }

    public static final String NORMAL_SIMPLE_MESSAGE = "normal.simple.res";

    /**
     * expression为false 抛出异常
     */
    public static void isTrue(boolean expression, String message, Object... args) {
        if (!expression) {
            buildException(message, args);
        }
    }

    /**
     * expression为 true 抛出异常
     */
    public static void isFalse(boolean expression, String message, Object... args) {
        if (expression) {
            buildException(message, args);
        }
    }

    /**
     * number为 不是数字 抛出异常
     */
    public static void isNumber(String number, String message, Object... args) {
        if (!NumberUtil.isNumber(number)) {
            buildException(message, args);
        }
    }

    /**
     * obj为null 抛出异常
     */
    public static void notNull(Object obj, String message, Object... args) {
        if (obj == null) {
            buildException(message, args);
        }
    }

    /**
     * test为null 或 为空字符串 抛出异常
     */
    public static void isBlank(String test, String message, Object... args) {
        if (StringUtils.isNotBlank(test)) {
            buildException(message, args);
        }
    }

    /**
     * test不为null且不为空字符串 抛出异常
     */
    public static void notBlank(String test, String message, Object... args) {
        if (StringUtils.isBlank(test)) {
            buildException(message, args);
        }
    }

    /**
     * collection 为null 或 为空j集合 抛出异常
     */
    public static void notEmpty(Collection<?> collection, String message, Object... args) {
        if (CollectionUtil.isEmpty(collection)) {
            buildException(message, args);
        }
    }

    /**
     * 如果supplier返回结果为false  抛出异常
     */
    public static void notSupplier(Supplier<Boolean> supplier, String message, Object... args) {
        Assert.notNull(supplier, "error.param.missing");
        if (!supplier.get()) {
            buildException(message, args);
        }
    }

    /**
     * 如果supplier返回结果为true  抛出异常
     */
    public static void isSupplier(Supplier<Boolean> supplier, String message, Object... args) {
        Assert.notNull(supplier, "error.param.missing");
        if (supplier.get()) {
            buildException(message, args);
        }
    }


    private static void buildException(String message, Object[] args) {
        if (args == null) {
            throw new LocaleBizServiceException(message);
        }
        throw new LocaleBizServiceException(message, args);
    }

//    public class LocaleBizServiceException extends BizServiceException {
//        public LocaleBizServiceException(String errorCode, Object... args) {
//            super(Messages.getMessage(errorCode, args), errorCode);
//        }
//    }

}
