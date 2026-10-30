package com.na.admin.service;

import com.na.common.config.service.IRedisKeyExpirationService;
import com.na.common.constant.CommonConst;
import com.na.common.utils.CacheTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MyRedisKeyExpirationListener implements IRedisKeyExpirationService {

    @Autowired
    private CacheTemplateUtil cacheTemplateUtil;

    @Override
    public void run(String message, byte[] pattern) throws Exception {
        System.out.println("RedisMessageListener:" + message);
        if(StringUtils.isNotEmpty(message)){
            //TODO 处理过期key
            // 加Redis分布式锁，防止和定时任务并发处理同一个数据
            String lockKey = CommonConst.LOCK_PUBLISH + message;
            // 锁过期时间设置为30秒，足够处理完业务逻辑
            boolean locked = cacheTemplateUtil.tryLock(lockKey, "1", 30, TimeUnit.SECONDS);
            if (!locked) {
                log.info("【Redis过期事件】数据正在被其他线程处理，跳过：{}", message);
                return;
            }
            try {
                //TODO 处理过期key
            }finally {
                // 释放锁
                cacheTemplateUtil.releaseLock(lockKey);
            }

        }
    }
}
