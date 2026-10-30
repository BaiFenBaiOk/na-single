package com.na.common.login;

import com.na.common.constant.CacheConstInterFace;
import com.na.common.dto.ResLoginVO;
import com.na.common.utils.CacheTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 登录态存储服务。
 *
 * <p>统一维护短 token 的 Redis 读写规则，避免 upms / flow / WebSocket 各写一套。</p>
 *
 * <p>Redis key 约定：</p>
 * <pre>
 * token:login:index:{token}       -> userId        （索引：鉴权时只有 token，必须先反查 userId）
 * token:login:{userId}:{token}    -> ResLoginVO    （多点模式会话，na.login.single-token=false）
 * token:login:{userId}            -> ResLoginVO    （单点模式会话，na.login.single-token=true）
 * </pre>
 *
 * <p>单点开关见 {@link LoginProperties#isSingleToken()}（配置项 {@code na.login.single-token}）。</p>
 *
 * @author pg
 */
@Slf4j
@RequiredArgsConstructor
public class LoginSessionService {

    private final CacheTemplateUtil cacheTemplateUtil;

    private final LoginProperties loginProperties;

    /**
     * 保存登录态：写入 token 索引 + 登录会话。
     *
     * <p>单点模式下，如果该用户已有旧会话，会先删除旧 token 的索引，
     * 避免旧索引残留到 TTL 过期。</p>
     *
     * @param token     短 token（22 字符）
     * @param loginVO   登录信息（必须包含 userId）
     * @param ttlMillis 有效期（毫秒）
     */
    public void save(String token, ResLoginVO loginVO, long ttlMillis) {
        if (!StringUtils.hasText(token) || loginVO == null || loginVO.getUserId() == null) {
            throw new IllegalArgumentException("保存登录态失败：token 或 userId 不能为空");
        }
        Long userId = loginVO.getUserId();
        String sessionKey = sessionKey(userId, token);

        // 单点模式：覆盖旧会话前先清理旧 token 的索引
        if (loginProperties.isSingleToken()) {
            ResLoginVO old = cacheTemplateUtil.getCache(sessionKey, ResLoginVO.class);
            if (old != null && StringUtils.hasText(old.getToken())) {
                cacheTemplateUtil.clear(tokenIndex(old.getToken()));
            }
        }

        // 1. 索引：token -> userId
        //    鉴权时只有 token，没有 userId，必须先查索引才能拼出会话 key
        cacheTemplateUtil.setCache(tokenIndex(token), userId, ttlMillis, TimeUnit.MILLISECONDS);
        // 2. 会话：userId（+ token）-> 登录信息
        cacheTemplateUtil.setCache(sessionKey, loginVO, ttlMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 用 token 反查登录态。
     *
     * @return 有效登录信息；token 不存在、已过期、或与缓存不一致时返回 {@code null}
     */
    public ResLoginVO get(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        // 1. 索引 -> userId
        Long userId = cacheTemplateUtil.getCache(tokenIndex(token), Long.class);
        if (userId == null) {
            return null;
        }
        // 2. 会话 -> ResLoginVO
        ResLoginVO loginVO = cacheTemplateUtil.getCache(sessionKey(userId, token), ResLoginVO.class);
        if (loginVO == null) {
            return null;
        }
        // 3. 双重校验，防止索引与会话不匹配
        if (!Objects.equals(userId, loginVO.getUserId())
                || !Objects.equals(token, loginVO.getToken())) {
            return null;
        }
        return loginVO;
    }

    /**
     * 删除单个 token 的索引（登出当前终端时调用）。
     *
     * <p>会话 key 由 {@link #removeAll(Long)} 按用户前缀统一清理。</p>
     */
    public void removeTokenIndex(String token) {
        if (StringUtils.hasText(token)) {
            cacheTemplateUtil.clear(tokenIndex(token));
        }
    }

    /**
     * 清理某个用户的所有登录会话。
     *
     * <p>通过 {@code token:login:{userId}} 前缀同时覆盖单点与多点模式的会话 key；
     * 索引 key 不在该前缀下，单点模式下由 {@link #removeTokenIndex(String)} 清理，
     * 多点模式下的其他终端索引会随 TTL 自然过期（残留索引无法通过 {@link #get(String)} 校验）。</p>
     */
    public void removeAll(Long userId) {
        if (userId != null) {
            cacheTemplateUtil.clearByPrefix(CacheConstInterFace.single(userId));
        }
    }

    /**
     * 查询某用户的在线 token 列表（用户分页列表展示用）。
     *
     * <p>单点模式返回当前唯一 token；多点模式返回该用户前缀下的所有会话值。</p>
     */
    public List<Object> getOnlineTokens(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        if (loginProperties.isSingleToken()) {
            ResLoginVO loginVO = cacheTemplateUtil.getCache(
                    CacheConstInterFace.single(userId), ResLoginVO.class);
            if (loginVO == null || !StringUtils.hasText(loginVO.getToken())) {
                return Collections.emptyList();
            }
            return Collections.<Object>singletonList(loginVO.getToken());
        }
        return new ArrayList<>(cacheTemplateUtil.getValuesByPrefix(CacheConstInterFace.single(userId)));
    }

    /**
     * 会话 key：单点模式固定为 userId，多点模式为 userId + token
     */
    private String sessionKey(Long userId, String token) {
        return loginProperties.isSingleToken()
                ? CacheConstInterFace.single(userId)
                : CacheConstInterFace.notSingle(userId, token);
    }

    /**
     * token -> userId 索引 key
     */
    private String tokenIndex(String token) {
        return CacheConstInterFace.tokenIndex(token);
    }
}
