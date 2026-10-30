package com.na.admin.controller.vo.res.dept;

import com.na.common.utils.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "SysDept树出参")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResSysDeptTreeVO implements Serializable, TreeNode<Long> {

    @Schema(description = "")
    private Long id;

    @Schema(description = "上级部门ID（0=顶级部门）")
    private Long parentId;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "显示顺序")
    private Integer orderNum;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "")
    private Boolean isDel;

    /** 树层级（组树时回填，根为 1） */
    @Schema(description = "树层级（组树时回填，根为 1）")
    private Integer level;

    private List<ResSysDeptTreeVO> children;

    @Override
    public void setChildren(List<? extends TreeNode<Long>> children) {
        this.children = (List<ResSysDeptTreeVO>) children;
    }

}
