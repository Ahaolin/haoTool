package com.haolin.haotool.extension.factory.config;


import com.haolin.haotool.extension.factory.SpringExtensionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ExtensionFactoryConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 对标ServiceBeen 的该方法
        SpringExtensionFactory.addApplicationContext(applicationContext);
        // 省略了 后面的监听器、及其暴露行为
    }
}
