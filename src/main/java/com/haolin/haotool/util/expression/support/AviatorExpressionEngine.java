package com.haolin.haotool.util.expression.support;

import cn.hutool.extra.expression.engine.aviator.AviatorEngine;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Options;
import com.haolin.haotool.extension.URL;
import com.haolin.haotool.util.expression.ExpressionEngine;

import java.util.Map;

/**
 * Aviator引擎封装<br>
 * 见：https://github.com/killme2008/aviatorscript
 *
 * @see AviatorEngine
 *
 * @author looly
 * @since 5.5.0
 */
public class AviatorExpressionEngine implements ExpressionEngine {

	private static final AviatorEvaluatorInstance AVIATOR_ENGINE = AviatorEvaluator.getInstance();

//	static {
//        AVIATOR_ENGINE.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL,true);
//    }

	@Override
	public Object eval(URL url, String expression, Map<String, Object> context) {
		return AVIATOR_ENGINE.execute(expression, context);
	}

    @Override
    public void close(URL url){
        AVIATOR_ENGINE.clearExpressionCache();
    }

}
