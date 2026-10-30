package com.na.admin.service.impl;

import com.na.admin.controller.vo.req.sysrole.ReqSysRolePagingVO;
import com.na.admin.controller.vo.req.sysrole.ReqSysRoleSaveVO;
import com.na.admin.controller.vo.req.sysrole.ReqSysRoleUpdateVO;
import com.na.admin.controller.vo.res.sysrole.ResSysRolePagingVO;
import com.na.admin.service.ISysRoleService;
import com.na.common.constant.IGlobalConst;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.base.BasePagesDomain;
import com.na.common.result.enums.Status;
import com.na.common.utils.ObjConversionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.na.common.utils.TokenUtil;
import com.na.core.repo.ISysMapRoleMenuRepo;
import com.na.core.repo.ISysMapRoleUserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.na.core.repo.ISysRoleRepo;
import com.na.model.SysRoleModel;
import com.na.params.SysRoleParams;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * sys_role Service实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysRoleServiceImpl implements ISysRoleService {

    private final ISysRoleRepo sysRoleRepo;
    private final ISysMapRoleUserRepo sysMapRoleUserRepo;
    private final ISysMapRoleMenuRepo sysMapRoleMenuRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveObj(ReqSysRoleSaveVO vo) {
        // 非系统用户不得创建「全部数据权限」角色，避免绕过角色-菜单绑定拿到全量菜单
        if (!TokenUtil.isDefAuth()
                && IGlobalConst.SECURITY.SYS_MENU_DATA_SCOPE.ALL.equals(vo.getDataScope())) {
            return R.error(Status.USER_NO_ACCESS);
        }
        SysRoleModel sysRoleModel = SysRoleModel.builder()
                .roleCode(vo.getRoleCode())
                .build();
        SysRoleModel model = sysRoleRepo.queryRoleByRoleObj(sysRoleModel);
        if (!Objects.isNull(model)) {
            return R.error(Status.ROLE_ALREADY_EXIST);
        }

        sysRoleModel.setRoleName(vo.getRoleName());
        sysRoleModel.setRoleSort(Objects.isNull(vo.getRoleSort()) ? 0 : vo.getRoleSort());
        sysRoleModel.setDataScope(Objects.isNull(vo.getDataScope())
                ? IGlobalConst.SECURITY.SYS_MENU_DATA_SCOPE.CUSTOM
                : vo.getDataScope());
        sysRoleModel.setStatus(Objects.isNull(vo.getStatus())
                ? IGlobalConst.SECURITY.STATUS.NORMAL
                : vo.getStatus());
        // 接口创建的角色一律为普通角色；00 只允许由初始化脚本或 DBA 维护
        sysRoleModel.setRoleType(IGlobalConst.SECURITY.SYS_TYPE.ORDINARY);
        sysRoleModel.setIsDel(false);
        sysRoleModel.setCreateTime(LocalDateTime.now());

        sysRoleRepo.saveObj(sysRoleModel);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateObj(ReqSysRoleUpdateVO vo) {
        SysRoleModel roleModel = sysRoleRepo.queryRoleById(vo.getId());
        if (Objects.isNull(roleModel)) {
            return R.error(Status.ROLE_NOT_EXIST);
        }
        if (IGlobalConst.SECURITY.isSysDef(roleModel.getRoleType())) {
            return R.error(Status.SYS_DEF_DATA_BAN_OPT);
        }

        roleModel.setRoleName(vo.getRoleName());
        roleModel.setUpdateTime(LocalDateTime.now());
        roleModel.setStatus(vo.getStatus());
        roleModel.setIsDel(vo.getIsDel());
        sysRoleRepo.updateObj(roleModel);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteObj(Long id) {
        SysRoleModel roleModel = sysRoleRepo.queryRoleById(id);
        if (Objects.isNull(roleModel) || Boolean.TRUE.equals(roleModel.getIsDel())) {
            return R.error(Status.ROLE_NOT_EXIST);
        }
        // 00 系统角色为内置保护数据，任何操作者都不可删除
        if (IGlobalConst.SECURITY.isSysDef(roleModel.getRoleType())) {
            return R.error(Status.SYS_DEF_DATA_BAN_OPT);
        }
        // 有用户绑定时禁止删除
        if (sysMapRoleUserRepo.countByRoleId(id) > 0) {
            return R.error("请先解除用户与该角色的绑定关系，再删除角色");
        }
        roleModel.setIsDel(true);
        roleModel.setUpdateTime(LocalDateTime.now());
        sysRoleRepo.updateObj(roleModel);
        // 同步清理角色菜单绑定，避免授权关系成为孤儿数据
        sysMapRoleMenuRepo.deleteMapRoleMenuByRoleId(id);
        return R.successNotMsg();
    }

    @Override
    public Result queryPaging(ReqSysRolePagingVO pagingVO) {
        SysRoleParams params = SysRoleParams.builder()
                .roleName(pagingVO.getRoleName())
                .roleCode(pagingVO.getRoleCode())
                .status(pagingVO.getStatus())
                .isDel(pagingVO.getIsDel())
                .build();
        params.setCurrentPage(pagingVO.getCurrentPage());
        params.setPageSize(pagingVO.getPageSize());
        IPage<SysRoleModel> sysRoleModelIPage = sysRoleRepo.queryPaging(params);
        return R.successNotMsg(BasePagesDomain.pages(sysRoleModelIPage,
                ObjConversionUtil.copyPropertiesList(sysRoleModelIPage.getRecords(), ResSysRolePagingVO.class)));
    }

}
