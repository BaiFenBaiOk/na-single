package com.na.admin.controller.vo.res;

import com.na.common.utils.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema
@Data
@Builder
public class ResMenuTreeVO implements Serializable, TreeNode<Long> {

    private Long id;
    private String name;
    private String menuName;
    private Long parentId;
    private String parentName;
    private Integer orderNum;
    private String path;
    private String component;
    private String requestUri;
    private String perms;
    private String query;
    private Integer isFrame;
    private Integer isCache;
    private String menuType;
    private String visible;
    private String status;
    private String icon;
    private String sysMenuType;
    private String remark;
    private Boolean isDel;

    private Integer level;

    /** 仅为补全树层级而加入的祖先节点（不在实际授权范围内），前端应显示为不可勾选 */
    private Boolean disabled;

    private List<ResMenuTreeVO> children;

    List<ResMenuTreeVO> listBtn;

    @Override
    public void setChildren(List<? extends TreeNode<Long>> children) {
        this.children = (List<ResMenuTreeVO>) children;
    }

}
