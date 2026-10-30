package com.na.admin.controller.vo.res.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "SysPost分页出参")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResSysPostPagingVO {

    @Schema(description = "")
    private Long id;

    @Schema(description = "上级岗位ID（平铺结构恒为 0）")
    private Long parentId;

    @Schema(description = "岗位名称")
    private String name;

    @Schema(description = "岗位编码（全局唯一）")
    private String code;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "")
    private Boolean isDel;

}
