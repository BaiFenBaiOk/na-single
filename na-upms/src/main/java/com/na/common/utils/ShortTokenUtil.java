package com.na.common.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 短登录 token 生成工具。
 *
 * <p>生成 16 字节（128 位）安全随机数并做 Base64 URL 无填充编码，
 * 结果为 22 个字符，可直接放在请求头或 WebSocket 查询参数中使用。</p>
 *
 * <p>token 本身不携带任何业务信息，登录态以 Redis 中的
 * {@code token:login:index:{token}} 与 {@code token:login:{userId}:{token}} 为准。</p>
 *
 * @author pg
 */
public final class ShortTokenUtil {

    /**
     * 128 位随机数，Base64 URL 无填充后固定 22 个字符
     */
    private static final int TOKEN_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private ShortTokenUtil() {
    }

    /**
     * 生成短 token
     */
    public static String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
