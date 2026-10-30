package com.na.admin.service.impl;

import com.na.admin.controller.vo.req.sysmenu.ReqSysMenuPagingVO;
import com.na.admin.controller.vo.req.sysmenu.ReqSysMenuSaveVO;
import com.na.admin.controller.vo.req.sysmenu.ReqSysMenuUpdateVO;
import com.na.admin.controller.vo.res.ResMenuTreeVO;
import com.na.admin.controller.vo.res.sysmenu.*;
import com.na.admin.service.ISysMenuService;
import com.na.admin.utils.MenuUtil;
import com.na.common.constant.GlobalConst;
import com.na.common.constant.IGlobalConst;
import com.na.common.dto.MyAuthDto;
import com.na.common.exceptions.BusinessException;
import com.na.common.exceptions.UnauthorizedException;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.common.utils.TokenUtil;
import com.na.common.utils.TreeUtil;
import com.na.core.repo.*;
import com.na.entity.SysMenuEntity;
import com.na.model.*;
import com.na.params.QueryMenuParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * sys_menu Service实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysMenuServiceImpl implements ISysMenuService {

    private final ISysMenuRepo sysMenuRepo;
    private final TransactionTemplate transactionTemplate;
    private final ISysMapRoleMenuRepo sysMapRoleMenuRepo;
    private final ISysRoleRepo sysRoleRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveObj(ReqSysMenuSaveVO vo) {
        if (!TokenUtil.isDefAuth()) {
            return R.error(Status.USER_NO_ACCESS);
        }
        // 新增仅创建新记录，不涉及已有内置数据，故此处不做 sys_menu_type 校验：
        // 本方法不接受 sysMenuType 入参，新记录的该字段取数据库默认值（'03' 普通）。
        // 若需为系统菜单（'00'）补充内置项，同样由开发人员直接操作数据库，不走接口。
        // 菜单
        if (GlobalConst.TYPE_DIR.equals(vo.getMenuType())) {
            if (!Objects.isNull(vo.getParentId()) && !Objects.equals(IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID, vo.getParentId())) {
                return R.error("目录的上级只能为0");
            }
            if (StringUtils.isEmpty(vo.getPath())) {
                return R.error("路由地址path不能为空");
            }
        } else {
            if (Objects.equals(IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID, vo.getParentId())) {
                return R.error("子集的上级不能为0");
            }
            if (GlobalConst.TYPE_MENU.equals(vo.getMenuType())) {
                if (StringUtils.isEmpty(vo.getPath())) {
                    return R.error("路由地址path不能为空");
                }
                if (StringUtils.isEmpty(vo.getComponent())) {
                    return R.error("组件路径component不能为空");
                }
            }
            if (GlobalConst.TYPE_BUTTON.equals(vo.getMenuType())) {
                if (StringUtils.isEmpty(vo.getRequestUri())) {
                    return R.error("请求Api requestUri不能为空");
                }
                if (StringUtils.isEmpty(vo.getPerms())) {
                    return R.error("权限标识perms不能为空");
                }
            }
            if (GlobalConst.TYPE_API.equals(vo.getMenuType())) {
                if (StringUtils.isEmpty(vo.getRequestUri())) {
                    return R.error("请求Api requestUri不能为空");
                }
                if (StringUtils.isEmpty(vo.getPerms())) {
                    return R.error("权限标识perms不能为空");
                }
            }
        }
        // 父子类型链校验：目录 → 菜单 → 按钮 → 接口
        String typeError = validateMenuTypeChain(vo.getParentId(), vo.getMenuType());
        if (typeError != null) {
            return R.error(typeError);
        }
        SysMenuModel model = SysMenuModel.builder()
                .name(vo.getName())
                .menuName(vo.getMenuName())
                .parentId(vo.getParentId())
                .orderNum(vo.getOrderNum())
                .path(vo.getPath())
                .component(vo.getComponent())
                .requestUri(vo.getRequestUri())
                .perms(vo.getPerms())
                .query(vo.getQuery())
                .isFrame(vo.getIsFrame())
                .isCache(vo.getIsCache())
                .menuType(vo.getMenuType())
                .visible(vo.getVisible())
                .status(vo.getStatus())
                .icon(vo.getIcon())
                .remark(vo.getRemark())
                .build();
        sysMenuRepo.saveObj(model);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateObj(ReqSysMenuUpdateVO vo) {
        if (!TokenUtil.isDefAuth()) {
            return R.error(Status.USER_NO_ACCESS);
        }
        // ------------------------------------------------------------------
        // 【内置数据保护】sys_menu_type = '00' 的系统菜单不允许经接口修改。
        //
        // 前端已同步隐藏该行的操作入口，此处为服务端兜底校验，二者必须同时保留。
        // 若确需调整（例如修正路由地址或权限标识），由开发人员直接操作数据库变更，
        // 不要放宽这里的校验，也不要新增任何绕过入口。
        // ------------------------------------------------------------------
        List<SysMenuModel> models = sysMenuRepo.queryByIds(Collections.singletonList(vo.getId()));
        boolean hasSysDef = models.stream()
                .anyMatch(model -> IGlobalConst.SECURITY.SYS_TYPE.SYS_DEF.equals(model.getSysMenuType()));
        if (hasSysDef) {
            return R.error(Status.BAN_OPT);
        }
        // 菜单
        if (GlobalConst.TYPE_DIR.equals(vo.getMenuType())) {
            if (!Objects.isNull(vo.getParentId()) && !Objects.equals(IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID, vo.getParentId())) {
                return R.error("目录的上级只能为0");
            }
            if (StringUtils.isEmpty(vo.getPath())) {
                return R.error("路由地址path不能为空");
            }
        } else {
            if (Objects.equals(IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID, vo.getParentId())) {
                return R.error("子集的上级不能为0");
            }
            if (GlobalConst.TYPE_MENU.equals(vo.getMenuType())) {
                if (StringUtils.isEmpty(vo.getPath())) {
                    return R.error("路由地址path不能为空");
                }
                if (StringUtils.isEmpty(vo.getComponent())) {
                    return R.error("组件路径component不能为空");
                }
            }
            if (GlobalConst.TYPE_BUTTON.equals(vo.getMenuType())) {
                if (StringUtils.isEmpty(vo.getRequestUri())) {
                    return R.error("请求Api requestUri不能为空");
                }
                if (StringUtils.isEmpty(vo.getPerms())) {
                    return R.error("权限标识perms不能为空");
                }
            }
            if (GlobalConst.TYPE_API.equals(vo.getMenuType())) {
                if (StringUtils.isEmpty(vo.getRequestUri())) {
                    return R.error("请求Api requestUri不能为空");
                }
                if (StringUtils.isEmpty(vo.getPerms())) {
                    return R.error("权限标识perms不能为空");
                }
            }
        }
        // 菜单类型层级校验。仅在上级或类型发生变化时校验：
        // 存量数据可能不符合层级限制，若每次都校验，这类菜单连改名称都会被拦住。
        if (!Objects.isNull(vo.getParentId()) && CollectionUtils.isNotEmpty(models)) {
            SysMenuModel origin = models.get(0);
            boolean structureChanged = !Objects.equals(origin.getParentId(), vo.getParentId())
                    || !Objects.equals(origin.getMenuType(), vo.getMenuType());
            if (structureChanged) {
                String typeError = validateMenuTypeChain(vo.getParentId(), vo.getMenuType());
                if (typeError != null) {
                    return R.error(typeError);
                }
            }
        }
        SysMenuModel model = SysMenuModel.builder()
                .id(vo.getId())
                .name(vo.getName())
                .menuName(vo.getMenuName())
                .parentId(vo.getParentId())
                .orderNum(vo.getOrderNum())
                .path(vo.getPath())
                .component(vo.getComponent())
                .query(vo.getQuery())
                .isFrame(vo.getIsFrame())
                .isCache(vo.getIsCache())
                .menuType(vo.getMenuType())
                .visible(vo.getVisible())
                .status(vo.getStatus())
                .icon(vo.getIcon())
                .remark(vo.getRemark())
                .isDel(vo.getIsDel())
                .build();
        sysMenuRepo.updateObj(model);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteObj(Long id) {
        // 菜单为系统级资源，仅 00 系统用户可维护（与 saveObj / updateObj 一致）
        if (!TokenUtil.isDefAuth()) {
            return R.error(Status.USER_NO_ACCESS);
        }
        List<SysMenuModel> models = sysMenuRepo.queryByIds(Collections.singletonList(id));
        if (CollectionUtils.isEmpty(models) || Boolean.TRUE.equals(models.get(0).getIsDel())) {
            return R.error(Status.MENU_NOT_EXIST);
        }
        SysMenuModel menu = models.get(0);
        // 00 系统菜单为内置保护数据，任何操作者都不可删除
        if (IGlobalConst.SECURITY.SYS_TYPE.SYS_DEF.equals(menu.getSysMenuType())) {
            return R.error(Status.BAN_OPT);
        }
        // 存在下级菜单时禁止删除，避免菜单树断裂
        Long childCount = sysMenuRepo.lambdaQuery()
                .eq(SysMenuEntity::getParentId, id)
                .eq(SysMenuEntity::getIsDel, false)
                .count();
        if (childCount > 0) {
            return R.error("请先删除该菜单下的子菜单");
        }
        // 逻辑删除菜单
        SysMenuModel model = new SysMenuModel();
        model.setId(id);
        model.setIsDel(true);
        sysMenuRepo.updateObj(model);
        // 同步清理角色授权，避免回显出现已删除菜单
        sysMapRoleMenuRepo.deleteMapRoleMenuByMenuId(id);
        return R.successNotMsg();
    }

    @Override
    public Result queryPaging(ReqSysMenuPagingVO pagingVO) {
        List<SysMenuModel> menuAll = sysMenuRepo.queryMenuAll(QueryMenuParams.builder().build());
        if (CollectionUtils.isEmpty(menuAll)) {
            return R.successNotMsg(new ArrayList<>());
        }

        // 转换为 VO 列表（使用 Stream 映射）
        List<ResMenuTreeVO> list = menuAll.stream()
                .map(menu -> ResMenuTreeVO.builder()
                        .id(menu.getId())
                        .name(menu.getName())
                        .menuName(menu.getMenuName())
                        .parentId(menu.getParentId())
                        .orderNum(menu.getOrderNum())
                        .path(menu.getPath())
                        .component(menu.getComponent())
                        .requestUri(menu.getRequestUri())
                        .perms(menu.getPerms())
                        .query(menu.getQuery())
                        .isFrame(menu.getIsFrame())
                        .isCache(menu.getIsCache())
                        .menuType(menu.getMenuType())
                        .visible(menu.getVisible())
                        .status(menu.getStatus())
                        .icon(menu.getIcon())
                        .sysMenuType(menu.getSysMenuType())
                        .remark(menu.getRemark())
                        .build())
                .collect(Collectors.toList());

        // 转换成树结构
        List<ResMenuTreeVO> menuTree = TreeUtil.toTree(list, IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID);

        String keyword = pagingVO.getMenuName();
        if (StringUtils.isNotEmpty(keyword)) {
            // 1. 扁平化树
            List<ResMenuTreeVO> flatList = MenuUtil.flatten(menuTree);
            Map<Long, ResMenuTreeVO> idMap = flatList.stream()
                    .collect(Collectors.toMap(ResMenuTreeVO::getId, m -> m));

            // 2. 找出命中的节点
            List<ResMenuTreeVO> matchList = flatList.stream()
                    .filter(m -> m.getMenuName() != null && m.getMenuName().contains(keyword))
                    .collect(Collectors.toList());

            // 3. 找出所有命中节点的祖先链（包含 parentId=0）
            Set<ResMenuTreeVO> fullMatchedSet = new LinkedHashSet<>();
            for (ResMenuTreeVO node : matchList) {
                ResMenuTreeVO cur = node;
                while (cur != null) {
                    fullMatchedSet.add(cur);
                    cur = idMap.get(cur.getParentId());
                }
            }

            // 4. 构建排序后的扁平列表：先放命中链，再放其他
            List<ResMenuTreeVO> reordered = new ArrayList<>(fullMatchedSet);

            reordered.addAll(
                    flatList.stream()
                            .filter(m -> !fullMatchedSet.contains(m))
                            .collect(Collectors.toList())
            );

            // 5. 重新构建树
            menuTree = MenuUtil.toTree(reordered, IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID);

        }
        return R.successNotMsg(menuTree);
    }

    @Override
    public Result tree() {
        Long userId = TokenUtil.userId();
        if (Objects.isNull(userId)) {
            throw new UnauthorizedException("未登录", null);
        }
        List<SysMenuModel> menuList;
        if (TokenUtil.isDefAuth()) {
            menuList = sysMenuRepo.selectMenuTreeAll(new HashSet<>());
        } else {
            menuList = sysMenuRepo.selectMenuTreeByUserId(userId);
        }
        List<ResMenuTreeVO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(menuList)) {
            for (SysMenuModel menu : menuList) {
                ResMenuTreeVO vo = ResMenuTreeVO.builder()
                        .id(menu.getId())
                        .name(menu.getName())
                        .menuName(menu.getMenuName())
                        .parentId(menu.getParentId())
                        .orderNum(menu.getOrderNum())
                        .path(menu.getPath())
                        .component(menu.getComponent())
                        .requestUri(menu.getRequestUri())
                        .perms(menu.getPerms())
                        .query(menu.getQuery())
                        .isFrame(menu.getIsFrame())
                        .isCache(menu.getIsCache())
                        .menuType(menu.getMenuType())
                        .visible(menu.getVisible())
                        .status(menu.getStatus())
                        .icon(menu.getIcon())
                        .build();
                list.add(vo);
            }
        }

//        List<ResMenuTreeVO> result = list.stream()
//                .filter(item -> Objects.equals(IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID, item.getParentId())
//                        && !IGlobalConst.SECURITY.SYS_MENU_DATA_SCOPE.ALL.equals(item.getVisible()))
//                .collect(Collectors.toList());
//
//        List<ResMenuTreeVO> resultItem = list.stream()
//                .filter(item -> !IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID.equals(item.getParentId()))
//                .collect(Collectors.toList());
//        result.addAll(resultItem);
        List<ResMenuTreeVO> tree = TreeUtil.toTree(list, IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID);
        if (CollectionUtils.isEmpty(tree)) {
            return R.error("请联系管理员分配角色/分配菜单后再进行登录");
        }
        return R.successNotMsg(tree);
    }

    @Override
    public Result getMenuBtnByParentId(Long parentId) {
        List<SysMenuModel> sysMenuModels;
        if (TokenUtil.isDefAuth()) {
            sysMenuModels = sysMenuRepo.getMenuBtnByParentId(parentId);
        } else {
            sysMenuModels = sysMenuRepo.getMenuBtnByParentIdAndUserId(parentId, TokenUtil.userId());
        }
        List<ResMenuBtnVO> list = new ArrayList<>();
        sysMenuModels.forEach(menu -> {
            list.add(ResMenuBtnVO.builder()
                    .id(menu.getId())
                    .menuName(menu.getMenuName())
                    .perms(menu.getPerms())
                    .icon(menu.getIcon())
                    .build());
        });
        return R.successNotMsg(list);
    }

    @Override
    public Result roleMenuPaging(ReqRoleMenuVO pagingVO) {
        MyAuthDto authDto = TokenUtil.getUser();
        if (authDto == null
                || Objects.equals(IGlobalConst.SECURITY.SYS_TYPE.ORDINARY, authDto.getUserType())) {
            return R.error(Status.USER_NO_ACCESS);
        }

        // 全量菜单树（未删除），供角色勾选
        List<SysMenuModel> menuModels = sysMenuRepo.queryMenuAll(QueryMenuParams.builder().build());
        if (CollectionUtils.isEmpty(menuModels)) {
            return R.error(Status.MENU_NOT_EXIST);
        }

        List<ResMenuTreeVO> list = menuModels.stream()
                .map(menu -> ResMenuTreeVO.builder()
                        .id(menu.getId())
                        .name(menu.getName())
                        .menuName(menu.getMenuName())
                        .parentId(menu.getParentId())
                        .orderNum(menu.getOrderNum())
                        .path(menu.getPath())
                        .component(menu.getComponent())
                        .requestUri(menu.getRequestUri())
                        .perms(menu.getPerms())
                        .query(menu.getQuery())
                        .isFrame(menu.getIsFrame())
                        .isCache(menu.getIsCache())
                        .menuType(menu.getMenuType())
                        .visible(menu.getVisible())
                        .status(menu.getStatus())
                        .icon(menu.getIcon())
                        .sysMenuType(menu.getSysMenuType())
                        .remark(menu.getRemark())
                        .build())
                .collect(Collectors.toList());

        List<ResMenuTreeVO> menuTree = TreeUtil.toTree(list, IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID);
        Set<Long> treeIds = MenuUtil.flatten(menuTree).stream()
                .map(ResMenuTreeVO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 角色已授权的菜单 id，仅保留树中存在的（树外的无法回显）
        List<Long> menuIds = sysMapRoleMenuRepo.queryAllRoleMenuByRoleId(pagingVO.getRoleId()).stream()
                .map(SysMapRoleMenuModel::getMenuId)
                .filter(Objects::nonNull)
                .filter(treeIds::contains)
                .collect(Collectors.toList());

        Set<List<Long>> menuIdsArr = MenuUtil.findMenuIdsArr(menuTree, new HashSet<>(menuIds));
        Map<String, Object> result = new HashMap<>();
        result.put("menuTree", menuTree);
        result.put("menuIdsArr", menuIdsArr);
        result.put("menuIds", menuIds);
        return R.successNotMsg(result);
    }

    @Override
    public Result saveRoleMenu(ReqBindRoleMenuVO vo) {
        MyAuthDto authDto = TokenUtil.getUser();
        if (authDto == null
                || Objects.equals(IGlobalConst.SECURITY.SYS_TYPE.ORDINARY, authDto.getUserType())) {
            return R.error(Status.USER_NO_ACCESS);
        }
        Long roleId = vo.getRoleId();
        List<Long> menuIds = vo.getMenuIds();
        transactionTemplate.executeWithoutResult(status -> {
            // 角色必须存在且未删除
            SysRoleModel roleModel = sysRoleRepo.queryRoleById(roleId);
            if (roleModel == null || Boolean.TRUE.equals(roleModel.getIsDel())) {
                throw new BusinessException(Status.ROLE_NOT_EXIST, null);
            }
            // 菜单必须都存在且未删除
            if (CollectionUtils.isNotEmpty(menuIds)) {
                Set<Long> menuIdsDB = sysMenuRepo.queryMenuAll(QueryMenuParams.builder().build()).stream()
                        .map(SysMenuModel::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!menuIdsDB.containsAll(menuIds)) {
                    throw new BusinessException(Status.MENU_NOT_EXIST, null);
                }
            }
            // 删除该角色原有菜单绑定后重新写入
            sysMapRoleMenuRepo.deleteMapRoleMenuByRoleId(roleId);

            List<SysMapRoleMenuModel> list = new ArrayList<>();
            for (Long menuId : menuIds) {
                list.add(SysMapRoleMenuModel.builder()
                        .roleId(roleId)
                        .menuId(menuId)
                        .build());
            }
            if (CollectionUtils.isNotEmpty(list)) {
                sysMapRoleMenuRepo.saveBatchMapRoleMenu(list);
            }
        });
        return R.successNotMsg();
    }

    /**
     * 校验「上级 → 子级」的菜单类型层级关系。
     *
     * 规则：
     * 1. 接口（A）不受层级限制——挂在目录 / 菜单 / 按钮之下均可创建；
     * 2. 接口下不能再创建任何子级，根级（parentId 为空或 0）只能创建目录；
     * 3. 其余按层级：目录下可继续建目录或菜单，菜单下只能建按钮，按钮下只能建接口。
     *
     * 前端菜单表单的类型联动与此保持一致。
     *
     * @return null 表示校验通过，否则返回具体错误提示
     */
    private String validateMenuTypeChain(Long parentId, String childType) {
        boolean isRoot = Objects.isNull(parentId)
                || Objects.equals(IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID, parentId);
        List<SysMenuModel> parents = isRoot ? Collections.emptyList()
                : sysMenuRepo.queryByIds(Collections.singletonList(parentId));
        if (!isRoot && CollectionUtils.isEmpty(parents)) {
            return "上级菜单不存在";
        }
        String parentType = isRoot ? null : parents.get(0).getMenuType();

        // 接口下不能再建任何子级
        if (GlobalConst.TYPE_API.equals(parentType)) {
            return "接口下不能再创建子级";
        }
        // 根级只能创建目录
        if (isRoot) {
            return GlobalConst.TYPE_DIR.equals(childType) ? null : "根级只能创建目录";
        }
        // 接口不受层级限制：挂在非接口的上级之下即可
        if (GlobalConst.TYPE_API.equals(childType)) {
            return null;
        }
        if (GlobalConst.TYPE_DIR.equals(parentType)) {
            boolean allowed = GlobalConst.TYPE_MENU.equals(childType) || GlobalConst.TYPE_DIR.equals(childType);
            return allowed ? null : "目录下只能创建目录或菜单";
        }
        if (GlobalConst.TYPE_MENU.equals(parentType)) {
            return GlobalConst.TYPE_BUTTON.equals(childType) ? null : "菜单下只能创建按钮";
        }
        if (GlobalConst.TYPE_BUTTON.equals(parentType)) {
            return GlobalConst.TYPE_API.equals(childType) ? null : "按钮下只能创建接口";
        }
        return "菜单类型与上级不匹配";
    }

}