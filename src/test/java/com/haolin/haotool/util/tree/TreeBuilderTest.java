package com.haolin.haotool.util.tree;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import com.haolin.haotool.extension.ExtensionLoader;
import com.haolin.haotool.extension.URL;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TreeBuilderTest {

    @Test
    public void testBuild(){
        List<TreeNode<String>> treeNodes = getTreeNodes();
        if (treeNodes == null) return /*Collections.emptyList()*/;

        List<SysOrgDeptTreeDTO> treeDto = TreeUtilBackUp.covertTree(treeNodes, "0", SysOrgDeptTreeDTO.class,
                TreeUtilBackUp.wrapperNonDataNodeParser(TreeUtilBackUp.DEFAULT_STRING_NODE_PARSER));

        Assertions.assertTrue(treeDto != null);
    }

    @Test
    public void testBuildExt(){
        List<TreeNode<String>> treeNodes = getTreeNodes();

        // 测试hutool
        URL url = new URL(null, null, 80);
        List<SysOrgDeptTreeDTO> treeDto = ExtensionLoader.getExtensionLoader(TreeBuilder.class).getAdaptiveExtension()
                .covertTree(url, treeNodes, "0", SysOrgDeptTreeDTO.class, TreeBuilder.DEFAULT_STRING_NODE_PARSER);
        Assertions.assertTrue(treeDto != null);


        // 测试 nonParentNode
        url = new URL("unknown", null, 80);
        url.addParameter("tree.enabled", true); // 测试添加额外参数
        treeDto = ExtensionLoader.getExtensionLoader(TreeBuilder.class).getAdaptiveExtension()
                .covertTree(url, treeNodes, "0", SysOrgDeptTreeDTO.class, TreeBuilder.DEFAULT_STRING_NODE_PARSER);
        Assertions.assertTrue(treeDto != null);
    }


    private List<TreeNode<String>> getTreeNodes() {
        List<SysOrgDeptTreeDTO> data = /*sysDeptMapper.getTreeDto(dept)*/ new ArrayList<>();
        if (CollectionUtil.isEmpty(data)) return null;

        // 1.写法
        List<TreeNode<String>> collect = data.stream().map(v -> new TreeNode<>(
                String.valueOf(v.getId()),
                Optional.ofNullable(v.getParentId()).map(String::valueOf).orElse("0"),
                v.getName(),
                (Comparable<?>) v,
                v.covertTreeNodeExtraConsumer())
        ).collect(Collectors.toList());
        // 2.写法
        collect = data.stream().map(SysOrgDeptTreeDTO::covertTreeNode).collect(Collectors.toList());
        return collect;
    }
}

@Data
class SysOrgDeptTreeDTO implements ITreeVO<SysOrgDeptTreeDTO,String> {
    private Long id;
    private Long parentId;
    private String name;
    private List<SysOrgDeptTreeDTO> children;

    //extra prop
    private Integer level;

    @Override
    public void covertData(Tree<String> treeNode) {
        this.setLevel((Integer) treeNode.get("level"));
    }

    @Override
    public TreeNode<String> covertTreeNode() {
        return new TreeNode<>(
                String.valueOf(getId()),
                Optional.ofNullable(getParentId()).map(String::valueOf).orElse("0"),
                getName(),
                (Comparable<?>) this,
                this.covertTreeNodeExtraConsumer());
    }

    @Override
    public Consumer<TreeNode<String>> covertTreeNodeExtraConsumer() {
        return node -> node.addExtra("level", this.getLevel());
    }

}
