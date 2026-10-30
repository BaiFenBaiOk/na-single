package com.na.admin.service.impl;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.na.admin.controller.vo.res.ResUploadVO;
import com.na.admin.service.IFileUploadService;
import com.na.admin.utils.CaCheUtils;
import com.na.common.constant.CacheSysConfigConst;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.common.utils.CacheTemplateUtil;
import com.na.common.utils.TokenUtil;
import com.na.core.repo.ISystemConfigRepo;
import com.na.model.SystemConfigModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements IFileUploadService {

    /** 未配置 aliMaxSize 时的兜底：10MB（配置值单位为字节） */
    private static final long DEFAULT_MAX_SIZE = 10L * 1024 * 1024;

    /** 未配置 aliWhite 时的兜底图片类型 */
    private static final Set<String> DEFAULT_WHITE = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final CaCheUtils caCheUtils;
    private final CacheTemplateUtil cacheTemplateUtil;
    private final ISystemConfigRepo systemConfigRepo;


    @Override
    public Result upload(MultipartFile file) {
        if (TokenUtil.userId() == null) {
            return R.error(Status.USER_AUTH_EXPIRED);
        }
        if (file == null || file.isEmpty()) {
            return R.error("请选择要上传的文件");
        }

        String cacheKey = caCheUtils.getCacheKey();
        String accessKey = getConfig(cacheKey, CacheSysConfigConst.ALI_ACCESS_KEY);
        String secretKey = getConfig(cacheKey, CacheSysConfigConst.ALI_SECRET_KEY);
        String endpoint = getConfig(cacheKey, CacheSysConfigConst.ALI_ENDPOINT);
        String bucketName = getConfig(cacheKey, CacheSysConfigConst.ALI_BUCKET_NAME);
        String domain = getConfig(cacheKey, CacheSysConfigConst.ALI_DOMAIN);
        if (StringUtils.isAnyBlank(accessKey, secretKey, endpoint, bucketName, domain)) {
            return R.error("未配置完整的阿里云 OSS 参数");
        }

        long maxSize = parseLong(
                getConfig(cacheKey, CacheSysConfigConst.ALI_MAX_SIZE), DEFAULT_MAX_SIZE);
        if (file.getSize() > maxSize) {
            return R.error("文件大小超过限制");
        }

        String originalName = StringUtils.defaultIfBlank(file.getOriginalFilename(), "file");
        String extension = "." + StringUtils.substringAfterLast(originalName, ".")
                .toLowerCase(Locale.ROOT);
        if (".".equals(extension) || StringUtils.isBlank(extension.substring(1))) {
            return R.error("文件缺少扩展名");
        }
        Set<String> whiteList = parseWhite(
                getConfig(cacheKey, CacheSysConfigConst.ALI_WHITE));
        if (whiteList.isEmpty()) {
            whiteList = DEFAULT_WHITE;
        }
        if (!whiteList.contains(extension)) {
            return R.error("不支持的文件类型：" + extension);
        }

        String objectName = "files/"
                + LocalDate.now().format(DATE_FORMATTER) + "/"
                + UUID.randomUUID().toString().replace("-", "") + extension;

        OSSClient client = null;
        try {
            client = new OSSClient(normalizeEndpoint(endpoint), accessKey, secretKey);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (StringUtils.isNotBlank(file.getContentType())) {
                metadata.setContentType(file.getContentType());
            }
            try (InputStream inputStream = file.getInputStream()) {
                client.putObject(bucketName, objectName, inputStream, metadata);
            }
        } catch (Exception e) {
            log.error("[OSS上传] 上传失败，object={}", objectName, e);
            return R.error("文件上传失败，请稍后重试");
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }

        String url = normalizeDomain(domain) + "/" + objectName;
        log.info("[OSS上传] 上传成功，url={}", url);
        return R.successNotMsg(ResUploadVO.builder()
                .url(url)
                .name(originalName)
                .build());
    }

    /**
     * 读取全局配置：Redis hash 优先，未命中查 system_config 并回写。
     */
    private String getConfig(String cacheKey, String code) {
        Object cached = cacheTemplateUtil.hget(cacheKey, code);
        if (cached != null && StringUtils.isNotBlank(cached.toString())) {
            return cached.toString().trim();
        }
        SystemConfigModel model = systemConfigRepo.queryByCode(code);
        if (model != null && StringUtils.isNotBlank(model.getValue())) {
            String value = model.getValue().trim();
            cacheTemplateUtil.hset(cacheKey, code, value);
            return value;
        }
        return null;
    }

    private static long parseLong(String value, long defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** 白名单支持 .jpg 或 jpg 两种写法，统一转成小写带点格式 */
    private static Set<String> parseWhite(String raw) {
        if (StringUtils.isBlank(raw)) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(item -> item.startsWith(".") ? item : "." + item)
                .map(item -> item.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private static String normalizeEndpoint(String endpoint) {
        String value = endpoint.trim().replaceAll("/+$", "");
        if (value.startsWith("http://")) {
            return value.substring("http://".length());
        }
        if (value.startsWith("https://")) {
            return value.substring("https://".length());
        }
        return value;
    }

    private static String normalizeDomain(String domain) {
        String value = domain.trim().replaceAll("/+$", "");
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return "https://" + value;
        }
        return value;
    }
}
