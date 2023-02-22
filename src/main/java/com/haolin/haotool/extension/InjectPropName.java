package com.haolin.haotool.extension;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface InjectPropName {

    /**
     * SPI中需要注入的 属性名称. 默认为  get|set方法 截后n位
     *
     * @author Ahaolin
     */
    String propName() default "";
}
