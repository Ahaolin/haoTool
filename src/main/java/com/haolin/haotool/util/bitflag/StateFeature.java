package com.haolin.haotool.util.bitflag;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 二进制状态提供方
 */
public interface StateFeature {

    /**
     * 返回默认的状态
     *
     * @return
     */
    long getMask();

    /**
     * 计算其mask
     *
     * @param ordinal 枚举所在位置,int类型最多30种,long类型 最多62种
     * @return
     */
    default long computeMask(int ordinal) {
        return 1L << ordinal;
    }

    public static <T extends StateFeature> boolean isEnabled(long state, T feature) {
        long mask = feature.getMask();
        return (state & mask) != 0;
    }

    /**
     * 配置某个特性
     *
     * @param state    特性值
     * @param feature  某个特性
     * @param isEnable 是否开启 or 移除
     */
    public static <T extends StateFeature> long config(long state, T feature, boolean isEnable) {
        if (isEnable) {
            state |= feature.getMask();
        } else {
            state &= ~feature.getMask();
        }
        return state;
    }

    /**
     * 开启某些特性的值
     *
     * @param features 特性数组
     * @return
     */
    public static long of(StateFeature... features) {
        if (features == null || features.length == 0)
            return 0L;
        long val = 0;
        for (StateFeature feature : features) {
            val = StateFeature.config(val, feature, true);
        }
        return val;
    }

    /**
     * 判断特性值包含哪些特性
     *
     * @param state        特性值
     * @param featureArray 匹配特性数组
     * @return 包含的特性
     */
    default <T extends StateFeature> Set<T> resolve(long state, T[] featureArray) {
        if (featureArray == null || featureArray.length == 0) {
            throw new IllegalArgumentException("特征数组为null");
        }
        if (state == 0) {
            return Collections.emptySet();
        }
        Set<T> featureSet = new HashSet<>();
        for (T feature : featureArray) {
            if (isEnabled(state, feature)) featureSet.add(feature);
        }
        return featureSet;
    }
}

