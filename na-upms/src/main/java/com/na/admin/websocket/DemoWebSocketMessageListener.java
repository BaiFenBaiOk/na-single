package com.na.admin.websocket;

import com.na.admin.websocket.dto.DemoWebSocketMessage;
import com.na.common.dto.MyAuthDto;
import com.na.common.utils.TokenUtil;
import com.na.common.websocket.core.listener.WebSocketMessageListener;
import com.na.common.websocket.core.sender.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * WebSocket 示例监听器：回显消息并附带当前登录上下文。
 *
 * <p>客户端发送：</p>
 * <pre>
 * {"type":"demo","content":"{\"text\":\"hello\"}"}
 * </pre>
 * <p>服务端回复：</p>
 * <pre>
 * {"type":"demo-reply","content":"{\"echo\":\"hello\",\"userId\":1}"}
 * </pre>
 *
 * <p>该示例同时验证：type 路由、泛型反序列化、SecurityContext 恢复、按 sessionId 定向发送。</p>
 *
 * @author pg
 */
@Slf4j
@Component
@RequiredArgsConstructor
// WebSocket 关闭时不要加载监听器，否则会因缺少 WebSocketMessageSender 导致启动失败
@ConditionalOnProperty(prefix = "na.websocket", name = "enable",
        havingValue = "true", matchIfMissing = true)
public class DemoWebSocketMessageListener implements WebSocketMessageListener<DemoWebSocketMessage> {

    public static final String TYPE = "demo";

    public static final String REPLY_TYPE = "demo-reply";

    private final WebSocketMessageSender webSocketMessageSender;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void onMessage(WebSocketSession session, DemoWebSocketMessage message) {
        // 监听器内已恢复 SecurityContext，可直接使用 TokenUtil 获取当前登录用户
        MyAuthDto loginUser = TokenUtil.getUser();
        Map<String, Object> reply = new LinkedHashMap<>();
        reply.put("echo", message == null ? null : message.getText());
        reply.put("userId", loginUser == null ? null : loginUser.getId());

        webSocketMessageSender.sendToSession(session.getId(), REPLY_TYPE, reply);
        log.info("[WebSocket][demo] 收到消息并回显, sessionId={}, userId={}",
                session.getId(), loginUser == null ? null : loginUser.getId());
    }
}
