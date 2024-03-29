/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haolin.haotool.extension.factory;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.haolin.haotool.extension.ExtensionFactory;
import org.springframework.context.ApplicationContext;

import java.util.Set;

/**
 * SpringExtensionFactory
 */
public class SpringExtensionFactory implements ExtensionFactory {

    /**
     * Spring Context 集合
     */
    private static final Set<ApplicationContext> contexts = new ConcurrentHashSet<>();

    public static void addApplicationContext(ApplicationContext context) {
        contexts.add(context);
    }

    public static void removeApplicationContext(ApplicationContext context) {
        contexts.remove(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<T> type, String name) {
        for (ApplicationContext context : contexts) {
            if (context.containsBean(name)) {
                // 获得属性
                Object bean = context.getBean(name);
                // 判断类型
                if (type.isInstance(bean)) {
                    return (T) bean;
                }
            }
        }
        return null;
    }

}
