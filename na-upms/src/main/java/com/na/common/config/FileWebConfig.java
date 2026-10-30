package com.na.common.config;

import com.na.common.utils.CommonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地文件资源的静态映射配置。
 *
 * <p>把 YAML 中配置的本地目录（Windows / Linux 各一份绝对路径）映射为 HTTP 请求前缀，
 * 适用于视频、图片、附件等任意本地文件；运行时通过 {@link CommonUtil#isWindows()}
 * 判断当前操作系统自动选用对应路径，因此同一份配置在开发机（Windows）与服务器（Linux）上都能直接使用。
 *
 * <p>YAML 配置示例（na.file，见 na-upms / bootstrap.yml）：
 * <pre>
 * na:
 *   file:
 *     request-prefixes:              # 请求前缀集合，可配置多个，全部指向同一个本地目录
 *       - /video/**
 *     windows-path: D:/temp          # Windows 下的本地目录绝对路径（开发机）
 *     linux-path: /na/temp           # Linux 下的本地目录绝对路径（服务器）
 * </pre>
 *
 * @author pg
 * @date 2026年10月14日
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "na.file")
public class FileWebConfig implements WebMvcConfigurer {

    /**
     * 请求前缀集合：命中这些前缀的请求会被映射到本地文件目录。
     * 支持配置多个前缀（集合形式），例如：
     * <ul>
     *     <li>/video/** —— 普通访问：/video/{相对路径} → {本地目录}/{相对路径}</li>
     *     <li>/video/admin-api/infra/file/*&#47;get/** —— 兼容 infra 文件下载地址形式</li>
     * </ul>
     */
    private List<String> requestPrefixes = new ArrayList<>();

    /** Windows 系统下的本地文件目录绝对路径（开发机），如 D:/temp */
    private String windowsPath;

    /** Linux 系统下的本地文件目录绝对路径（服务器），如 /na/temp */
    private String linuxPath;

    /**
     * 注册本地文件资源的静态映射关系。
     * Spring MVC 启动时调用：按当前操作系统选择本地目录，为每个请求前缀注册一个资源处理器。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 未配置请求前缀时不注册任何映射，避免影响其它静态资源规则
        if (requestPrefixes == null || requestPrefixes.isEmpty()) {
            log.warn("未配置 na.file.request-prefixes，跳过文件资源映射");
            return;
        }

        // 按操作系统选择本地目录：Windows 用 windows-path，其余（Linux/Unix）用 linux-path
        boolean windows = CommonUtil.isWindows();
        String basePath = windows ? windowsPath : linuxPath;
        if (StringUtils.isBlank(basePath)) {
            log.warn("当前为 {} 系统，但 na.file.{} 未配置，跳过文件资源映射",
                    windows ? "Windows" : "Linux", windows ? "windows-path" : "linux-path");
            return;
        }

        // 资源目录必须以 "/" 结尾，否则 Spring 拼接文件相对路径时会出错；
        // 目录不存在时只告警不中断启动：文件可能尚未放入，或由部署脚本负责创建
        String location = basePath.endsWith("/") ? basePath : basePath + "/";
        if (!new File(basePath).exists()) {
            log.warn("本地文件目录不存在: {}，请检查 na.file 配置", basePath);
        }

        // 每个请求前缀分别注册到同一个本地目录
        for (String prefix : requestPrefixes) {
            registry.addResourceHandler(prefix)
                    .addResourceLocations("file:" + location)
                    .resourceChain(false)
                    .addResolver(new InfraFileResourceResolver());
            log.info("文件资源映射: {} -> file:{}", prefix, location);
        }
    }

    /**
     * infra 文件下载地址兼容解析器。
     *
     * <p>用于兼容请求形如 {@code /video/admin-api/infra/file/{fileId}/get/20261012/xxx.jpg} 的地址：
     * 截掉 /get/ 之前的 infra 路径部分，只用其后的相对路径到本地目录找文件。
     * 不含 /get/ 的普通请求（如 /video/20261012/xxx.jpg）不会被修改，走默认解析逻辑。
     */
    private static class InfraFileResourceResolver extends PathResourceResolver {
        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            // 不同路径匹配器算出的 resourcePath 形态不同（可能带完整前缀，也可能只剩剩余路径），
            // 只要包含 /get/ 就截掉它之前的部分，只用后面的相对路径到本地目录找文件
            int index = resourcePath.indexOf("/get/");
            if (index >= 0) {
                resourcePath = resourcePath.substring(index + "/get".length());
            }
            return super.getResource(resourcePath, location);
        }
    }
}
