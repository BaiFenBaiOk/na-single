package com.na.admin.utils;

import com.na.admin.controller.vo.res.ResMenuTreeVO;
import com.na.common.constant.IGlobalConst;
import com.na.common.utils.TreeNode;
import com.na.model.SysMenuModel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pg
 * @date 2025年11月12日
 */
public class MenuUtil {
    /**
     * 递归查找所有下级菜单id（包括自己）
     */
    public static Set<Long> getAllChildrenIds(List<SysMenuModel> allMenus, Set<Long> rootIds) {
        Set<Long> result = new HashSet<>(rootIds);

        // 建立 parentId -> children 映射，提升查找效率
        Map<Long, List<SysMenuModel>> childrenMap = allMenus.stream()
                .filter(menu -> menu.getParentId() != null)
                .collect(Collectors.groupingBy(SysMenuModel::getParentId));

        // 队列遍历（非递归）
        Queue<Long> queue = new LinkedList<>(rootIds);
        while (!queue.isEmpty()) {
            Long parentId = queue.poll();
            List<SysMenuModel> children = childrenMap.get(parentId);
            if (children != null) {
                for (SysMenuModel child : children) {
                    if (result.add(child.getId())) {
                        queue.offer(child.getId());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 根据菜单ID集合，递归获取所有上级菜单ID（包含自身），直到 parentId = 0L
     *
     * @param menuIds   菜单ID集合（Set）
     * @param allMenus  所有菜单列表
     * @return 所有上级ID集合（去重）
     */
    public static Set<Long> getAllParentIds(Set<Long> menuIds, List<SysMenuModel> allMenus) {
        if (menuIds == null || menuIds.isEmpty() || allMenus == null || allMenus.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Long> result = new LinkedHashSet<>(menuIds); // 先放入自身

        Deque<Long> stack = new ArrayDeque<>(menuIds);

        while (!stack.isEmpty()) {
            Long currentId = stack.pop();

            SysMenuModel current = allMenus.stream()
                    .filter(m -> Objects.equals(m.getId(), currentId))
                    .findFirst()
                    .orElse(null);

            if (current != null && current.getParentId() != null && !IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID.equals(current.getParentId())) {
                Long parentId = current.getParentId();
                // 如果结果集中还没有父级，就加入并继续追溯
                if (result.add(parentId)) {
                    stack.push(parentId);
                }
            }
        }

        return result;
    }

    /**
     * 根据菜单ID集合，递归获取所有上级菜单（包含自身），直到 parentId = 0L
     *
     * @param menuIds  菜单ID集合（Set）
     * @param allMenus 所有菜单列表（建议一次性查询）
     * @return 所有上级菜单对象列表（去重）
     */
    public static List<SysMenuModel> getAllParentMenus(Set<Long> menuIds, List<SysMenuModel> allMenus) {
        if (menuIds == null || menuIds.isEmpty() || allMenus == null || allMenus.isEmpty()) {
            return Collections.emptyList();
        }

        // 用 Set 防止重复
        Set<Long> resultIds = new LinkedHashSet<>(menuIds);
        Deque<Long> stack = new ArrayDeque<>(menuIds);

        while (!stack.isEmpty()) {
            Long currentId = stack.pop();

            SysMenuModel current = allMenus.stream()
                    .filter(m -> Objects.equals(m.getId(), currentId))
                    .findFirst()
                    .orElse(null);

            if (current != null && current.getParentId() != null && !IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID.equals(current.getParentId())) {
                Long parentId = current.getParentId();
                // 如果没处理过，就加入结果并继续向上
                if (resultIds.add(parentId)) {
                    stack.push(parentId);
                }
            }
        }

        // 按结果 ID 集合顺序返回菜单对象列表
        return allMenus.stream()
                .filter(m -> resultIds.contains(m.getId()))
                .sorted(Comparator.comparing(SysMenuModel::getId)) // 可选：按id排序
                .collect(Collectors.toList());
    }

    /**
     * 找到给定菜单树中包含指定菜单ID的所有路径
     *
     * @param menuTree 菜单树
     * @param menuIds  目标菜单ID集合
     * @return 包含目标菜单ID的路径集合
     */
    public static Set<List<Long>> findMenuIdsArr(List<ResMenuTreeVO> menuTree,
                                                 Set<Long> menuIds) {
        Set<List<Long>> result = new HashSet<>();  // 用于存储结果路径的集合

        // 遍历菜单树的每个节点
        for (ResMenuTreeVO menu : menuTree) {
            if (menu != null) {
                // 查找包含目标菜单ID的路径
                findPaths(menu, new ArrayList<>(), menuIds, result);
            }
        }

        return result;
    }

    /**
     * 递归查找包含指定菜单ID的路径
     *
     * @param node       当前节点
     * @param currentPath 当前路径
     * @param menuIds    目标菜单ID集合
     * @param result     存储结果路径的集合
     */
    private static void findPaths(ResMenuTreeVO node, List<Long> currentPath, Set<Long> menuIds, Set<List<Long>> result) {
        if (node == null || node.getId() == null) {
            return;  // 如果当前节点或节点ID为空，则返回
        }

        currentPath.add(node.getId());  // 将当前节点ID添加到当前路径中

        // 如果当前节点ID在目标菜单ID集合中，则将当前路径添加到结果集合中
        if (menuIds.contains(node.getId())) {
            result.add(new ArrayList<>(currentPath));
        }

        // 遍历当前节点的子节点
        if (node.getChildren() != null) {
            for (ResMenuTreeVO child : node.getChildren()) {
                // 递归查找子节点
                findPaths(child, currentPath, menuIds, result);
            }
        }

        currentPath.remove(currentPath.size() - 1);  // 从当前路径中移除当前节点ID，以便处理其他路径
    }

    //    扁平化树
    public static List<ResMenuTreeVO> flatten(List<ResMenuTreeVO> tree) {
        List<ResMenuTreeVO> result = new ArrayList<>();
        for (ResMenuTreeVO node : tree) {
            result.add(node);
            if (node.getChildren() != null) {
                result.addAll(flatten(node.getChildren()));
            }
        }
        return result;
    }

    public static <T extends TreeNode<ID>, ID> List<T> toTree(List<T> list, ID rootParentId) {
        List<T> rootList = list.stream()
                .filter(node -> Objects.equals(rootParentId, node.getParentId()))
                .collect(Collectors.toList());

        List<T> subList = list.stream()
                .filter(node -> !Objects.equals(rootParentId, node.getParentId()))
                .collect(Collectors.toList());

        rootList.forEach(root -> findChild(root, subList));
        return rootList;
    }


    private static <T extends TreeNode> void findChild(T root, List<T> subList) {
        // 通过根节点去id和子节点的parentId是否相等，如果相等的话，代表是当前根的子集
        List<T> childrenList = subList.stream().filter(org -> org.getParentId().equals(root.getId()))
                .collect(Collectors.toList());

        // 如果当前有子集，初始化并放回去
        if (!childrenList.isEmpty()) {
            root.setChildren(childrenList);
            // 再次递归构建
            childrenList.forEach(org -> findChild(org, subList));
        } else {
            root.setChildren(new ArrayList<>());
        }
    }
}
