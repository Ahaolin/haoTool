package com.haolin.haotool.util.tree;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.parser.NodeParser;
import com.haolin.haotool.extension.URL;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 注意： 无父数据  默认为 最大节点
 *  只要是{@param collect} 中的数据,全部会添加到树化节点中
 *
 *  注意有些额外的配置:{@link UnknownNodeTreeBuilder#customTreeNodeConfig(com.haolin.haotool.extension.URL)}
 * @author Ahaolin
 */
public class UnknownNodeTreeBuilder implements TreeBuilder {


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
        if (!(parser instanceof NonDataNodeParser)) {
            parser = new NonDataNodeParser<>(parser);
        }
        TreeNodeConfig nodeConfig = customTreeNodeConfig(url);
        for (TreeNode<M> obj : collect) {
            Tree<M> treeNode = new Tree<>(nodeConfig);
            parser.parse(obj, treeNode);
        }
        return covertData(parentId, clazz, ((NonDataNodeParser<TreeNode<M>, M>) parser).getNonParentData());
    }

    /**
     * 定制化字段设置
     */
    private TreeNodeConfig customTreeNodeConfig(URL url) {
        TreeNodeConfig nodeConfig = TreeNodeConfig.DEFAULT_CONFIG;
        if (url.getParameter("tree.enabled", true)) {
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


    /**
     * 递归的转化 为 clazz类型的集合
     * @param curParentId 指定parrentId
     * @param clazz 需要转换的类型
     * @param nonParentData  树的全量数据  key为id
     */
    private static <N extends ITreeVO<N, M>, M> List<N> covertData(M curParentId, Class<N> clazz,
                                                                   Map<M, Tree<M>> nonParentData) {
        List<N> results = new ArrayList<>();
        if (CollectionUtil.isEmpty(nonParentData)) return Collections.emptyList();
        // rebuildTree
        // nonParent可能存在一些【半成品树(缺失根节点)】 需要重新构造一遍父子关系
        TreeSet<Tree<M>> rootTrees = new TreeSet<>(Tree::compareTo); // 完整树节点的集合
//        TreeSet<Tree<M>> nonParentTrees = new TreeSet<>(Tree::compareTo); // 【半成品树(缺失某一个父节点)】
        Set<M> removeKeys = new HashSet<>();

        nonParentData.forEach((k, v) -> {
            M parentId = v.getParentId();
            Tree<M> parentTree;
            if (isRootNode(curParentId, parentId)) { //  完整树节点的集合（一般不可能）
                rootTrees.add(v);
                removeKeys.add(k);
            } else if ((parentTree = nonParentData.get(parentId)) != null) { // 【半成品树(缺失某一个父节点)】
                List<Tree<M>> nodes = Optional.ofNullable(parentTree.getChildren()).orElse(new ArrayList<>());
                parentTree.setChildren(nodes);
                nodes.add(v);
                nodes.sort(Tree::compareTo);
//                if (!isRootNode(curParentId, parentTree.getParentId())){
//                    nonParentTrees.add(parentTree); // 非根节点 添加
//                }
                removeKeys.add(k);
            }
            // 【非树无父】节点 留到最后
        });
        /**
         * 半成品树
         */
        if (CollectionUtil.isNotEmpty(removeKeys)){
            // 判断半成品树是否需要移除  注意  添加的时候
//            nonParentTrees.removeIf( v-> removeKeys.contains(v.getParentId()));
            removeKeys.forEach(nonParentData::remove);
        }

        // 先【部分树】Add至【完整树】 （走一遍排序的代码）
        if (CollUtil.isNotEmpty(nonParentData)) rootTrees.addAll(nonParentData.values());
        if (CollUtil.isNotEmpty(rootTrees)) TreeUtil.buildChildren(rootTrees, results, clazz);
        return results;

        // cusNote 跟上述代码相比：存在 一部分数据 在rootTree, 一部分在nonParentData  这两部分的数据没有排序
        // cusNote 注释代码： 存在 非根节点为啥需要添加（查询某一个节点，为啥需要比较该节点的父节点 是否有父  没有加入根）
        // 先Add 【完整树】、【部分树】
        /*if (CollectionUtil.isNotEmpty(rootTrees)) TreeUtil.buildChildren(rootTrees, results,clazz);
//        if (CollectionUtil.isNotEmpty(nonParentTrees)) {
//            TreeUtil.buildChildren(nonParentTrees, results,clazz);
//            nonParentTrees.forEach(v->nonParentData.remove(v.getId()));
//        }
        if (CollectionUtil.isNotEmpty(nonParentData)) TreeUtil.buildChildren(nonParentData.values(), results,clazz);
        return results;*/
    }

    private static <M> boolean isRootNode(M curParentId, M parentId) {
        return parentId == null || Objects.equals(parentId, curParentId);
    }

    /**
     * 树节点解析器 。无父数据  默认为 最大节点
     *
     * <pre class="code" >
     *     new TreeUtil.NonDataNodeParser<>(TreeUtil.DEFAULT_STRING_NODE_PARSER)
     * </pre>

     * @param <T>
     * @param <E>
     */
    @Getter
    @Setter
    private static class NonDataNodeParser<T,E> implements NodeParser<T,E>{

        /**
         * 有parentId 但是没有 上级信息的集合
         */
        private Map<E, Tree<E>> nonParentData;

        private NodeParser<T,E> proxyParser;

        /**
         * @param object   源数据实体
         * @param treeNode 树节点实体
         */
        @Override
        public void parse(T object, Tree<E> treeNode) {
            proxyParser.parse(object, treeNode);
            // 全部丢进去
            nonParentData.put(treeNode.getId(), treeNode);
        }

        public NonDataNodeParser(NodeParser<T, E> proxyParser) {
            this.proxyParser = proxyParser;
            nonParentData = new HashMap<>();
        }
    }
}
