package com.haolin.haotool.util.tree;

import cn.hutool.core.lang.tree.Tree;

import java.util.List;
import java.util.function.Consumer;

public interface ITreeVO<T, K> {

    void setChildren(List<T> data);

    List<T> getChildren();

    void covertData(Tree<K> treeNode);

    /**
     * 实例K 转换成 TreeNode<K> 对象
     */
    TreeNode<K> covertTreeNode();

    /**
     * 实例K 转换成 TreeNode<K> 对象
     *    额外【数据】处理
     */
    default Consumer<TreeNode<K>> covertTreeNodeExtraConsumer() {
        return v -> v.addExtra("", null); // 默认实现
    }
}
