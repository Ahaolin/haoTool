package com.haolin.haotool.util.tree;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.parser.NodeParser;
import com.haolin.haotool.extension.URL;

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
        final List<Tree<M>> build = cn.hutool.core.lang.tree.TreeUtil.build(collect, parentId, parser);
        return TreeUtil.covertData(build, clazz);
    }
}