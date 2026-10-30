package com.na.common.websocket.util;

import com.na.common.dto.MyAuthDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * WebSocket 会话工具类。
 *
 * @author pg
 */
public final class WebSocketFrameworkUtils {

    /** 登录用户存放在 session attributes 中的键 */
    public static final String ATTRIBUTE_LOGIN_USER = "NA_WS_LOGIN_USER";

    /** 原始 token 存放在 session attributes 中的键 */
    public static final String ATTRIBUTE_TOKEN = "NA_WS_TOKEN";

    /** 上次 token 复核时间存放在 session attributes 中的键 */
    public static final String ATTRIBUTE_AUTH_CHECK_TIME = "NA_WS_AUTH_CHECK_TIME";

    private WebSocketFrameworkUtils() {
    }

    /**
     * 把登录用户写入握手 attributes
     */
    public static void setLoginUser(MyAuthDto loginUser, Map<String, Object> attributes) {
        attributes.put(ATTRIBUTE_LOGIN_USER, loginUser);
    }

    /**
     * 获取当前连接的登录用户
     */
    public static MyAuthDto getLoginUser(WebSocketSession session) {
        if (session == null) {
            return null;
        }
        Object user = session.getAttributes().get(ATTRIBUTE_LOGIN_USER);
        return user instanceof MyAuthDto ? (MyAuthDto) user : null;
    }

    /**
     * 获取当前连接的用户 ID
     */
    public static Long getLoginUserId(WebSocketSession session) {
        MyAuthDto user = getLoginUser(session);
        return user == null ? null : toLong(user.getId());
    }

    /**
     * 获取当前连接的用户类型（00 / 03）
     */
    public static String getLoginUserType(WebSocketSession session) {
        MyAuthDto user = getLoginUser(session);
        return user == null ? null : user.getUserType();
    }

    /**
     * 获取当前连接握手时使用的原始 token
     */
    public static String getToken(WebSocketSession session) {
        if (session == null) {
            return null;
        }
        Object token = session.getAttributes().get(ATTRIBUTE_TOKEN);
        return token instanceof String ? (String) token : null;
    }

    /**
     * 把原始 token 写入握手 attributes
     */
    public static void setToken(String token, Map<String, Object> attributes) {
        attributes.put(ATTRIBUTE_TOKEN, token);
    }

    /**
     * 在监听器内恢复登录用户上下文，使 TokenUtil.userId() / isDefAuth() 可用。
     *
     * <p>注意：WebSocket 线程没有 RequestContextHolder，TokenUtil.token() / menuAll() 不可用。</p>
     */
    public static void runAs(MyAuthDto user, Runnable action) {
        SecurityContext previous = SecurityContextHolder.getContext();
        try {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            if (user != null) {
                context.setAuthentication(new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities()));
            }
            SecurityContextHolder.setContext(context);
            action.run();
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }

    private static Long toLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
