package com.base.spring.utils;

import com.base.spring.domain.RoleEntity;
import com.base.spring.domain.TreeEntity;
import com.base.spring.vo.ZTreeNodeBean;

import java.util.List;

/**
 * Description : TODO(TreeNodeEntity -> ZTreeJsonNode 转换工具，注意非事物状态下，递归方法，返回值的写法)
 * Description : TODO(ztree 本身不提供节点排序功能，按照返回的节点顺序排序，所以需要待转换的 TreeNodeEntity 事先排好序)
 * User: h819
 * Date: 2016/2/3
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
public class ZTreeUtils {


    private ZTreeUtils() {
    }

    /**
     * 把 TreeNodeEntity 类型转换为 ZTreeJsonNode 类型
     *
     * @param zTreeNode 待转换的对象
     * @return
     */
    public static ZTreeNodeBean getJsonData(TreeEntity zTreeNode, RoleEntity roleEntity) {

        ZTreeNodeBean parentTemp = new ZTreeNodeBean(0, "name", "url", false, false, false); //创建一个临时的变量，用于保存转换结果。
        convertTreeNodeToZTreeNode(zTreeNode, parentTemp, roleEntity);
        return parentTemp.getChildren().get(0); // parentTemp 只有一个子元素，为待转换的 treeNode
    }

    /**
     * 把 TreeNodeEntity 类型转换为 ZTreeJsonNode 类型，并返回其子节点
     *
     * @param treeNode 待转换的对象
     * @return
     */
    public static List<ZTreeNodeBean> getJsonDataChildren(TreeEntity treeNode, RoleEntity roleEntity) {
        return getJsonData(treeNode, roleEntity).getChildren();
    }


    /**
     * 根据已有的节点(该节点可以有子节点)，创建一个相同的新的节点（可以递归创建参考节点的左右子节点），作为 parent 的子节点
     * -
     * 最终的结果是，parent 中新生成了一个新的和 currentNode 一样的子节点。parent 参数需要外部提供。
     * <p>
     * 该方法不能象 getJsonData() 方法一样，通过临时创建的父对象返回，因为即使是临时创建 new ，在事务中也会创建表记录。
     *
     * @param copyNode 已有节点，是数据库里一条数据，可以有子节点。在数据库中创建一个新的记录，如果有子节点，可以递归创建。在事务环境中，new 操作会自动创建表记录
     * @param parent   新创建的节点的父节点
     */
    public static void createCopyNode(TreeEntity parent, TreeEntity copyNode) {

        //生成 copyNode 节点本身，new 操作会在数据库中对应一条新的记录
        TreeEntity newTree = new TreeEntity(copyNode.getType(), copyNode.getName(), copyNode.getIndex(), parent.isParentNode(), parent);
        newTree.setUrl(copyNode.getUrl());
        newTree.setCss(copyNode.getCss());
        parent.addChildToLastIndex(newTree); // 添加到所有子节点的尾部
        parent.setParentNode(true);

        //生成 currentNode 节点的子节点
        if (!copyNode.getChildren().isEmpty()) {
            for (TreeEntity child : copyNode.getChildren())
                createCopyNode(newTree, child); // 递归
        }

    }

    /**
     * 把 TreeNodeEntity 类型转换为 ZTreeNode 类型，把其值用 parent 带回
     *
     * @param zTreeJsonNodeParentTemp 通过此参数，保存转换后的结果。因为利用了递归，可以通过参数传回转换后的结果
     * @param treeNodeEntity          待转换的对象
     */
    private static void convertTreeNodeToZTreeNode(TreeEntity treeNodeEntity, ZTreeNodeBean zTreeJsonNodeParentTemp, RoleEntity roleEntity) {
        if (treeNodeEntity == null || zTreeJsonNodeParentTemp == null)
            return;

        long id = treeNodeEntity.getId();
//        long pId = 0;
//        if (!treeNodeEntity.isRoot())
//            pId = treeNodeEntity.getParent().getId();

        //  System.out.println(String.format("%d",pId));

        String name = treeNodeEntity.getName();
        String url = treeNodeEntity.getUrl();
        boolean open = false;
        boolean checked = false;

        //是否展开有子节点的父节点
        if (!treeNodeEntity.getChildren().isEmpty())
            open = true;

        //是否属于指定的 RoleEntity
        if (roleEntity != null)
            if (treeNodeEntity.getRoles().contains(roleEntity))
                checked = true;

        ZTreeNodeBean zTreeNode = new ZTreeNodeBean(id, name, url, open, treeNodeEntity.isParentNode(), checked);

        zTreeJsonNodeParentTemp.addChild(zTreeNode);

        if (!treeNodeEntity.getChildren().isEmpty())
            for (TreeEntity child : treeNodeEntity.getChildren())
                convertTreeNodeToZTreeNode(child, zTreeNode, roleEntity);

    }


}
