package com.na.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.na.common.utils.DateTimeUtil;
import com.na.common.utils.TokenUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Configuration
public class MySqlTimeConfig implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasSetter("createTime")) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, DateTimeUtil.getCurrentBeijingDateTime());
        }
        if (metaObject.hasSetter("isDel")) {
            Object isDel = getFieldValByName("isDel", metaObject);
            if(isDel == null){
                this.setFieldValByName("isDel", Boolean.FALSE, metaObject);
            }
        }
        if (metaObject.hasSetter("creatorId")) {
            this.strictInsertFill(metaObject, "creatorId", Long.class, TokenUtil.userId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = DateTimeUtil.getCurrentBeijingDateTime();
        if (metaObject.hasSetter("updateTime")) {
            this.setFieldValByName("updateTime",  now, metaObject);
        }
        if (metaObject.hasSetter("isDel")) {
            // ⭐ 如果是逻辑删除，同时填 deleteTime
            Object isDel = getFieldValByName("isDel", metaObject);
            if (Boolean.TRUE.equals(isDel)) {
                this.setFieldValByName("isDel", Boolean.TRUE, metaObject);
                if (metaObject.hasSetter("deleteTime")) {
                    this.setFieldValByName("deleteTime", now, metaObject);
                }
            }
        }
    }
}
