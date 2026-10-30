package com.na.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.List;

/**
 * 响应层异常兜底配置。
 *
 * <p>把"预期内、不该按服务端错误上报"的异常在解析链最前面消化掉，
 * 避免它们进入全局异常处理器
 * （{@code com.na.common.exceptions.ApiExceptionHandler}，位于外部 jar，会无条件打 ERROR 全堆栈）：
 *
 * <ol>
 *     <li><b>客户端断开</b>：视频 / 大文件传输中途浏览器主动断开（seek、暂停、关闭页面），
 *         服务端继续写数据抛 {@code IOException: Connection reset by peer}。属预期行为，降为 debug。</li>
 *     <li><b>资源不存在</b>：Spring Boot 3.2+ 对未命中任何 handler / 静态文件的请求抛
 *         {@link NoResourceFoundException}（如浏览器自动请求的 /favicon.ico）。
 *         修正为标准 404 状态码，降为 debug。</li>
 *     <li><b>响应已提交</b>：此时错误体无法再写出，打一行 warn 保留现场，避免全局处理器重复报错。</li>
 * </ol>
 *
 * <p>其余异常返回 null 继续走原解析链，全局异常处理行为不变。
 */
@Slf4j
@Configuration
public class WebExceptionResolverConfig implements WebMvcConfigurer, HandlerExceptionResolver {

    /**
     * 各类断开场景的异常信息关键词：
     * Connection reset（Linux）、Broken pipe（Unix 管道断开）、
     * An established connection was aborted（Windows）、Stream closed（Undertow 输出流已关闭）。
     */
    private static final String[] DISCONNECT_KEYWORDS = {
            "Connection reset", "Broken pipe", "An established connection was aborted", "Stream closed"
    };

    /**
     * 把本解析器插到解析链最前面：异常先经过这里，命中预期场景直接消化；
     * 未命中返回 null，继续交给后面的解析器（全局异常处理器）处理。
     */
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, this);
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {
        if (isClientDisconnect(ex)) {
            // 浏览器 seek / 暂停 / 关闭导致的正常断开，属预期行为，降为 debug 且不打堆栈
            log.debug("客户端已断开连接，忽略响应写入异常: {} {}", request.getRequestURI(), ex.getMessage());
            return new ModelAndView();
        }
        if (ex instanceof NoResourceFoundException) {
            // 保持 HTTP 语义：不存在的资源 / 接口就该是 404，而不是 200 + 内部错误码；
            // 这里不触发 /error 转发，直接置 404 空响应，避免被安全过滤器链二次拦截
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.debug("请求的资源不存在: {} {}", request.getMethod(), request.getRequestURI());
            return new ModelAndView();
        }
        if (response.isCommitted()) {
            // 响应已提交后异常无法再写出错误体，打一行 warn 保留现场
            log.warn("响应已提交，异常无法写出错误响应: {} {}", request.getRequestURI(), ex.toString());
            return new ModelAndView();
        }
        // 其余异常交回默认解析链（全局异常处理器）
        return null;
    }

    /** 沿 cause 链判断是否为客户端断开类 IOException */
    private static boolean isClientDisconnect(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof IOException) {
                String message = t.getMessage();
                if (message != null) {
                    for (String keyword : DISCONNECT_KEYWORDS) {
                        if (message.contains(keyword)) {
                            return true;
                        }
                    }
                }
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }
}
