package com.na.common.websocket.core.security;

import com.na.common.dto.MyAuthDto;
import com.na.common.dto.ResLoginVO;
import com.na.common.login.LoginSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * WebSocket 握手鉴权服务。
 *
 * <p>登录 token 是 22 字符的短 token，本身不携带业务信息。
 * 校验统一委托给 {@link LoginSessionService}：
 * 先通过 {@code token:login:index:{token}} 反查 userId，
 * 再读取 {@code token:login:{userId}:{token}}（单点模式为 {@code token:login:{userId}}）的登录会话。</p>
 *
 * @author pg
 */
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthService {

    private final LoginSessionService loginSessionService;

    /**
     * 校验 token 并返回登录用户；校验失败返回 null
     */
    public MyAuthDto authenticate(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("[WebSocket] token 为空，跳过鉴权");
            return null;
        }
        try {
            ResLoginVO loginVO = loginSessionService.get(token);
            if (loginVO == null) {
                log.warn("[WebSocket] token 校验失败：登录态不存在、已过期或已登出");
                return null;
            }
            Long userId = loginVO.getUserId();
            log.debug("[WebSocket] token 校验通过, userId={}", userId);
            return buildAuthUser(userId, token, loginVO);
        } catch (Exception e) {
            log.warn("[WebSocket] token 鉴权异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 仅校验 token 是否仍然有效（用于长连接期间的周期复核）
     */
    public boolean isTokenValid(String token) {
        return authenticate(token) != null;
    }

    private MyAuthDto buildAuthUser(Long userId, String token, ResLoginVO loginVO) {
        MyAuthDto authUser = new MyAuthDto();
        authUser.setId(String.valueOf(userId));
        authUser.setUserName(loginVO.getUserName());
        authUser.setNickName(loginVO.getNickName());
        authUser.setPhone(loginVO.getPhone());
        authUser.setEmail(loginVO.getEmail());
        authUser.setAvatar(loginVO.getAvatar());
        authUser.setUserType(loginVO.getUserType());
        authUser.setToken(token);
        return authUser;
    }
}
