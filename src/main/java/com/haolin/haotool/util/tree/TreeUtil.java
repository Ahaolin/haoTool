package com.haolin.haotool.util.tree;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TreeUtil {

    /**
     * 递归的转化 为 clazz类型的集合
     * @param build 需要转换前的原始数据 。 如果build的 父id有，但是找不到 父对象，移除
     * @param clazz 需要转换的类型
     */
    protected static <M, N extends ITreeVO<N, M>> List<N> covertData(List<Tree<M>> build, Class<N> clazz) {
        if (CollectionUtil.isEmpty(build)) return Collections.emptyList();
        List<N> results = new ArrayList<>();
        buildChildren(build, results, clazz);
        return results;
    }

    protected static <M, N extends ITreeVO<N, M>> void buildChildren(Collection<Tree<M>> build, List<N> results, Class<N> clazz) {
        build.forEach(v -> {
            final N result = getResult(clazz, v);
            result.covertData(v);
            final List<Tree<M>> children = v.getChildren();
            if (CollectionUtil.isNotEmpty(children)) {
                result.setChildren(new ArrayList<>());
                buildChildren(children, result.getChildren(), clazz);
            }
            results.add(result);
        });
    }

    private static <N extends ITreeVO<N, M>, M> N getResult(Class<N> clazz, Tree<M> v) {
        try {
            return ReflectUtil.getConstructor(clazz).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    StrUtil.format("class {}的构造方法需要一个空构造", clazz.getName())
            );
        }
    }

}
