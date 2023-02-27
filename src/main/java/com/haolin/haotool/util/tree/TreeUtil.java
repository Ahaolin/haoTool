package com.haolin.haotool.util.tree;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.haolin.dubbo.common.util.holder.Holder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TreeUtil {

    /**
     * 递归的转化 为 clazz类型的集合
     * @param build 需要转换前的原始数据 。 如果build的 父id有，但是找不到 父对象，移除
     * @param clazz 需要转换的类型
     */
    public static <M, N extends ITreeVO<N, M>> List<N> covertData(List<Tree<M>> build, Class<N> clazz) {
        if (CollectionUtil.isEmpty(build)) return Collections.emptyList();
        List<N> results = new ArrayList<>();
        buildChildren(build, results, clazz);
        return results;
    }

    public static <M, N extends ITreeVO<N, M>> void buildChildren(Collection<Tree<M>> build, List<N> results, Class<N> clazz) {
        build.forEach(v -> {
            final N result = getResult(clazz, v);
            result.restoreData(v);
            final List<Tree<M>> children = v.getChildren();
            if (CollectionUtil.isNotEmpty(children)) {
                result.setChildren(new ArrayList<>());
                buildChildren(children, result.getChildren(), clazz);
            }
            results.add(result);
        });
    }

    private final static  Map<Class<?>,Holder<Constructor<?>>> CACHE_CONSTRUCTOR = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked"})
    public static <N extends ITreeVO<N, M>, M> N getResult(Class<N> clazz, Tree<M> v) {
        Objects.requireNonNull(clazz, "getResult() class must not null!");
        Holder<Constructor<?>> holder = CACHE_CONSTRUCTOR.get(clazz);
        if (holder == null) {
            CACHE_CONSTRUCTOR.putIfAbsent(clazz, new Holder<>());
            holder = CACHE_CONSTRUCTOR.get(clazz);
        }
        Constructor<?> constructor = holder.get();
        if (constructor == null) {
            synchronized (holder) {
                try {
                    constructor = ReflectUtil.getConstructor(clazz);
                    holder.set(constructor);
                } catch (Exception e) {
                    throw new RuntimeException(
                            StrUtil.format("class {}的构造方法需要一个空构造", clazz.getName())
                    );
                }
            }
        }
        try {
            return (N) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(StrUtil.format("class {}的构造方法错误！！！", clazz.getName()));
        }
    }

}
