package com.cyp.nanningworkloadreportsystem.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树形结构工具类
 * 用于将平铺列表数据转换为树形结构
 * 适用于组织架构（org_unit）和用工项目字典（work_item）等树形数据
 */
public class TreeUtil {

    /**
     * 树节点接口，所有需要转换为树的实体需要实现此接口
     *
     * @param <T> 节点ID类型（通常为Long）
     */
    public interface TreeNode<T> {
        /** 获取当前节点ID */
        T getId();
        /** 获取父节点ID */
        T getParentId();
        /** 获取子节点列表 */
        List<? extends TreeNode<T>> getChildren();
        /** 设置子节点列表 */
        void setChildren(List<? extends TreeNode<T>> children);
    }

    /**
     * 将平铺列表转换为树形结构
     * 算法思路：
     * 1. 根据 parentId 进行分组
     * 2. 从根节点（parentId=0 或 parentId=null）开始递归组装
     *
     * @param list      平铺的节点列表
     * @param <T>       节点类型
     * @param <ID>      节点ID类型
     * @return 树形结构的根节点列表
     */
    public static <T extends TreeNode<ID>, ID> List<T> buildTree(List<T> list, ID rootParentId) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 按 parentId 分组
        Map<ID, List<T>> parentIdMap = list.stream()
                .filter(node -> node.getParentId() != null)
                .collect(Collectors.groupingBy(TreeNode::getParentId));

        // 获取根节点
        List<T> roots = parentIdMap.getOrDefault(rootParentId, new ArrayList<>());

        // 递归设置子节点
        for (T root : roots) {
            setChildrenRecursive(root, parentIdMap);
        }

        return roots;
    }

    /**
     * 递归为节点设置子节点
     *
     * @param node        当前节点
     * @param parentIdMap 按parentId分组的Map
     * @param <T>         节点类型
     * @param <ID>        节点ID类型
     */
    private static <T extends TreeNode<ID>, ID> void setChildrenRecursive(T node, Map<ID, List<T>> parentIdMap) {
        List<T> children = parentIdMap.get(node.getId());
        if (children != null && !children.isEmpty()) {
            node.setChildren(new ArrayList<>(children));
            for (T child : children) {
                setChildrenRecursive(child, parentIdMap);
            }
        }
    }
}
