package com.haolin.haotool.util.tree;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.tree.Tree;
import com.alibaba.fastjson.JSON;
import com.haolin.haotool.extension.ExtensionLoader;
import com.haolin.haotool.extension.URL;
import com.haolin.haotool.util.tree.support.TreeUtil;
import com.haolin.haotool.util.tree.support.TreeUtilBackUp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TreeBuilderTest {
    @Test
    public void testJson() {
        SysOrgDeptTreeDTO root = new SysOrgDeptTreeDTO(1L, null, "根节点", null, 1, true, 1);
        SysOrgDeptTreeDTO c1 = new SysOrgDeptTreeDTO(10L, 1L, "A", null, 2, true, 10);
        SysOrgDeptTreeDTO c2 = new SysOrgDeptTreeDTO(11L, 1L, "B", null, 2, false, 12);
        SysOrgDeptTreeDTO c3 = new SysOrgDeptTreeDTO(12L, 1L, "C", null, 2, true, 5);
        // 无父节点
        SysOrgDeptTreeDTO data = new SysOrgDeptTreeDTO(999L, 9999L, "未知子节点", null, 4, true, 1);

        List<SysOrgDeptTreeDTO> treeDTOS = Arrays.asList(root, c1, c2, c3, data);
        System.out.println(JSON.toJSONString(treeDTOS));
    }


    @Test
    @SuppressWarnings("all")
    @Deprecated
    public void testBuild(){
        List<TreeNode<String>> treeNodes = getTreeNodes();
        if (treeNodes == null) return /*Collections.emptyList()*/;

        List<SysOrgDeptTreeDTO> treeDto = TreeUtilBackUp.covertTree(treeNodes, "0", SysOrgDeptTreeDTO.class,
                TreeUtilBackUp.wrapperNonDataNodeParser(TreeUtilBackUp.DEFAULT_STRING_NODE_PARSER));

        Assertions.assertNotNull(treeDto);
    }

    /**
     * 测试 树是否满足 json文件定义的数据
     */
    @Test
    @SuppressWarnings("all")
    public void testBuildExt() {
        List<TreeNode<String>> treeNodes = getTreeNodes();

        // 测试hutool
        URL url = new URL(null, null, 80);
        // ***测试添加额外参数（使得 Tree对象中key值改变）*********************
        url = url.addParameter("tree.enabled", true).addParameter("tree.nameKey","orgName");
        // **************************************************************
        List<SysOrgDeptTreeDTO> treeDto = ExtensionLoader.getExtensionLoader(TreeBuilder.class).getAdaptiveExtension()
                .covertTree(url, treeNodes, "0", SysOrgDeptTreeDTO.class, TreeBuilder.DEFAULT_STRING_NODE_PARSER);
        Assertions.assertNotNull(treeDto);
        Assertions.assertEquals(1, treeDto.size());

        // 测试 nonParentNode
        url = url.setProtocol("unknown");
        System.out.println(url.toFullString());

        treeDto = ExtensionLoader.getExtensionLoader(TreeBuilder.class).getAdaptiveExtension()
                .covertTree(url, treeNodes, "0", SysOrgDeptTreeDTO.class, TreeBuilder.DEFAULT_STRING_NODE_PARSER);
        Assertions.assertNotNull(treeDto);
        Assertions.assertEquals(2, treeDto.size());
        Assertions.assertEquals("根节点", treeDto.get(0).getName());
        Assertions.assertEquals("未知子节点", treeDto.get(1).getName());
    }



    @SuppressWarnings("all")
    private List<TreeNode<String>> getTreeNodes() {
        // 从本地的文件读取
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("json/treeData.json");
        String json = IoUtil.read(stream, StandardCharsets.UTF_8);
//        List<SysOrgDeptTreeDTO> data= JSON.parseObject(json, new TypeReference<List<SysOrgDeptTreeDTO>>() {});
        List<SysOrgDeptTreeDTO> data= JSON.parseArray(json, SysOrgDeptTreeDTO.class);

        if (CollectionUtil.isEmpty(data)) return Collections.emptyList();

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

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeBuilderTest.class);

    @Test
    public void testReflect(){
        StopWatch watch = new StopWatch();

        watch.start("holder");
        Tree<String> tree = new Tree<>();
        for (long i = 0; i < 1000000000L; i++) {
            TreeUtil.reflectGetResult(SysOrgDeptTreeDTO.class, tree);
        }
        watch.stop();
        LOGGER.info("=== holder name execute 【{}】ms", watch.getTotalTimeMillis());

        watch.start("reflect");
        for (long i = 0; i < 1000000000L; i++) {
            try {
                Constructor<SysOrgDeptTreeDTO> constructor = SysOrgDeptTreeDTO.class.getConstructor();
                constructor.newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        watch.stop();
        LOGGER.info("=== reflect name execute 【{}】ms", watch.getTotalTimeMillis());


        LOGGER.info("{}",watch.prettyPrint());

    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class SysOrgDeptTreeDTO implements ITreeVO<SysOrgDeptTreeDTO,String>,Comparable<SysOrgDeptTreeDTO>, Serializable {
    private Long id; // id 不可为0
    private Long parentId;
    private String name;

    @JsonIgnore
    private transient List<SysOrgDeptTreeDTO> children;

    //extra prop
    private Integer level;
    private Boolean orgFlag;
    private Integer orderNum;

    @Override
    public void restoreData(Tree<String> treeNode) {
        this.setId(Long.valueOf(treeNode.getId()));
        this.setParentId( "0".equals(treeNode.getParentId()) ? null : Long.valueOf(treeNode.getParentId()));
        this.setName((String) treeNode.getName());

        this.setLevel((Integer) treeNode.get("level"));
        this.setOrgFlag((Boolean) treeNode.get("orgFlag"));
        this.setOrderNum((Integer) treeNode.get("orderNum"));
    }

    @Override
    @SuppressWarnings("all")
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
        return node -> node.addExtra("level", this.getLevel())
                .addExtra("orgFlag", this.getOrgFlag())
                .addExtra("orderNum", this.getOrderNum());
    }

    @Override
    public int compareTo(SysOrgDeptTreeDTO o) {
        // 机构在前 其次级别  最后是orderNum
        int compare = CompareUtil.compare(this.orgFlag, o.orgFlag);
        if (compare != 0) return compare;
        compare = CompareUtil.compare(this.level, o.level);
        if (compare != 0) return compare;
        return CompareUtil.compare(this.orderNum, o.orderNum);
    }

}


