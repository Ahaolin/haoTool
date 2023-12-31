package com.haolin.haotool.util.expression;

import com.google.common.collect.ImmutableMap;
import com.haolin.haotool.extension.ExtensionLoader;
import com.haolin.haotool.extension.URL;
import com.haolin.haotool.util.expression.support.SpELExpressionEngine;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionEngineTest {

    private Map<String,Object> context;
    private URL url;

    @Before
    public void beforeMethod(){

    }

    @Test
    public void testSpEL(){
        context = new HashMap<>(ImmutableMap.of("a", 1,"b",2));
        url = new URL(null, null, 80);
        String expression = "#a";

        ExpressionEngine engine = null;
        try {
            engine = ExtensionLoader.getExtensionLoader(ExpressionEngine.class).getAdaptiveExtension();
            Object eval = engine.eval(url, expression, context);
            assertEquals(eval, 1);

            expression = "#a + #b";
            eval = engine.eval(url, expression, context);
            assertEquals(eval, 3);
        } finally {
            assert engine != null;
            engine.close(url);
        }
    }
}