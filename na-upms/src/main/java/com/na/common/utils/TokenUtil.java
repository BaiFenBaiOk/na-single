package com.na.common.utils;

import com.na.auth.utils.AuthUtil;
import com.na.common.constant.CommonConst;
import com.na.common.constant.IGlobalConst;
import com.na.common.dto.MyAuthDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 令牌/登录用户上下文工具类
 * <p>
 * 数据来源:
 * <ul>
 *     <li>用户身份: Spring Security 上下文(SecurityContextHolder), principal 必须为 {@link MyAuthDto}</li>
 *     <li>原始令牌: 当前请求 Header 中的 JWT(前缀如 "Bearer ")</li>
 * </ul>
 * 注意: 依赖 RequestContextHolder, 仅在 Web 请求线程内可用;
 * 异步线程(@Async)/定时任务/启动阶段调用会取不到用户与请求信息
 */
@UtilityClass
public class TokenUtil {

    /**
     * 获取当前登录用户
     *
     * @return 当前用户; 未登录/匿名/非请求线程时返回 null
     */
    public static MyAuthDto getUser() {
        return getCurrentUser();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID; 未登录时返回 null
     */
    public static Long userId() {
        MyAuthDto user = getUser();
        return user == null ? null : Long.valueOf(user.getId());
    }

    /**
     * 获取当前用户所属部门ID(多个部门时取最小ID作为主部门)
     *
     * @return 部门ID; 未登录或无部门时返回 null
     */
    public static Long deptId() {
        MyAuthDto user = getUser();
        Set<String> deptIds = user == null ? null : user.getDeptIds();
        if (CollectionUtils.isEmpty(deptIds)) {
            return null;
        }
        return deptIds.stream()
                .map(Long::valueOf)
                .min(Long::compareTo)
                .orElse(null);
    }

    /**
     * 判断当前用户是否为系统默认超管身份
     *
     * @return 是返回 true; 未登录或普通用户返回 false
     */
    public static boolean isDefAuth() {
        MyAuthDto model = getCurrentUser();
        return model != null && IGlobalConst.SECURITY.isSysDef(model.getUserType());
    }

    /**
     * 提取当前请求中的原始 JWT(去掉 "Bearer " 前缀)
     * <p>原实现在请求头缺失时会因 frontToken 返回 null 直接 NPE, 已修复
     *
     * @return 纯令牌字符串; 请求头缺失或非请求线程时返回 null
     */
    public static String token() {
        String authHeader = frontToken();
        if (StringUtils.isBlank(authHeader)) {
            return null;
        }
        // ^ 锚定只去头部前缀, \\s* 兼容 "Bearer xxx"/"Bearer  xxx"(多空格); Pattern.quote 防前缀含正则元字符
        return authHeader.replaceFirst("^" + Pattern.quote(IGlobalConst.SECURITY.JWT_TOKEN_PREFIX) + "\\s*", "");
    }

    /**
     * 读取令牌请求头(安全版: 不用 requireNonNull 抛无上下文的 NPE)
     */
    private static String frontToken() {
        HttpServletRequest request = getRequest();
        return request == null ? null
                : StringUtils.trimToNull(request.getHeader(IGlobalConst.SECURITY.HEADER_STRING));
    }

    /**
     * 校验当前请求 URI 是否在用户已授权的菜单权限集合内
     * <p>权限 URI 集合来源: 登录时写入 principal.mapParams 的 USER_MAP_PARAMS_URIS
     *
     * @return 在授权集合内返回 true; 无请求/无权限数据/URI 为空返回 false
     */
    public static boolean menuAll() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return false;
        }
        Set<String> requestUriSet = requestUriSet();
        if (CollectionUtils.isEmpty(requestUriSet)) {
            return false;
        }
        String requestURI = request.getRequestURI();
        return StringUtils.isNotEmpty(requestURI) && AuthUtil.checkUri(requestUriSet, requestURI);
    }

    /**
     * 安全获取 HttpServletRequest
     */
    public HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    /**
     * 从当前用户上下文提取授权 URI 集合(防御式拷贝, 避免外部修改污染 principal 内数据)
     */
    @SuppressWarnings("unchecked")
    public static Set<String> requestUriSet() {
        MyAuthDto user = getUser();
        if (user == null || user.getMapParams() == null) {
            return new HashSet<>();
        }
        Object obj = user.getMapParams().get(CommonConst.USER_MAP_PARAMS_URIS);
        if (obj instanceof Set) {
            return new HashSet<>((Set<String>) obj);
        }
        return new HashSet<>();
    }

    /**
     * 从 Spring Security 上下文解析当前用户
     * <p>过滤三种无效身份: 未认证 / anonymousUser(匿名) / principal 非 MyAuthDto 类型
     */
    private static MyAuthDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        // "anonymousUser" 是 Spring Security 匿名身份固定的 principal 字符串
        if ("anonymousUser".equals(principal)) {
            return null;
        }
        return principal instanceof MyAuthDto ? (MyAuthDto) principal : null;
    }
}
