package com.haolin.haotool.util.tree;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.parser.NodeParser;
import com.haolin.haotool.extension.Adaptive;
import com.haolin.haotool.extension.SPI;
import com.haolin.haotool.extension.URL;

import java.util.List;

/**
 * 构建树的方法
 */
@SPI("hutool")
public interface TreeBuilder {

    /**
     * @param collect  需要树化的数据需要先转化为该结构数据
     * @param parentId 首个父id
     * @param clazz    返回数据的类型
     * @param parser   collect中的数据转换为Tree<M>节点的实现  {@link TreeBuilder#DEFAULT_STRING_NODE_PARSER}
     * @param <M>      TreeNode的id类型
     * @param <N>      返回的集合  需要继承{@link ITreeVO}。 Tree转换为 N的逻辑在 {@link ITreeVO#covertData(Tree)}
     *                 <p>
     *                 返回树化结构数据
     */
    @Adaptive({"tree.build", "protocol"})
    <M, N extends ITreeVO<N, M>> List<N> covertTree(URL url, List<TreeNode<M>> collect, M parentId,
                                                    Class<N> clazz, NodeParser<TreeNode<M>, M> parser);

    /**
     * 默认类型的数据
     */
    NodeParser<TreeNode<String>, String> DEFAULT_STRING_NODE_PARSER = (obj, treeNode) -> {
        treeNode.setId(obj.getId());
        treeNode.setParentId(obj.getParentId());
        treeNode.setWeight(obj.getWeight());
        treeNode.setName(obj.getName());
        // 扩展
        final Dict extra = obj.getExtra();
        if (extra != null)
            extra.forEach(treeNode::putExtra);
    };
}
