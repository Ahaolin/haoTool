package com.haolin.haotool.util.expression.support;

import com.haolin.haotool.extension.URL;
import com.haolin.haotool.util.expression.ExpressionEngine;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * Spring-Expression引擎封装
 * <a href='https://github.com/spring-projects/spring-framework/tree/master/spring-expression'></a>
 *
 * @author looly
 * @since 5.5.0
 */
public class SpELExpressionEngine  implements ExpressionEngine  {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    @Override
    public Object eval(URL url, String expression, Map<String, Object> context) {
        final EvaluationContext evaluationContext = new StandardEvaluationContext();
        context.forEach(evaluationContext::setVariable);
        return PARSER.parseExpression(expression).getValue(evaluationContext);
    }

    @Override
    public void close(URL url){
    }

}