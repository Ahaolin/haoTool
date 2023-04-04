package com.haolin.haotool.util;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.haolin.dubbo.common.util.holder.Holder;
import com.haolin.dubbo.common.exce.HolderException;
import com.haolin.dubbo.common.util.holder.HolderStep;
import com.haolin.dubbo.common.util.holder.SyncHolder;
import com.haolin.haotool.bytecode.Form;
import com.haolin.haotool.util.tree.TreeBuilderTest;
import com.haolin.haotool.util.tree.support.TreeUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public class SyncHolderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeBuilderTest.class);


    @Test
    public void testReflect(){
        StopWatch watch = new StopWatch();

        watch.start("holder");
        MockTreeUtil mock = new MockTreeUtil();

        for (long i = 0; i < 1000000000L; i++) {
            mock.getResult(Form.class);
        }
        watch.stop();
        LOGGER.info("=== holder name execute 【{}】ms", watch.getTotalTimeMillis());

        watch.start("reflect");
        for (long i = 0; i < 1000000000L; i++) {
            try {
                Constructor<Form> constructor = Form.class.getConstructor();
                constructor.newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        watch.stop();
        LOGGER.info("=== reflect name execute 【{}】ms", watch.getTotalTimeMillis());
        LOGGER.info("{}",watch.prettyPrint());

    }

}


/**
 * mock class. 另一种写法
 * @see TreeUtil#getResult(Class, Tree)
 */
class MockTreeUtil extends SyncHolder<Class<?>, Constructor<?>> {

    private static final ConcurrentHashMap<Class<?>, Holder<Constructor<?>>> LOCAL_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked"})
    public <N> N getResult(Class<N> clazz) {
        Constructor<?> constructor = null;
        try {
            constructor = getVal(clazz, LOCAL_MAP);
        } catch (HolderException e) {
            // 针对异常 抛出自定义异常
            HolderStep step = e.getHolderStep();
            switch (step) {
                case KEY_IS_ERROR:
                    throw new NullPointerException("getResult() class must not null!");
                case COMPUTE_VAL_ERROR:
                    throw new RuntimeException(
                            StrUtil.format("class {}的构造方法需要一个空构造", e.getKey().toString())
                    );
            }
        }
        try {
            return (N) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(StrUtil.format("class {}的构造方法错误！！！", clazz.getName()));
        }
    }

    /**
     * 本地缓存未加载到时  加载val的返回
     */
    @Override
    protected Constructor<?> computeVal(Class<?> clazz) {
        return ReflectUtil.getConstructor(clazz);
    }

    /**
     * 校验key. 错误返回true
     */
    @Override
    protected boolean isKeyError(Class<?> key) {
        return key == null;
    }
}

