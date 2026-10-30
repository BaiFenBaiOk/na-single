package com.na.admin.service.impl;

import com.na.admin.controller.vo.req.post.ReqSysPostPagingVO;
import com.na.admin.controller.vo.req.post.ReqSysPostSaveVO;
import com.na.admin.controller.vo.req.post.ReqSysPostUpdateVO;
import com.na.admin.controller.vo.res.post.ResSysPostPagingVO;
import com.na.admin.service.ISysPostService;
import com.na.common.base.BasePagesDomain;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.common.utils.ObjConversionUtil;
import com.na.core.repo.ISysMapUserPostRepo;
import com.na.core.repo.ISysPostRepo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.na.model.SysPostModel;
import com.na.params.SysPostParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * sys_post Service实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysPostServiceImpl implements ISysPostService {

    private final ISysPostRepo sysPostRepo;
    private final ISysMapUserPostRepo sysMapUserPostRepo;

    @Override
    public Result queryPaging(ReqSysPostPagingVO pagingVO) {
        SysPostParams params = SysPostParams.builder()
                .name(pagingVO.getName())
                .code(pagingVO.getCode())
                .isDel(pagingVO.getIsDel())
                .build();
        params.setCurrentPage(pagingVO.getCurrentPage());
        params.setPageSize(pagingVO.getPageSize());
        IPage<SysPostModel> iPage = sysPostRepo.queryPaging(params);
        return R.successNotMsg(BasePagesDomain.pages(iPage,
                ObjConversionUtil.copyPropertiesList(iPage.getRecords(), ResSysPostPagingVO.class)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveObj(ReqSysPostSaveVO vo) {
        // 查重故意不过滤 is_del：唯一索引不含 is_del，过滤会导致撞索引 500
        if (!Objects.isNull(sysPostRepo.queryPostByCode(vo.getCode()))) {
            return R.error("岗位编码已存在，请修改后重新尝试");
        }
        if (!Objects.isNull(sysPostRepo.queryPostByName(vo.getName()))) {
            return R.error("岗位名称已存在，请修改后重新尝试");
        }
        SysPostModel model = SysPostModel.builder()
                // 岗位为平铺结构，parent_id 恒为 0
                .parentId(0L)
                .name(vo.getName())
                .code(vo.getCode())
                .isDel(false)
                .build();
        sysPostRepo.saveObj(model);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateObj(ReqSysPostUpdateVO vo) {
        SysPostModel model = sysPostRepo.queryPostById(vo.getId());
        if (Objects.isNull(model) || Boolean.TRUE.equals(model.getIsDel())) {
            return R.error("岗位不存在");
        }
        // 编码查重需排除自身（同样不过滤 is_del）
        SysPostModel duplicate = sysPostRepo.queryPostByCode(vo.getCode());
        if (!Objects.isNull(duplicate) && !Objects.equals(duplicate.getId(), vo.getId())) {
            return R.error("岗位编码已存在，请修改后重新尝试");
        }
        // 名称查重同样排除自身
        SysPostModel duplicateName = sysPostRepo.queryPostByName(vo.getName());
        if (!Objects.isNull(duplicateName) && !Objects.equals(duplicateName.getId(), vo.getId())) {
            return R.error("岗位名称已存在，请修改后重新尝试");
        }
        model.setName(vo.getName());
        model.setCode(vo.getCode());
        sysPostRepo.updateObj(model);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteObj(Long id) {
        SysPostModel model = sysPostRepo.queryPostById(id);
        if (Objects.isNull(model) || Boolean.TRUE.equals(model.getIsDel())) {
            return R.error("岗位不存在");
        }
        // 有用户绑定时禁止删除
        if (sysMapUserPostRepo.countByPostId(id) > 0) {
            return R.error("请先解除用户与该岗位的绑定关系，再删除岗位");
        }
        // 逻辑删除
        model.setIsDel(true);
        sysPostRepo.updateObj(model);
        return R.successNotMsg();
    }
}
