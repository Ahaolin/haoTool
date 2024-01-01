package com.haolin.haotool.util.context.impl;

import com.google.common.collect.Maps;
import com.haolin.haotool.util.context.IBaseContext;
import com.haolin.haotool.util.context.NameFunction;

import java.util.Map;

/**
 * 默认的本地上下文
 */
public class DefaultContext implements IBaseContext {

    /**
     * 定义存储结构
     */
    private final Map<String, Object> contextMap = Maps.newConcurrentMap();


    /**
     * 获取一个存储对象
     *
     * @param name 更加name定义一个唯一存储对象
     * @return
     */
    @Override
    public <T, R> Store<R> with(NameFunction<T, R> name) {
        return new Store<>(name, this);
    }

    /**
     * 返回上下文Map对象
     *
     * @return
     */
    @Override
    public Map<String, Object> getContextMap() {
        return contextMap;
    }
}
