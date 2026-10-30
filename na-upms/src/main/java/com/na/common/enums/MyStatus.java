package com.na.common.enums;

import com.na.common.result.enums.IStatusProvider;
import com.na.common.utils.LocalLangUtil;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public enum MyStatus implements IStatusProvider {
    CAPTCHA_ERROR(140003, "code error", "验证码错误", "code.error"),

    ;


    /** 状态码 */
    private final Integer code;
    /** 内置英文消息(i18n 缺失时的兜底文案) */
    private final String enMsg;
    /** 内置中文消息 */
    private final String zhMsg;
    /** i18n 资源文件中的消息 key */
    private final String msgKey;

    /**
     * @param code   状态码
     * @param enMsg  内置英文消息
     * @param zhMsg  内置中文消息
     * @param msgKey i18n 资源文件消息 key(static/i18n/messages*.properties)
     */
    MyStatus(Integer code, String enMsg, String zhMsg, String msgKey) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
        this.msgKey = msgKey;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getEnMsg() {
        return enMsg;
    }

    @Override
    public String getZhMsg() {
        return zhMsg;
    }

    @Override
    public String getMsg() {
        // 中文语言环境返回内置中文(默认), 其余语言返回内置英文
        return LocaleContextHolder.getLocale().getLanguage().equals(Locale.CHINA.getLanguage()) ? zhMsg : enMsg;
    }

    @Override
    public String msgKey() {
        return msgKey;
    }

    @Override
    public String getLanguageMsg() {
        // 优先从 static/i18n/messages*.properties 读取当前语言翻译; key 不存在时回退内置英文
        return LocalLangUtil.get(msgKey, enMsg);
    }
}
