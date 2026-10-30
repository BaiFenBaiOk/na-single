package com.na.common.websocket.core.security;

import com.na.common.dto.MyAuthDto;
import com.na.common.websocket.util.WebSocketFrameworkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 登录用户握手拦截器。
 *
 * <p>浏览器原生 WebSocket 无法设置 Authorization 请求头，因此支持两种 token 传递方式：</p>
 * <ol>
 *     <li>连接地址查询参数：{@code ws://host/na-upms/ws?token=xxx}（浏览器主通道）</li>
 *     <li>请求头：{@code Authorization: xxx}（非浏览器客户端，直接放纯 token）</li>
 * </ol>
 *
 * <p>握手校验失败返回 401。由于该路径已加入 {@code na.auth.skipApis}，
 * 不会经过 na-auth 的 JwtAuthenticationTokenFilter。</p>
 *
 * @author pg
 */
@Slf4j
@RequiredArgsConstructor
public class LoginUserHandshakeInterceptor implements HandshakeInterceptor {

    private final WebSocketAuthService authService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String path = request.getURI().getPath();
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn("[WebSocket] 握手失败：未携带 token, path={}", path);
            return false;
        }

        MyAuthDto loginUser = authService.authenticate(token);
        if (loginUser == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 只打印脱敏后的 token 特征，避免把完整凭证写进日志
            log.warn("[WebSocket] 握手失败：token 无效或已过期, path={}, token={}",
                    path, describeToken(token));
            return false;
        }

        WebSocketFrameworkUtils.setLoginUser(loginUser, attributes);
        WebSocketFrameworkUtils.setToken(token, attributes);
        log.info("[WebSocket] 握手成功, path={}, userId={}, token={}",
                path, loginUser.getId(), describeToken(token));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // do nothing
    }

    /**
     * 优先取查询参数 token，其次取 Authorization 请求头；两者都直接使用纯 token，不处理任何前缀。
     */
    private String resolveToken(ServerHttpRequest request) {
        String token = decode(UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token"));
        if (StringUtils.hasText(token)) {
            return token;
        }
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return StringUtils.hasText(authorization) ? authorization.trim() : null;
    }

    /**
     * URL 解码查询参数，避免客户端对 token 做百分号编码后把 % 带进 JWT 解析。
     */
    private String decode(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    /**
     * 脱敏描述 token，仅用于日志排查。
     *
     * <p>如果客户端误传了带前缀的 token，这里会标记 {@code hasSpace=true}，
     * 便于快速判断是否传成了 {@code Bearer xxx}。</p>
     */
    private String describeToken(String token) {
        if (token == null) {
            return "null";
        }
        boolean hasSpace = token.indexOf(' ') >= 0;
        String masked = token.length() <= 20
                ? "***"
                : token.substring(0, 8) + "..." + token.substring(token.length() - 8);
        return masked + "(len=" + token.length() + ", hasSpace=" + hasSpace + ")";
    }
}
