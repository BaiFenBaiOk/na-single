package com.na.common.websocket.core.sender;

/**
 * WebSocket 消息发送器。
 *
 * <p>content 支持两种形式：</p>
 * <ul>
 *     <li>String / CharSequence：视为已经序列化好的 JSON 字符串，原样下发</li>
 *     <li>其他对象：由 ObjectMapper 序列化为 JSON 字符串后下发</li>
 * </ul>
 *
 * @author pg
 */
public interface WebSocketMessageSender {

    /**
     * 发送给指定连接
     */
    void sendToSession(String sessionId, String messageType, Object content);

    /**
     * 发送给指定用户（同一用户的多标签页 / 多端都会收到）
     *
     * @param userType 用户类型（00 / 03），可为 null 表示不限制
     * @param userId   用户 ID
     */
    void sendToUser(String userType, Long userId, String messageType, Object content);

    /**
     * 发送给指定用户类型的所有连接
     */
    void sendToUserType(String userType, String messageType, Object content);

    /**
     * 发送给所有连接
     */
    void broadcast(String messageType, Object content);
}
