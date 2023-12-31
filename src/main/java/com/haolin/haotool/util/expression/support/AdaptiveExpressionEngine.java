package com.haolin.haotool.util.expression.support;

import com.haolin.haotool.extension.Adaptive;
import com.haolin.haotool.extension.ExtensionLoader;
import com.haolin.haotool.extension.URL;
import com.haolin.haotool.util.expression.ExpressionEngine;

import java.util.Map;

/**
 * AdaptiveExpressionEngine. (SPI, Singleton, ThreadSafe)
 *
 * 自适应 ExpressionEngine 实现类
 */
@Adaptive
public class AdaptiveExpressionEngine implements ExpressionEngine {

    /**
     * 默认编辑器的拓展名
     */
    private static volatile String DEFAULT_Engine;

    public static void setDefaultExpressionEngine(String compiler) {
        DEFAULT_Engine = compiler;
    }

    @Override
    public Object eval(URL url, String expression, Map<String, Object> context) {
        ExpressionEngine engine;

        ExtensionLoader<ExpressionEngine> loader = ExtensionLoader.getExtensionLoader(ExpressionEngine.class);
        String name = DEFAULT_Engine;
        // 使用设置的拓展名，获得 Compiler 拓展对象
        if (name != null && name.length() > 0) {
            engine = loader.getExtension(name);
            // 获得默认的 Compiler 拓展对象
        } else {
            engine = loader.getDefaultExtension();
        }
        // 编译类
        return engine.eval(url, expression, context);
    }

    @Override
    public void close(URL url) {
        ExpressionEngine engine;

        ExtensionLoader<ExpressionEngine> loader = ExtensionLoader.getExtensionLoader(ExpressionEngine.class);
        String name = DEFAULT_Engine;
        // 使用设置的拓展名，获得 Compiler 拓展对象
        if (name != null && name.length() > 0) {
            engine = loader.getExtension(name);
            // 获得默认的 Compiler 拓展对象
        } else {
            engine = loader.getDefaultExtension();
        }
        // 编译类
        engine.close(url);
    }
}
