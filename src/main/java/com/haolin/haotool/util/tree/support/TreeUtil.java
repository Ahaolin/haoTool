package com.haolin.haotool.util.tree.support;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.haolin.dubbo.common.util.holder.Holder;
import com.haolin.haotool.extension.URL;
import com.haolin.haotool.util.tree.ITreeVO;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TreeUtil {

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

    /**
     * 定制化字段设置
     */
    public static TreeNodeConfig customTreeNodeConfig(URL url) {
        TreeNodeConfig nodeConfig = TreeNodeConfig.DEFAULT_CONFIG;
        if (url.getParameter("tree.enabled", false)) {
            String idKey = url.getParameter("tree.idKey", "id");
            String parentIdKey = url.getParameter("tree.parentIdKey", "parentId");
            String weightKey = url.getParameter("tree.weightKey", "weight");
            String nameKey = url.getParameter("tree.nameKey", "name");
            String childrenKey = url.getParameter("tree.childrenKey", "children");

            nodeConfig = new TreeNodeConfig().setIdKey(idKey).setParentIdKey(parentIdKey).setWeightKey(weightKey)
                    .setNameKey(nameKey).setChildrenKey(childrenKey);
        }
        return nodeConfig;
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
