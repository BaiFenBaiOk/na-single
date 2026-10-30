package com.na.common.constant;

public interface CacheConstInterFace {

    interface CAPTCHA{
        String KEY = "captcha:";
    }

    interface TOKEN {
        String TOKEN_LOGIN_USER = "token:login:{userId}";

        /**
         * token -> userId 索引，用于鉴权时通过短 token 反查用户
         */
        String TOKEN_LOGIN_INDEX = "token:login:index:{token}";
    }

    interface LOING_APP{
        /**
         * 登录客户端
         */
        public static final String APP = "APP";
        public static final String WEB = "WEB";
        public static final String WX_MINI = "WX_MINI";
    }

    static String single(Long userId){
        return CacheConstInterFace.TOKEN.TOKEN_LOGIN_USER.replace("{userId}",userId.toString());
    }

    static String notSingle(Long userId,String randomStr){
        return CacheConstInterFace.TOKEN.TOKEN_LOGIN_USER.replace("{userId}",userId.toString())+ ":" + randomStr;
    }

    /**
     * token -> userId 索引 key
     */
    static String tokenIndex(String token){
        return CacheConstInterFace.TOKEN.TOKEN_LOGIN_INDEX.replace("{token}", token);
    }
}
