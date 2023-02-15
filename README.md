> 一个属于自己的工具类

## 1.QUICK Start

### 1.1 引入maven

```xml
<dependency>
           <groupId>com.ahaolin</groupId>
          <artifactId>haoTool</artifactId>
	<version>1.1.0</version>
 </dependency>
```

### 1.2 创建Dmeo

#### 1.2.1 spring 启动添加如下代码

![image-20230215110705022](https://ahaolin-public-img.oss-cn-hangzhou.aliyuncs.com/img/202302151208872.png)

```java
// META-INF/custom/internal/com.haolin.haotool.extension.ExtensionFactory
spring=com.haolin.haotool.extension.factory.SpringExtensionFactory
    
// META-INF/custom/internal/com.haolin.haotool.extension.ExtensionFactory
demo=com.example.demo.service.impl.DemoServiceImpl 
    
// springboot 启动 强烈建议添加如下代码
ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
SpringExtensionFactory.addApplicationContext(context);
```

#### 1.2.2 DemoService

```java
@SPI
public interface DemoService {
    void sayHello();
}

import com.example.demo.service.DemoService;
import com.example.demo.service.InjectService;

public class DemoServiceImpl implements DemoService {
    /*
     * this service from spring context. use it must follow conditions [form dubbo 2.7.x]
     * 1. context must be register. like [SpringExtensionFactory.addApplicationContext(run);]
     * 2. inject service  must find from spring context. and [※inject name]
     * 3. set method | public method | param.size == 1
     */
    private InjectService injectServiceImpl;

    public void setInjectServiceImpl(InjectService injectServiceImpl) {
        this.injectServiceImpl = injectServiceImpl;
    }

    @Override
    public void sayHello() {
        System.out.println();
        System.out.println("ces DemoServiceImpl==== sayHello [" + injectServiceImpl.test());
    }
}
```

> 注意：如果想要再`@SPI`中使用spring context中的bean【也可来自其他context，具体见`com.haolin.haotool.extension.ExtensionFactory`】,需要满足以下3个条件
>
> 1. spring上下文需要添加。类似[1.21 SpringExtensionFactory.addApplicataionContext](#1.2.1 spring 启动添加如下代码)。
> 2. 存在public set方法，且参数数量为1。
> 3. 需要注入的service ，必须能够从spring上下文中找到。【首先根据 setxx()方法 获取bean的名称，根据名称从上下文中获取bean。】

#### 1.2.3 Test

```java
@GetMapping("/demo1")
public String demo1(){
    // 该service位于spring上下文中
    InjectService bean = applicationContext.getBean(InjectService.class);
    bean.test();

    // 调用DemoService, 内部调用spring中的bean【InjectService】
    ExtensionLoader<DemoService> demoServiceExtensionLoader = ExtensionLoader.getExtensionLoader(DemoService.class);
    demoServiceExtensionLoader.getExtension("demo").sayHello();

    // 查看ExtensionFactory 存在哪些SPI类
    Set<String> supportedExtensions = ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getSupportedExtensions();
    return supportedExtensions.toString(); // [spi, spring]
}
```



## 2.已经实现的功能

- 完成类似 Dubbo Wrapper的方法， 专注于前端参数重置。

  ```java
  WrapperCheck wrapper = WrapperCheck.getWrapper(Form.class);
  wrapper.clearParam(form);
  ```

- 实现了类似dubbo的spi机制。[Dubbo SPI 扩展实现说明 - 扩展点开发指南 ](https://www.bookstack.cn/read/dubbo-3.1-zh/c3b9049b6a0c218c.md) 【区别实现的是2.7.x版本，对比3.0有略微差异】
