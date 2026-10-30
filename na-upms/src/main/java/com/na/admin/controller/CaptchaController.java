package com.na.admin.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.na.admin.formatter.auth.captcha.LogCaptchaImageFormatter;
import com.na.auth.annotation.AnonymousAccess;
import com.na.common.constant.CacheConstInterFace;
import com.na.common.utils.CacheTemplateUtil;
import com.na.syslog.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "验证码")
@RestController
// 模块前缀 na-upms：与网关 / Nginx 的 /api/na-upms/** 分流规则保持一致
@RequestMapping("/api/na-upms/systemInfo")
@RequiredArgsConstructor
public class CaptchaController {

    private final CacheTemplateUtil cacheTemplateUtil;

    /**
     * 获取图形验证码图片，返回captchaKey给前端
     */
    @Operation(summary = "获取图形验证码图片，返回captchaKey给前端")
    @OperationLog(formatter = LogCaptchaImageFormatter.class)
    @GetMapping("/get/{id}")
    @Validated
    @AnonymousAccess
    public void getCaptcha(@PathVariable @NotBlank(message = "验证码不能为空") String id , HttpServletResponse response) throws IOException {
        // 1. 创建验证码 宽120 高40，4位字符，5条干扰线
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 5);
        String code = captcha.getCode();
        String redisKey = CacheConstInterFace.CAPTCHA.KEY + id;
        // 2. 生成key，存入redis
        cacheTemplateUtil.setCache(redisKey, code, 300L, TimeUnit.SECONDS);

        // 3. 把captchaKey放到响应头返回给前端
        response.setHeader("X-Captcha-Key", id);
        response.setContentType("image/jpeg");
        response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        response.setDateHeader("Expires", 0);

        // 输出图片流
        captcha.write(response.getOutputStream());
    }

    @AnonymousAccess
    @PostMapping("/post")
    public void getCaptcha2(@RequestBody Map<String,Object> map) throws IOException {
        System.out.println(map);
    }

    /**
     * 触发器联调接口：打印请求体后固定返回 HTTP 403，
     * 用于验证触发器的失败重试与失败落库逻辑。
     */
    @AnonymousAccess
    @PostMapping("/post2")
    public void getCaptcha3(@RequestBody Map<String,Object> map, HttpServletResponse response) throws IOException {
        System.out.println(map);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
