package com.haolin.haotool.util.tree.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.parser.NodeParser;
import com.haolin.haotool.extension.URL;
import com.haolin.haotool.util.tree.ITreeVO;
import com.haolin.haotool.util.tree.TreeBuilder;
import com.haolin.haotool.util.tree.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * hutool 树化工具类 封装实现
 * 注意： 该方法从 parentId 开始,至上而下, 中间没有或者未知的父类节点 会被抛弃。
 *
 * @author Ahaolin
 */
public class HutoolTreeBuilder implements TreeBuilder {

    /**
     * @param collect  需要树化的数据需要先转化为该结构数据
     * @param parentId 首个父id
     * @param clazz    返回数据的类型
     * @param parser   collect中的数据转换为Tree<M>节点的实现  {@link TreeBuilder#DEFAULT_STRING_NODE_PARSER}
     * @param <M>      TreeNode的id类型
     * @param <N>      返回的集合  需要继承{@link ITreeVO}。 Tree转换为 N的逻辑在 {@link ITreeVO#restoreData(Tree)}
     *                 <p>
     *                 返回树化结构数据
     */
    @Override
    public <M, N extends ITreeVO<N, M>> List<N> covertTree(URL url, List<TreeNode<M>> collect, M parentId,
                                                           Class<N> clazz, NodeParser<TreeNode<M>, M> parser) {
        if (CollUtil.isEmpty(collect)) return Collections.emptyList();
        TreeNodeConfig nodeConfig = TreeUtil.customTreeNodeConfig(url);
        final List<Tree<M>> build = cn.hutool.core.lang.tree.TreeUtil.build(collect, parentId, nodeConfig, parser);
        return covertData(build, clazz);
    }

    /**
     * 递归的转化 为 clazz类型的集合
     * @param build 需要转换前的原始数据 。 如果build的 父id有，但是找不到 父对象，移除
     * @param clazz 需要转换的类型
     */
    private <M, N extends ITreeVO<N, M>> List<N> covertData(List<Tree<M>> build, Class<N> clazz) {
        if (CollectionUtil.isEmpty(build)) return Collections.emptyList();
        List<N> results = new ArrayList<>();
        TreeUtil.buildChildren(build, results, clazz);
        return results;
    }
}