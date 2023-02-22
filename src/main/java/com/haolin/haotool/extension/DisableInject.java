package com.haolin.haotool.extension;

import java.lang.annotation.*;

/**
 * dubbo 官方注解
 * @since 3.0.x
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DisableInject {
}