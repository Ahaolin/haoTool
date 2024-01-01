package com.haolin.haotool.util.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 上下文定义
 */
public interface IBaseContext {

    /**
     * 获取一个存储对象
     *
     * @param name 更加name定义一个唯一存储对象
     * @param <T>
     * @param <R>
     * @return
     */
    <T, R> Store<R> with(NameFunction<T, R> name);


    /**
     * 返回上下文Map对象
     *
     * @return
     */
    Map<String, Object> getContextMap();


    /**
     * 存储对象
     *
     * @param <T>
     */
    class Store<T> {
        private static final Logger LOGGER = LoggerFactory.getLogger(Store.class);

        protected NameFunction function;
        protected IBaseContext data;

        private String key;

        public Store(NameFunction function, IBaseContext data) {
            this.function = function;
            this.data = data;

            Objects.requireNonNull(function);
            this.key = function.name();
        }

        /**
         * 获取对象值
         * <pre>
         *     此处泛型导致类型不匹配
         * </pre>
         *
         * @return
         */
        public T get() {
            Object value = data.getContextMap().get(this.getKey());
            if (value == null) return null;
            else if (LOGGER.isDebugEnabled() && value.getClass() != function.type()) {
                LOGGER.debug("key:{} value:{}, 存储类型={}与声明类型={}不一致！", getKey(), value, value.getClass(), function.type());
            }
            return (T) value;
        }


        /**
         * 缺失对象时使用
         *
         * @param supplier the supplier to compute a value, not null!
         * @return
         * @see Map#computeIfAbsent(Object, Function)
         */
        public T computeIfAbsent(Supplier<? extends T> supplier) {
            Objects.requireNonNull(supplier);
            T v;
            if ((v = get()) == null) {
                T newValue;
                if ((newValue = supplier.get()) != null) {
                    set(newValue);
                    return newValue;
                }
            }
            return v;
        }


        public void set(T value) {
            data.getContextMap().put(getKey(), value);
        }

        public String getKey() {
            return this.key;
        }
    }
}
