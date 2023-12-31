package com.haolin.haotool.bytecode;

import java.lang.annotation.*;

/**
 * 使用{@link WrapperCheck#clearParam(Object)} 忽视某些属性
 *
 * @see WrapperCheck
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IgnoreWrapperCheck {

}
