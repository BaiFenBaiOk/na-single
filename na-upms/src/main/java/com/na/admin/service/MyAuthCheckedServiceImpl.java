package com.na.admin.service;

import com.na.common.dto.ResLoginVO;
import com.na.auth.model.AuthSysRoleModel;
import com.na.auth.service.IAuthCheckedService;
import com.na.common.constant.CommonConst;
import com.na.common.constant.GlobalConst;
import com.na.common.constant.IGlobalConst;
import com.na.common.dto.MyAuthDto;
import com.na.common.login.LoginSessionService;
import com.na.common.utils.TokenUtil;
import com.na.core.repo.ISysMenuRepo;
import com.na.core.repo.ISysRoleRepo;
import com.na.core.repo.ISysUserRepo;
import com.na.model.SysMenuModel;
import com.na.model.SysRoleModel;
import com.na.model.SysUserModel;
import com.na.params.QueryMenuParams;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MyAuthCheckedServiceImpl implements IAuthCheckedService {

    @Autowired
    private LoginSessionService loginSessionService;
    @Autowired
    private ISysUserRepo sysUserRepo;
    @Autowired
    private ISysRoleRepo sysRoleRepo;
    @Autowired
    private ISysMenuRepo sysMenuRepo;

    @Override
    public String getToken(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = TokenUtil.token();
        if (authHeader != null) {
            return authHeader;
        }
        return "";
    }

    @Override
    public boolean handleTokenChecked(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    @Override
    public boolean setSimpleAuthUserModel(HttpServletRequest request, HttpServletResponse response) {
        String token = TokenUtil.token();
        if (StringUtils.isNotEmpty(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            ResLoginVO loginVO = loginSessionService.get(token);
            if (Objects.isNull(loginVO)) {
                return false;
            }
            Long userId = loginVO.getUserId();
            LocalDateTime now = LocalDateTime.now();
                SysUserModel user = sysUserRepo.queryUserByUserId(userId);
                if (user != null) {
                    user.setLoginTime(now);
                    sysUserRepo.updateObj(user);
                }

                List<SysRoleModel> roleModels = sysRoleRepo.queryRolesByUserId(userId);

                Boolean exists = roleModels.stream()
                        .anyMatch(role -> IGlobalConst.SECURITY.SYS_MENU_DATA_SCOPE.ALL.equals(role.getDataScope()));
                List<SysMenuModel> menuAll;
                if (exists || IGlobalConst.SECURITY.isSysDef(loginVO.getUserType())) {
                    QueryMenuParams params = QueryMenuParams.builder()
                            .menuTypes(new HashSet<>(Arrays.asList(
                                    GlobalConst.TYPE_DIR,
                                    GlobalConst.TYPE_MENU
                            )))
                            .build();
                    menuAll = sysMenuRepo.queryMenuAll(params);
                } else {
                    menuAll = sysMenuRepo.selectMenuTreeByUserId(userId);
                }

                Set<String> perms = menuAll.stream()
                        .map(SysMenuModel::getPerms)
                        .filter(perm -> perm != null && !perm.isEmpty())
                        .collect(Collectors.toSet());

                Set<String> requestUris = menuAll.stream()
                        .map(SysMenuModel::getRequestUri)
                        .filter(perm -> perm != null && !perm.isEmpty())
                        .collect(Collectors.toSet());

                List<AuthSysRoleModel> roles = roleModels.stream()
                        .map(roleModel -> AuthSysRoleModel.builder()
                                .id(String.valueOf(roleModel.getId()))
                                .roleName(roleModel.getRoleName())
                                .build())
                        .collect(Collectors.toList());

                Set<String> requestUriSet = loginVO.getRequestUriSet();
                Map<String,Object> mapParams = new HashMap<>();
                if(CollectionUtils.isNotEmpty(requestUriSet)){
                    mapParams = Map.of(CommonConst.USER_MAP_PARAMS_URIS,requestUriSet);
                }

                MyAuthDto authUserModel = new MyAuthDto();
                authUserModel.setId(String.valueOf(userId));
                authUserModel.setUserName(loginVO.getUserName());
                authUserModel.setPhone(loginVO.getPhone());
                authUserModel.setUserType(loginVO.getUserType());
                authUserModel.setRoles(roles);
                authUserModel.setPerms(perms);
                authUserModel.setRequestUris(requestUris);
                authUserModel.setMapParams(mapParams);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authUserModel, null, authUserModel.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        }
        return false;
    }

    @Override
    public void setAuthUserModel(HttpServletRequest request, HttpServletResponse response) {
        String token = TokenUtil.token();
        if (StringUtils.isNotEmpty(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            ResLoginVO loginVO = loginSessionService.get(token);
            if (!Objects.isNull(loginVO)) {
                Long userId = loginVO.getUserId();
                    List<SysRoleModel> roleModels = sysRoleRepo.queryRolesByUserId(userId);

                    Boolean exists = roleModels.stream()
                            .anyMatch(role -> IGlobalConst.SECURITY.SYS_MENU_DATA_SCOPE.ALL.equals(role.getDataScope()));
                    List<SysMenuModel> menuAll;
                    if (exists || IGlobalConst.SECURITY.isSysDef(loginVO.getUserType())) {
                        menuAll = sysMenuRepo.queryMenuAll();
                    } else {
                        menuAll = sysMenuRepo.queryMenuByUserId(userId);
                    }

                    Set<String> perms = menuAll.stream()
                            .map(SysMenuModel::getPerms)
                            .filter(perm -> perm != null && !perm.isEmpty())
                            .collect(Collectors.toSet());

                    Set<String> requestUris = menuAll.stream()
                            .map(SysMenuModel::getRequestUri)
                            .filter(perm -> perm != null && !perm.isEmpty())
                            .collect(Collectors.toSet());

                    List<AuthSysRoleModel> roles = roleModels.stream()
                            .map(roleModel -> AuthSysRoleModel.builder()
                                    .id(String.valueOf(roleModel.getId()))
                                    .roleName(roleModel.getRoleName())
                                    .build())
                            .collect(Collectors.toList());

                    Set<String> requestUriSet = loginVO.getRequestUriSet();
                    Map<String,Object> mapParams = new HashMap<>();
                    if(CollectionUtils.isNotEmpty(requestUriSet)){
                        mapParams = Map.of(CommonConst.USER_MAP_PARAMS_URIS,requestUriSet);
                    }
                    MyAuthDto authUserModel = new MyAuthDto();
                    authUserModel.setId(String.valueOf(userId));
                        authUserModel.setUserName(loginVO.getUserName());
                    authUserModel.setPhone(loginVO.getPhone());
                    authUserModel.setUserType(loginVO.getUserType());
                    authUserModel.setRoles(roles);
                    authUserModel.setPerms(perms);
                    authUserModel.setRequestUris(requestUris);
                    authUserModel.setMapParams(mapParams);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authUserModel, null, authUserModel.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
    }

    @Override
    public Set<String> getRequestApi(HttpServletRequest request) {
        return TokenUtil.requestUriSet();
    }
}
