package com.haolin.haotool.util.tree;

import cn.hutool.core.lang.Dict;

import java.util.function.Consumer;

public class TreeNode<T> extends cn.hutool.core.lang.tree.TreeNode<T> {

    private Dict extra;


    public TreeNode(T id, T parentId, String name, Comparable<?> weight, Consumer<TreeNode<T>> extraConsumer) {
        super(id, parentId, name, weight);
        extraConsumer.accept(this);
    }


    @Override
    public Dict getExtra(){
        return extra;
    }

    public TreeNode<T> addExtra(String key, Object obj){
        if (extra == null) extra = Dict.create();
        this.extra.set(key,obj);
        return this;
    }

    public Object getExtra(String key) {
        if (extra == null) return null;
        return extra.get(key);
    }

    public <M> M getExtra(String key, Class<M> clazz) {
        if (extra == null) return null;
        return (M) extra.get(key);
    }

}
