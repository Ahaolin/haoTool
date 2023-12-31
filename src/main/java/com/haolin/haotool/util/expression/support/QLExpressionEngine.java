package com.haolin.haotool.util.expression.support;

import cn.hutool.extra.expression.ExpressionException;
import cn.hutool.extra.expression.engine.qlexpress.QLExpressEngine;
import com.haolin.haotool.extension.URL;
import com.haolin.haotool.util.expression.ExpressionEngine;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * QLExpress引擎封装<br>
 * 见：https://github.com/alibaba/QLExpress
 *
 * @see QLExpressEngine
 * @author looly
 * @since 5.8.9
 */
public class QLExpressionEngine implements ExpressionEngine  {

    private static final ExpressRunner QL_ENGINE = new ExpressRunner();

    @Override
    public Object eval(URL url, String expression, Map<String, Object> context) {
        final DefaultContext<String, Object> defaultContext = new DefaultContext<>();
        defaultContext.putAll(context);
        try {
            return QL_ENGINE.execute(expression, defaultContext, null, true, false);
        } catch (final Exception e) {
            throw new ExpressionException(e);
        }
    }

    @Override
    public void close(URL url){
    }

}