package com.haolin.haotool.extension.factory.config;


import com.haolin.haotool.extension.factory.SpringExtensionFactory;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 如果需要使用 在SPI中使用Spring的类
 *  务必 创建<b>META-INF/custom/internal/com.haolin.haotool.extension.ExtensionFactory</b>
 *  内容如下：
 *  <pre>
 *      spring=com.haolin.haotool.extension.factory.SpringExtensionFactory
 *  </pre>
 */
public class ExtensionFactoryConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 对标ServiceBeen 的该方法
        SpringExtensionFactory.addApplicationContext(applicationContext);
        // 省略了 后面的监听器、及其暴露行为
    }
}
