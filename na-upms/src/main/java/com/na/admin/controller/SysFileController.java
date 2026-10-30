package com.na.admin.controller;

import com.na.common.config.FileWebConfig;
import com.na.common.result.R;
import com.na.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 通用文件上传。
 *
 * <p>文件保存到 na.file 配置的本地目录下：
 * {@code {basePath}/upload/{dir}/{yyyyMMdd}/{uuid}.{ext}}。</p>
 *
 * <p>返回的 url 形如 {@code /na-upms/video/upload/20261023/xxx.png}，
 * 通过网关 /na-upms/** 路由或 Nginx 直连访问，由 FileWebConfig 的 /video/** 映射读取。</p>
 */
@RestController
@RequestMapping("/api/na-upms/sys-file")
@RequiredArgsConstructor
@Slf4j
public class SysFileController {

    /** 后端兜底大小限制：100MB（前端组件会按字段配置做更小限制） */
    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024;

    /** 允许上传的扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "webp", "bmp",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "pdf", "csv", "zip", "rar",
            // 富文本编辑器内嵌视频
            "mp4", "webm", "ogg", "mov"
    );

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final FileWebConfig fileWebConfig;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "dir", required = false) String dir) {
        if (file == null || file.isEmpty()) {
            return R.error("请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return R.error("文件大小不能超过 100MB");
        }

        String originalName = StringUtils.defaultString(file.getOriginalFilename());
        String extension = StringUtils.substringAfterLast(originalName, ".").toLowerCase(Locale.ROOT);
        if (StringUtils.isBlank(extension) || !ALLOWED_EXTENSIONS.contains(extension)) {
            return R.error("不支持的文件类型: " + extension);
        }

        // 上传目录只允许字母、数字、下划线、短横线，防止路径穿越
        String subDir = StringUtils.trimToEmpty(dir);
        if (StringUtils.isNotBlank(subDir) && !subDir.matches("[A-Za-z0-9_-]+")) {
            return R.error("上传目录不合法");
        }

        boolean windows = System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT).contains("win");
        String basePath = windows ? fileWebConfig.getWindowsPath() : fileWebConfig.getLinuxPath();
        if (StringUtils.isBlank(basePath)) {
            return R.error("未配置 na.file 文件存储目录");
        }

        String dateDir = LocalDate.now().format(DATE_FORMAT);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path targetDir = StringUtils.isBlank(subDir)
                ? Paths.get(basePath, "upload", dateDir)
                : Paths.get(basePath, "upload", subDir, dateDir);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetDir.resolve(storedName).toFile());
        } catch (Exception e) {
            log.error("文件上传失败 originalName={}", originalName, e);
            return R.error("文件上传失败");
        }

        String url = "/na-upms/video/upload/"
                + (StringUtils.isBlank(subDir) ? "" : subDir + "/")
                + dateDir + "/" + storedName;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("url", url);
        data.put("name", originalName);
        data.put("size", file.getSize());
        return R.successNotMsg(data);
    }
}
