package com.haolin.dubbo.common.util.holder;

import com.haolin.dubbo.common.exce.HolderException;

import java.util.concurrent.ConcurrentHashMap;

public abstract class SyncHolder<K,M> {

    /**
     * 根据key 获取指定实例
     * @param LOCAL_MAP 使用本地缓存，该参数 最后 是静态的。
     * @throws HolderException key不合法 || {@link #computeVal(Object)}方法  返回该错误。
     */
    public M getVal(final K key, final ConcurrentHashMap<K, Holder<M>> LOCAL_MAP) throws HolderException {
        if (isKeyError(key)) throw new HolderException("key is ERROR!!!!", key, HolderStep.KEY_IS_ERROR);
        Holder<M> holder = LOCAL_MAP.get(key);
        if (holder == null) {
            LOCAL_MAP.putIfAbsent(key, new Holder<>());
            holder = LOCAL_MAP.get(key);
        }
        M instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    try {
                        instance = computeVal(key);
                        holder.set(instance);
                    } catch (Exception e) {
                        throw new HolderException(e, key, HolderStep.COMPUTE_VAL_ERROR);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * 本地缓存未加载到时  加载val的返回
     */
    protected abstract M computeVal(K key) throws Exception;

    /**
     * 校验key. 错误返回true
     */
    protected abstract boolean isKeyError(K key);
}
