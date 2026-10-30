package com.na.admin.controller.vo.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "文件上传结果")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResUploadVO {

    @Schema(description = "可访问的文件 URL")
    private String url;

    @Schema(description = "原始文件名")
    private String name;
}
