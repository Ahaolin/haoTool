package com.haolin.haotool.util.expression;


import com.haolin.haotool.extension.Adaptive;
import com.haolin.haotool.extension.SPI;
import com.haolin.haotool.extension.URL;

import java.util.Map;

/**
 * 表达式引擎API接口，通过实现此接口，完成表达式的解析和执行
 *
 * @author looll, independenter
 * @see cn.hutool.extra.expression.ExpressionEngine
 * @since 5.5.0
 */
@SPI("spel")
public interface ExpressionEngine {

    /**
     * 执行表达式
     *
     * @param url        访问方法参数
     * @param expression 表达式
     * @param context    表达式上下文，用于存储表达式中所需的变量值等
     * @return 执行结果
     */
    @Adaptive({"protocol"})
    Object eval(URL url, String expression, Map<String, Object> context);


    @Adaptive({"protocol"})
    void close(URL url);

}
