package com.na.admin.service.impl;

import com.na.admin.controller.vo.req.ReqLoginVO;
import com.na.admin.controller.vo.req.sysuser.ReqSysUserPagingVO;
import com.na.admin.controller.vo.req.sysuser.ReqSysUserSaveVO;
import com.na.admin.controller.vo.req.sysuser.ReqSysUserUpdateVO;
import com.na.admin.controller.vo.req.sysuser.ReqVerifyPasswordVO;
import com.na.common.dto.ResLoginVO;
import com.na.admin.controller.vo.res.sysrole.ResUserRoleVO;
import com.na.admin.controller.vo.res.sysuser.ReqBindDeptVO;
import com.na.admin.controller.vo.res.sysuser.ReqBindPostVO;
import com.na.admin.controller.vo.res.sysuser.ReqBindRoleVO;
import com.na.admin.controller.vo.res.sysuser.ResSysUserPagingVO;
import com.na.admin.controller.vo.res.sysuser.ResUserInfoVO;
import com.na.admin.service.ISysUserService;
import com.na.auth.AutoAuthConfig;
import com.na.common.base.BasePagesDomain;
import com.na.common.constant.CacheConstInterFace;
import com.na.common.constant.IGlobalConst;
import com.na.common.dto.MyAuthDto;
import com.na.common.enums.MyStatus;
import com.na.common.exceptions.BusinessException;
import com.na.common.login.LoginSessionService;
import com.na.common.result.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.common.utils.*;
import com.na.core.repo.ISysDeptRepo;
import com.na.core.repo.ISysMapRoleUserRepo;
import com.na.core.repo.ISysMapUserDeptRepo;
import com.na.core.repo.ISysMapUserPostRepo;
import com.na.core.repo.ISysPostRepo;
import com.na.core.repo.ISysRoleRepo;
import com.na.entity.SysMapRoleUserEntity;
import com.na.entity.SysMapUserDeptEntity;
import com.na.entity.SysUserEntity;
import com.na.model.SysDeptModel;
import com.na.model.SysMapRoleUserModel;
import com.na.model.SysMapUserDeptModel;
import com.na.model.SysMapUserPostModel;
import com.na.model.SysPostModel;
import com.na.model.SysRoleModel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.na.core.repo.ISysUserRepo;
import com.na.model.SysUserModel;
import com.na.params.SysUserParams;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * sys_user Service实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl implements ISysUserService {

    private final ISysUserRepo sysUserRepo;
    private final CacheTemplateUtil cacheTemplateUtil;
    private final LoginSessionService loginSessionService;
    private final AutoAuthConfig autoAuthConfig;
    private final ISysRoleRepo sysRoleRepo;
    private final ISysMapRoleUserRepo sysMapRoleUserRepo;
    private final ISysDeptRepo sysDeptRepo;
    private final ISysPostRepo sysPostRepo;
    private final ISysMapUserDeptRepo sysMapUserDeptRepo;
    private final ISysMapUserPostRepo sysMapUserPostRepo;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveObj(ReqSysUserSaveVO vo) {
        // 接口创建的用户一律为普通用户；00 只允许由初始化脚本或 DBA 维护
        vo.setUserType(IGlobalConst.SECURITY.SYS_TYPE.ORDINARY);
        if (!RequestParamsValidUtil.isAlphaNumeric(vo.getUserName())
                || !RequestParamsValidUtil.isValidLettersAndNumbersAndSymbols(vo.getPassWord())) {
            return R.error(Status.PARAMS_FORMAT_ERROR);
        }
        if (StringUtils.isNotEmpty(vo.getEmail()) && !RequestParamsValidUtil.isValidEmail(vo.getEmail())) {
            return R.error(Status.PARAMS_FORMAT_ERROR);
        }
        SysUserModel userModel = sysUserRepo.queryUserByUserName(vo.getUserName());
        if (!Objects.isNull(userModel)) {
            return R.error(Status.USER_ALREADY_EXIST);
        }
        if (StringUtils.isNotEmpty(vo.getPhone())) {
            userModel = sysUserRepo.queryUserByPhone(vo.getPhone());
            if (!Objects.isNull(userModel)) {
                return R.error(Status.USER_ALREADY_EXIST);
            }
        }
        SysUserModel model = ObjConversionUtil.copyProperties(vo, SysUserModel.class);
        model = sysUserRepo.saveObj(model);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateObj(ReqSysUserUpdateVO vo) {
        // 编辑时同样禁止通过接口把普通用户提升为 00
        vo.setUserType(IGlobalConst.SECURITY.SYS_TYPE.ORDINARY);
        if(StringUtils.isNotEmpty(vo.getPassWord()) && !RequestParamsValidUtil.isValidLettersAndNumbersAndSymbols(vo.getPassWord())){
            return R.error(Status.PARAMS_FORMAT_ERROR);
        }
        if (StringUtils.isNotEmpty(vo.getEmail()) && !RequestParamsValidUtil.isValidEmail(vo.getEmail())) {
            return R.error(Status.PARAMS_FORMAT_ERROR);
        }

        SysUserModel sysUserModel = sysUserRepo.queryUserByUserId(vo.getId());
        if (Objects.isNull(sysUserModel)) {
            return R.error(Status.USER_NOT_EXIST);
        }
        if (IGlobalConst.SECURITY.isSysDef(sysUserModel.getUserType())) {
            return R.error(Status.SYS_DEF_DATA_BAN_OPT);
        }
        SysUserModel model = ObjConversionUtil.copyProperties(vo, SysUserModel.class);
        sysUserRepo.updateObj(model);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteObj(Long id) {
        SysUserModel userModel = sysUserRepo.queryUserByUserId(id);
        if (Objects.isNull(userModel) || Boolean.TRUE.equals(userModel.getIsDel())) {
            return R.error(Status.USER_NOT_EXIST);
        }
        // 00 系统用户为内置保护数据，任何操作者都不可删除
        if (IGlobalConst.SECURITY.isSysDef(userModel.getUserType())) {
            return R.error(Status.SYS_DEF_DATA_BAN_OPT);
        }
        // 不允许删除当前登录账号
        if (Objects.equals(id, TokenUtil.userId())) {
            return R.error("不能删除当前登录的账号");
        }
        // 删除用户前必须先解除角色绑定（sys_map_role_user）
        if (sysMapRoleUserRepo.countByUserId(id) > 0) {
            return R.error("请先解除用户与角色的关系，再删除用户");
        }
        SysUserModel model = new SysUserModel();
        model.setId(id);
        model.setIsDel(true);
        sysUserRepo.updateObj(model);
        // 部门 / 岗位仅作为用户标签，随用户一并清理
        sysMapUserDeptRepo.deleteByUserId(id);
        sysMapUserPostRepo.deleteByUserId(id);
        // 删除后立即失效该用户的全部登录态
        loginSessionService.removeAll(id);
        return R.successNotMsg();
    }

    @Override
    public Result queryPaging(ReqSysUserPagingVO pagingVO) {
        SysUserParams params = SysUserParams.builder()
                .userName(pagingVO.getUserName())
                .nickName(pagingVO.getNickName())
                .status(pagingVO.getStatus())
                .isDel(pagingVO.getIsDel())
                .build();
        params.setCurrentPage(pagingVO.getCurrentPage());
        params.setPageSize(pagingVO.getPageSize());
        IPage<SysUserModel> sysUserModelIPage = sysUserRepo.queryPaging(params);
        List<ResSysUserPagingVO> resSysUserPagingVOS = ObjConversionUtil.copyPropertiesList(sysUserModelIPage.getRecords(), ResSysUserPagingVO.class);
        resSysUserPagingVOS.forEach( e->{
            // 在线 token：单点模式返回当前唯一 token，多点模式返回全部会话
            e.setTokens(loginSessionService.getOnlineTokens(e.getId()));
            List<SysRoleModel> roleModels = sysRoleRepo.queryRolesByUserId(e.getId());
            List<ResUserRoleVO> roleVOS = new ArrayList<>();
            for (SysRoleModel model1 : roleModels) {
                roleVOS.add(ResUserRoleVO.builder()
                        .id(model1.getId())
                        .roleName(model1.getRoleName())
                        .roleCode(model1.getRoleCode())
                        .build());
            }
            e.setRoles(roleVOS);
        });
        return R.successNotMsg(BasePagesDomain.pages(sysUserModelIPage, resSysUserPagingVOS));
    }

    @Override
    public Result login(ReqLoginVO vo, HttpServletRequest request) {
        String redisKey = CacheConstInterFace.CAPTCHA.KEY + vo.getUuid();
        String cache = cacheTemplateUtil.getCache(redisKey, String.class);
        if (StringUtils.isEmpty(cache) || !cache.equalsIgnoreCase(vo.getCaptcha())) {
            return R.error(MyStatus.CAPTCHA_ERROR);
        }
        cacheTemplateUtil.clear(redisKey);
        SysUserModel sysUserModel = sysUserRepo.queryUserByUserNameOrPhone(vo.getUserName());
        if (sysUserModel == null) {
            return R.error(Status.USER_NOT_EXIST);
        }

        if (IGlobalConst.SECURITY.STATUS.DISABLED.equals(sysUserModel.getStatus())
                || Boolean.TRUE.equals(sysUserModel.getIsDel())) {
            return R.error(Status.USER_NOT_ENABLE);
        }
        //密码比对
//        String md5 = EncryptionUtil.getMd5(vo.getPassWord());
        if (!vo.getPassWord().equals(sysUserModel.getPassWord())) {
            return R.error("密码错误");
        }
        return R.successNotMsg(setLogin(sysUserModel));
    }

    @Override
    public Result loginOut() {
        try {
            Long userId = TokenUtil.userId();
            String token = TokenUtil.token();
            // 清理该用户的所有登录会话
            loginSessionService.removeAll(userId);
            // 清理当前 token 的索引；其他终端的索引会随 TTL 自然过期
            loginSessionService.removeTokenIndex(token);
        } catch (Exception e) {
            System.out.println(e);
        }

        return R.successNotMsg();
    }

    @Override
    public Result verifyPassword(ReqVerifyPasswordVO vo) {
        Long userId = TokenUtil.userId();
        if (userId == null) {
            return R.error(Status.USER_NOT_EXIST);
        }
        SysUserModel sysUserModel = sysUserRepo.queryUserByUserId(userId);
        if (sysUserModel == null || Boolean.TRUE.equals(sysUserModel.getIsDel())) {
            return R.error(Status.USER_NOT_EXIST);
        }
        // 与登录保持一致：当前项目密码为明文比对
        if (!vo.getPassWord().trim().equals(sysUserModel.getPassWord())) {
            return R.error("密码错误");
        }
        return R.successNotMsg();
    }

    @Override
    public Result getUserInfoByUserId(Long userId) {
        SysUserModel sysUserModel = sysUserRepo.queryUserByUserId(userId);
        if (Objects.isNull(sysUserModel)) {
            return R.error(Status.USER_NOT_EXIST);
        }
        List<SysRoleModel> models = sysRoleRepo.queryRolesByUserId(userId);
        Set<String> roleNames = models.stream().map(SysRoleModel::getRoleName).collect(Collectors.toSet());
        Set<Long> roleIds = models.stream().map(SysRoleModel::getId).collect(Collectors.toSet());
        // 用户绑定部门（单个）与岗位（多个），供个人中心展示
        List<SysMapUserDeptModel> userDeptModels = sysMapUserDeptRepo.queryByUserId(userId);
        List<SysMapUserPostModel> userPostModels = sysMapUserPostRepo.queryByUserId(userId);
        Set<String> deptNames = Optional.ofNullable(userDeptModels).orElse(Collections.emptyList()).stream()
                .map(SysMapUserDeptModel::getDeptName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> postNames = Optional.ofNullable(userPostModels).orElse(Collections.emptyList()).stream()
                .map(SysMapUserPostModel::getPostName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        ResUserInfoVO vo = ResUserInfoVO.builder()
                .id(sysUserModel.getId())
                .userName(sysUserModel.getUserName())
                .nickName(sysUserModel.getNickName())
                .email(sysUserModel.getEmail())
                .phone(sysUserModel.getPhone())
                .createTime(sysUserModel.getCreateTime())
                .status(sysUserModel.getStatus())
                .userType(sysUserModel.getUserType())
                .roleNames(roleNames)
                .roleIds(roleIds)
                .deptName(deptNames)
                .postName(postNames)
                .build();
        return R.successNotMsg(vo);
    }

    @Override
    public Result getBindRoleByUserId(Long userId) {
        MyAuthDto authDto = TokenUtil.getUser();
        if (IGlobalConst.SECURITY.SYS_TYPE.ORDINARY.equals(authDto.getUserType())) {
            throw new BusinessException(Status.USER_NO_ACCESS, null);
        }
        List<SysRoleModel> sysRoleModels = sysRoleRepo.queryRolesByUserId(userId);
        // 判空，null转空集合，避免NPE
        sysRoleModels = Optional.ofNullable(sysRoleModels).orElse(Collections.emptyList());

        // 只转换一次，过滤掉id为null
        Set<Long> roleIds = sysRoleModels.stream()
                .map(SysRoleModel::getId)
                .filter(Objects::nonNull) // 过滤null id
                .collect(Collectors.toSet());
        return R.successNotMsg(roleIds);
    }

    @Override
    public Result bindRole(ReqBindRoleVO vo) {
        MyAuthDto authDto = TokenUtil.getUser();
        if (IGlobalConst.SECURITY.SYS_TYPE.ORDINARY.equals(authDto.getUserType())) {
            throw new BusinessException(Status.USER_NO_ACCESS, null);
        }
        SysUserModel sysUserModel = sysUserRepo.queryUserByUserId(vo.getUserId());
        if (Objects.isNull(sysUserModel)) {
            return R.error(Status.USER_NOT_EXIST);
        }
        if (IGlobalConst.SECURITY.isSysDef(sysUserModel.getUserType())) {
            return R.error(Status.SYS_DEF_DATA_BAN_OPT);
        }
        // 不允许修改自己的角色：一旦误清空会立刻失去全部权限，无法自行恢复
        if (Objects.equals(vo.getUserId(), TokenUtil.userId())) {
            return R.error("不能修改自己的角色");
        }

        transactionTemplate.executeWithoutResult(status -> {
            List<Long> roleIds = vo.getRoleIds();
            // 先校验角色都存在（校验不通过会回滚，不会误删原有绑定）
            if (CollectionUtils.isNotEmpty(roleIds)) {
                Set<Long> roleIdsDB = sysRoleRepo.queryRoleByRoleIds(roleIds).stream()
                        .filter(role -> !Boolean.TRUE.equals(role.getIsDel()))
                        .map(SysRoleModel::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!roleIdsDB.containsAll(roleIds)) {
                    throw new BusinessException(Status.PARAMS_NOT_VALID, null);
                }
            }
            // 清空该用户已有的角色绑定——放在条件外，入参为空集合即表示取消全部角色
            sysMapRoleUserRepo.deleteRoleByUserId(vo.getUserId());
            // 再按入参重新绑定
            if (CollectionUtils.isNotEmpty(roleIds)) {
                List<SysMapRoleUserModel> addModels = new ArrayList<>();
                for (Long id : roleIds) {
                    SysMapRoleUserModel model = new SysMapRoleUserModel();
                    model.setUserId(vo.getUserId());
                    model.setRoleId(id);
                    addModels.add(model);
                }
                sysMapRoleUserRepo.saveRoleUserMap(addModels);
            }
        });


        return R.successNotMsg();
    }

    @Override
    public Result getBindDeptByUserId(Long userId) {
        // 普通用户（03）不得查看他人绑定
        MyAuthDto authDto = TokenUtil.getUser();
        if (authDto == null
                || IGlobalConst.SECURITY.SYS_TYPE.ORDINARY.equals(authDto.getUserType())) {
            throw new BusinessException(Status.USER_NO_ACCESS, null);
        }
        SysUserModel sysUserModel = sysUserRepo.queryUserByUserId(userId);
        if (Objects.isNull(sysUserModel)) {
            return R.error(Status.USER_NOT_EXIST);
        }
        List<SysMapUserDeptModel> userDeptModels = sysMapUserDeptRepo.queryByUserId(userId);
        if (CollectionUtils.isEmpty(userDeptModels)) {
            // 无绑定返回 null，前端据此清空选中
            return R.successNotMsg(null);
        }
        return R.successNotMsg(userDeptModels.get(0).getDeptId());
    }

    @Override
    public Result bindDept(ReqBindDeptVO vo) {
        // 与绑定角色一致：普通用户（03）不得操作他人的绑定关系
        MyAuthDto authDto = TokenUtil.getUser();
        if (authDto == null
                || IGlobalConst.SECURITY.SYS_TYPE.ORDINARY.equals(authDto.getUserType())) {
            throw new BusinessException(Status.USER_NO_ACCESS, null);
        }
        SysUserModel sysUserModel = sysUserRepo.queryUserByUserId(vo.getUserId());
        if (Objects.isNull(sysUserModel)) {
            return R.error(Status.USER_NOT_EXIST);
        }
        if (IGlobalConst.SECURITY.isSysDef(sysUserModel.getUserType())) {
            return R.error(Status.SYS_DEF_DATA_BAN_OPT);
        }
        // deptId 传空 = 解除绑定
        if (Objects.isNull(vo.getDeptId())) {
            sysMapUserDeptRepo.deleteByUserId(vo.getUserId());
            return R.successNotMsg();
        }
        SysDeptModel sysDeptModel = sysDeptRepo.queryDeptById(vo.getDeptId());
        if (Objects.isNull(sysDeptModel) || Boolean.TRUE.equals(sysDeptModel.getIsDel())) {
            return R.error(Status.DEPT_NOT_EXIST);
        }
        transactionTemplate.executeWithoutResult(status -> {
            // 单部门绑定：先清空既有绑定，再绑定新部门
            sysMapUserDeptRepo.deleteByUserId(vo.getUserId());
            sysMapUserDeptRepo.saveObj(SysMapUserDeptModel.builder()
                    .userId(vo.getUserId())
                    .deptId(vo.getDeptId())
                    .build());
        });
        return R.successNotMsg();
    }

    @Override
    public Result getBindPostByUserId(Long userId) {
        // 普通用户（03）不得查看他人绑定
        MyAuthDto authDto = TokenUtil.getUser();
        if (authDto == null
                || IGlobalConst.SECURITY.SYS_TYPE.ORDINARY.equals(authDto.getUserType())) {
            throw new BusinessException(Status.USER_NO_ACCESS, null);
        }
        SysUserModel sysUserModel = sysUserRepo.queryUserByUserId(userId);
        if (Objects.isNull(sysUserModel)) {
            return R.error(Status.USER_NOT_EXIST);
        }
        List<SysMapUserPostModel> userPostModels = sysMapUserPostRepo.queryByUserId(userId);
        Set<Long> postIds = Optional.ofNullable(userPostModels).orElse(Collections.emptyList()).stream()
                .map(SysMapUserPostModel::getPostId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return R.successNotMsg(postIds);
    }

    @Override
    public Result bindPost(ReqBindPostVO vo) {
        // 与绑定角色一致：普通用户（03）不得操作他人的绑定关系
        MyAuthDto authDto = TokenUtil.getUser();
        if (authDto == null
                || IGlobalConst.SECURITY.SYS_TYPE.ORDINARY.equals(authDto.getUserType())) {
            throw new BusinessException(Status.USER_NO_ACCESS, null);
        }
        SysUserModel sysUserModel = sysUserRepo.queryUserByUserId(vo.getUserId());
        if (Objects.isNull(sysUserModel)) {
            return R.error(Status.USER_NOT_EXIST);
        }
        if (IGlobalConst.SECURITY.isSysDef(sysUserModel.getUserType())) {
            return R.error(Status.SYS_DEF_DATA_BAN_OPT);
        }
        // 去重，避免映射表（无唯一索引）出现重复行
        Set<Long> postIds = Optional.ofNullable(vo.getPostIds()).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        transactionTemplate.executeWithoutResult(status -> {
            // 先校验：岗位必须都存在且未删除（校验不通过会回滚，不会误删原有绑定）
            if (CollectionUtils.isNotEmpty(postIds)) {
                List<SysPostModel> postModels = sysPostRepo.queryPostsByIds(new ArrayList<>(postIds));
                Set<Long> postIdsDB = postModels.stream()
                        .map(SysPostModel::getId)
                        .collect(Collectors.toSet());
                if (!postIdsDB.containsAll(postIds)) {
                    throw new BusinessException(Status.PARAMS_NOT_VALID, null);
                }
            }
            // 清空既有绑定后重新绑定（入参为空数组 = 取消该用户全部岗位）
            sysMapUserPostRepo.deleteByUserId(vo.getUserId());
            if (CollectionUtils.isNotEmpty(postIds)) {
                List<SysMapUserPostModel> addModels = new ArrayList<>();
                for (Long postId : postIds) {
                    addModels.add(SysMapUserPostModel.builder()
                            .userId(vo.getUserId())
                            .postId(postId)
                            .build());
                }
                sysMapUserPostRepo.saveBatchObj(addModels);
            }
        });
        return R.successNotMsg();
    }

    @Override
    public Result clearToken(Long userId) {
        loginSessionService.removeAll(userId);
        return R.successNotMsg();
    }

    private ResLoginVO setLogin(SysUserModel sysUserModel) {
        long time = autoAuthConfig.getExpirationSeconds() * 1000L * 30 / 7; // 30天

        // 生成短 token（22 字符）；token 本身不携带业务信息，登录态以 Redis 为准
        String token = ShortTokenUtil.generate();

        String loginIp = AddressUtil.getIpAddress(null);
        LocalDateTime currentDateTime = LocalDateTime.now();

        sysUserModel.setLoginTime(currentDateTime);
        sysUserModel.setLoginIp(loginIp);

        sysUserRepo.updateObj(sysUserModel);

//        List<SysMenuModel> menuAll = sysMenuRepo.selectMenuByUserId(sysUserModel.getId());
//        Set<String> requestUriSet = menuAll.stream()
//                .filter(menu -> menu != null && CacheConstInterFace.MENU.SIGN_CODE.ALL.equals(menu.getSignCode()))
//                .map(SysMenuModel::getRequestUri)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());

        // 部门用于登录信息展示（单部门绑定，取第一个）
        List<SysMapUserDeptModel> userDeptModels = sysMapUserDeptRepo.queryByUserId(sysUserModel.getId());
        String deptName = CollectionUtils.isNotEmpty(userDeptModels) && userDeptModels.get(0).getDeptName() != null
                ? userDeptModels.get(0).getDeptName()
                : "";
        ResLoginVO resLoginVO = ResLoginVO.builder()
                .userId(sysUserModel.getId())
                .userName(sysUserModel.getUserName())
                .nickName(sysUserModel.getNickName())
                .userType(sysUserModel.getUserType())
                .token(token)
                .loginTime(currentDateTime)
                .loginIp(loginIp)
                .loginAddress(IpParseUtil.parse(loginIp, new int[]{2, 3, 4}))
                .email(sysUserModel.getEmail())
                .phone(sysUserModel.getPhone())
                .avatar(sysUserModel.getAvatar())
                .deptName(deptName)
                .build();
        // 统一保存登录态：写 token 索引 + 会话；单点模式由 na.login.single-token 控制
        loginSessionService.save(token, resLoginVO, time);

        return resLoginVO;
    }

}