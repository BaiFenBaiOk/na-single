package com.na.admin.service;

import com.na.common.aop.service.INoRepeatSubmitUserService;
import com.na.common.utils.TokenUtil;
import org.springframework.stereotype.Service;

@Service
public class NoRepeatSubmitUserServiceImpl implements INoRepeatSubmitUserService {
    @Override
    public String userId() {
        return String.valueOf(TokenUtil.userId());
    }
}
