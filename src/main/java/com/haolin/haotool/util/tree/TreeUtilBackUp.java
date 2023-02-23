package com.haolin.haotool.util.tree;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.parser.NodeParser;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.*;

/**
 * TreeUtil 原始实现。会存在问题。
 * <pre>
 *     1.非SPI实现
 *     2.
 * </pre>
 *
 * @see TreeBuilder
 */
@Deprecated
public class TreeUtilBackUp {

    public static void main(String[] args) {
//        final List<TreeNode<String>> treeNodes = new ArrayList<>();
//        final List<PrivilegeResult> data = new ArrayList<>();
//        final List<PrivilegeResult> ns = TreeUtil.covertTree(treeNodes, "0", PrivilegeResult.class, null);
    }

    /**
     * 默认类型的数据
     */
    public static final NodeParser<TreeNode<String>, String> DEFAULT_STRING_NODE_PARSER = (obj, treeNode) -> {
        treeNode.setId(obj.getId());
        treeNode.setParentId(obj.getParentId());
        treeNode.setWeight(obj.getWeight());
        treeNode.setName(obj.getName());
        // 扩展
        final Dict extra = obj.getExtra();
        if (extra != null)
            extra.forEach(treeNode::putExtra);
    };

    /**
     * 返回 特殊处理的 nodeParser
     * @see NonDataNodeParser
     */
    public static <T,E> NonDataNodeParser<T,E> wrapperNonDataNodeParser(NodeParser<T,E> nodeParser){
        return new NonDataNodeParser<T, E>(nodeParser);
    }


    /**
     * @param collect  需要树化的数据需要先转化为该结构数据
     * @param parentId 首个父id
     * @param clazz    返回数据的类型
     * @param parser   collect中的数据转换为Tree<M>节点的实现
     * @param <M>      TreeNode的id类型
     * @param <N>      返回的集合  需要继承{@link ITreeVO}。 Tree转换为 N的逻辑在 {@link ITreeVO#restoreData(Tree)}
     *                 <p>
     *                 返回树化结构数据
     */
    public static <M, N extends ITreeVO<N, M>> List<N> covertTree(List<TreeNode<M>> collect, M parentId, Class<N> clazz,
                                                                  NodeParser<TreeNode<M>, M> parser) {
        if (parser instanceof NonDataNodeParser) { // 是否添加 空父集合
            for (TreeNode<M> obj : collect) {
                Tree<M> treeNode = new Tree<>(TreeNodeConfig.DEFAULT_CONFIG);
                parser.parse(obj, treeNode);
            }
            return covertData(parentId, clazz, ((NonDataNodeParser<TreeNode<M>, M>) parser).getNonParentData());
        }
        final List<Tree<M>> build = cn.hutool.core.lang.tree.TreeUtil.build(collect, parentId, parser);
        return covertData(build, clazz);
    }


    /**
     * @param collect  需要树化的数据需要先转化为该结构数据
     * @param parentId 首个父id
     * @param clazz    返回数据的类型
     * @param <M>      TreeNode的id类型
     * @param <N>      返回的集合  需要继承{@link ITreeVO}。 Tree转换为 N的逻辑在 {@link ITreeVO#restoreData(Tree)}
     *                 <p>
     *                 返回树化结构数据
     */
    public static <M, N extends ITreeVO<N, M>> List<N> covertTree(List<TreeNode<M>> collect, M parentId, Class<N> clazz) {
        return covertTree(collect, parentId, clazz, (obj, treeNode) -> {
            treeNode.setId(obj.getId());
            treeNode.setParentId(obj.getParentId());
            treeNode.setWeight(obj.getWeight());
            treeNode.setName(obj.getName());
        });
    }

    /**
     * 递归的转化 为 clazz类型的集合
     * @param build 需要转换前的原始数据 。 如果build的 父id有，但是找不到 父对象，移除
     * @param clazz 需要转换的类型
     */
    private static <M, N extends ITreeVO<N, M>> List<N> covertData(List<Tree<M>> build, Class<N> clazz) {
        if (CollectionUtil.isEmpty(build)) return Collections.emptyList();
        List<N> results = new ArrayList<>();
        buildChildren(build, results, clazz);
        return results;
    }

    /**
     * 递归的转化 为 clazz类型的集合
     * @param curParentId 指定parrentId
     * @param clazz 需要转换的类型
     * @param nonParentData  树的全量数据  key为id
     */
    private static <N extends ITreeVO<N, M>, M> List<N> covertData(M curParentId, Class<N> clazz, Map<M, Tree<M>> nonParentData) {
        List<N> results = new ArrayList<>();
        if (CollectionUtil.isEmpty(nonParentData)) return Collections.emptyList();
        // rebuildTree
        // nonParent可能存在一些【半成品树(缺失根节点)】 需要重新构造一遍父子关系
        TreeSet<Tree<M>> rootTrees = new TreeSet<>(Tree::compareTo); // 完整树节点的集合
        TreeSet<Tree<M>> nonParentTrees = new TreeSet<>(Tree::compareTo); // 【半成品树(缺失某一个父节点)】
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
                if (!isRootNode(curParentId, parentTree.getParentId())){
                    nonParentTrees.add(parentTree); // 非根节点 添加
                }
                removeKeys.add(k);
            }
            // 【非树无父】节点 留到最后
        });
        /**
         * 半成品树
         */
        if (CollectionUtil.isNotEmpty(removeKeys)){
            // 判断半成品树是否需要移除  注意  添加的时候
            nonParentTrees.removeIf( v-> removeKeys.contains(v.getParentId()));
            removeKeys.forEach(nonParentData::remove);
        }
        // 先Add 【完整树】、【部分树】
        if (CollectionUtil.isNotEmpty(rootTrees)) buildChildren(rootTrees, results,clazz);
        if (CollectionUtil.isNotEmpty(nonParentTrees)) buildChildren(nonParentTrees, results,clazz);
        if (CollectionUtil.isNotEmpty(nonParentData)) buildChildren(nonParentData.values(), results,clazz);
        return results;
    }

    private static <M> boolean isRootNode(M curParentId, M parentId) {
        return parentId == null || Objects.equals(parentId, curParentId);
    }


    private static <M, N extends ITreeVO<N, M>> void buildChildren(Collection<Tree<M>> build, List<N> results, Class<N> clazz) {
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

    private static <N extends ITreeVO<N, M>, M> N getResult(Class<N> clazz, Tree<M> v) {
        try {
            return ReflectUtil.getConstructor(clazz).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    StrUtil.format("class {}的构造方法需要一个空构造", clazz.getName())
            );
        }
    }


    /**
     * 树节点解析器 。无父数据  默认为 最大节点
     *
     * <pre class="code" >
     *     new TreeUtil.NonDataNodeParser<>(TreeUtil.DEFAULT_STRING_NODE_PARSER)
     * </pre>

     * FIXME Aholin 可以使用代理模式优化
     * @param <T>
     * @param <E>
     */
    @Deprecated
    private static class NonDataNodeParser<T,E> implements NodeParser<T,E>{

        /**
         * 有parentId 但是没有 上级信息的集合
         */
        private Map<E,Tree<E>> nonParentData;

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

        public Map<E, Tree<E>> getNonParentData() {
            return nonParentData;
        }

        public void setNonParentData(Map<E, Tree<E>> nonParentData) {
            this.nonParentData = nonParentData;
        }

        public NodeParser<T, E> getProxyParser() {
            return proxyParser;
        }

        public void setProxyParser(NodeParser<T, E> proxyParser) {
            this.proxyParser = proxyParser;
        }
    }
}
