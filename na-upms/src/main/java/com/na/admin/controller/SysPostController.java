package com.na.admin.controller;

import com.na.admin.controller.vo.req.post.ReqSysPostPagingVO;
import com.na.admin.controller.vo.req.post.ReqSysPostSaveVO;
import com.na.admin.controller.vo.req.post.ReqSysPostUpdateVO;
import com.na.admin.formatter.auth.post.LogPostDeleteFormatter;
import com.na.admin.formatter.auth.post.LogPostPagingFormatter;
import com.na.admin.formatter.auth.post.LogPostSaveFormatter;
import com.na.admin.formatter.auth.post.LogPostUpdateFormatter;
import com.na.admin.service.ISysPostService;
import com.na.auth.annotation.AuthNotCheckedApi;
import com.na.common.annotation.NoRepeatSubmit;
import com.na.common.base.BaseController;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.syslog.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "岗位")
@RestController
// 模块前缀 na-upms：与网关 / Nginx 的 /api/na-upms/** 分流规则保持一致
@RequestMapping("/api/na-upms/post")
@RequiredArgsConstructor
public class SysPostController extends BaseController {

    private final ISysPostService sysPostService;

    @Operation(summary = "列表")
    @OperationLog(formatter = LogPostPagingFormatter.class)
    @PostMapping("/list-paging")
    @AuthNotCheckedApi
    public Result queryPaging(@RequestBody ReqSysPostPagingVO pagingVO) {
        if (Status.SUCCESS != checkPageParams(pagingVO.getCurrentPage(), pagingVO.getPageSize())) {
            return R.error(Status.ERROR);
        }
        return sysPostService.queryPaging(pagingVO);
    }

    @Operation(summary = "创建")
    @OperationLog(formatter = LogPostSaveFormatter.class)
    @PostMapping("/save")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result save(@RequestBody @Validated ReqSysPostSaveVO vo) {
        return sysPostService.saveObj(vo);
    }

    @Operation(summary = "编辑")
    @OperationLog(formatter = LogPostUpdateFormatter.class)
    @PutMapping("/update")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    public Result update(@RequestBody @Validated ReqSysPostUpdateVO vo) {
        return sysPostService.updateObj(vo);
    }

    @Operation(summary = "删除，有用户绑定则拒绝")
    @OperationLog(formatter = LogPostDeleteFormatter.class)
    @DeleteMapping("/delete/{id}")
    @AuthNotCheckedApi
    @NoRepeatSubmit(value = 2000)
    @Validated
    public Result delete(@PathVariable("id") @NotNull(message = "id不能为空") Long id) {
        return sysPostService.deleteObj(id);
    }

}
