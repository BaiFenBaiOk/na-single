package com.na.admin.service.impl;

import com.na.admin.controller.vo.req.dept.ReqSysDeptSaveVO;
import com.na.admin.controller.vo.req.dept.ReqSysDeptUpdateVO;
import com.na.admin.controller.vo.res.dept.ResSysDeptTreeVO;
import com.na.admin.service.ISysDeptService;
import com.na.common.constant.IGlobalConst;
import com.na.common.result.R;
import com.na.common.result.Result;
import com.na.common.result.enums.Status;
import com.na.common.utils.ObjConversionUtil;
import com.na.common.utils.TreeUtil;
import com.na.core.repo.ISysDeptRepo;
import com.na.core.repo.ISysMapUserDeptRepo;
import com.na.model.SysDeptModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * sys_dept Service实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysDeptServiceImpl implements ISysDeptService {

    private final ISysDeptRepo sysDeptRepo;
    private final ISysMapUserDeptRepo sysMapUserDeptRepo;

    @Override
    public Result tree() {
        List<SysDeptModel> deptModels = sysDeptRepo.queryDeptsAll();
        List<ResSysDeptTreeVO> list = ObjConversionUtil.copyPropertiesList(deptModels, ResSysDeptTreeVO.class);
        return R.successNotMsg(TreeUtil.toTree(list, IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveObj(ReqSysDeptSaveVO vo) {
        Long parentId = Objects.isNull(vo.getParentId())
                ? IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID
                : vo.getParentId();
        // 顶级部门（parentId=0）可以直接创建；否则校验上级部门存在
        if (!Objects.equals(IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID, parentId)) {
            Result parentError = checkParentDept(parentId);
            if (!Objects.isNull(parentError)) {
                return parentError;
            }
        }
        // 查重故意不过滤 is_del：已删除的部门名不允许重建
        if (!Objects.isNull(sysDeptRepo.queryDeptByName(vo.getName()))) {
            return R.error(Status.DEPT_ALREADY_EXIST);
        }
        SysDeptModel model = SysDeptModel.builder()
                .parentId(parentId)
                .name(vo.getName())
                .orderNum(Objects.isNull(vo.getOrderNum()) ? 9999 : vo.getOrderNum())
                .remark(vo.getRemark())
                .isDel(false)
                .build();
        sysDeptRepo.saveObj(model);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateObj(ReqSysDeptUpdateVO vo) {
        SysDeptModel model = sysDeptRepo.queryDeptById(vo.getId());
        if (Objects.isNull(model) || Boolean.TRUE.equals(model.getIsDel())) {
            return R.error(Status.DEPT_NOT_EXIST);
        }
        Long parentId = Objects.isNull(vo.getParentId())
                ? IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID
                : vo.getParentId();
        if (Objects.equals(parentId, vo.getId())) {
            return R.error("上级部门不能是自己");
        }
        if (!Objects.equals(IGlobalConst.SECURITY.SYS_MENU_PARENT.PARENT_ID, parentId)) {
            // 上级必须存在，且不能是自己的子孙（否则会形成环）
            Result parentError = checkParentDept(parentId);
            if (!Objects.isNull(parentError)) {
                return parentError;
            }
            Set<Long> descendantIds = collectDescendantIds(sysDeptRepo.queryDeptsAll(), vo.getId());
            if (descendantIds.contains(parentId)) {
                return R.error("上级部门不能是自己的下级部门");
            }
        }
        // 重名校验需排除自身
        SysDeptModel duplicate = sysDeptRepo.queryDeptByName(vo.getName());
        if (!Objects.isNull(duplicate) && !Objects.equals(duplicate.getId(), vo.getId())) {
            return R.error(Status.DEPT_ALREADY_EXIST);
        }
        model.setParentId(parentId);
        model.setName(vo.getName());
        model.setOrderNum(Objects.isNull(vo.getOrderNum()) ? 9999 : vo.getOrderNum());
        model.setRemark(vo.getRemark());
        sysDeptRepo.updateObj(model);
        return R.successNotMsg();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteObj(Long id) {
        SysDeptModel model = sysDeptRepo.queryDeptById(id);
        if (Objects.isNull(model) || Boolean.TRUE.equals(model.getIsDel())) {
            return R.error(Status.DEPT_NOT_EXIST);
        }
        // 有未删除的子部门时禁止删除
        List<SysDeptModel> children = sysDeptRepo.queryDeptsByParentId(id);
        if (CollectionUtils.isNotEmpty(children)) {
            return R.error("该部门下存在子部门，无法删除");
        }
        // 有用户绑定时禁止删除
        if (sysMapUserDeptRepo.countByDeptId(id) > 0) {
            return R.error("请先解除用户与该部门的绑定关系，再删除部门");
        }
        // 逻辑删除
        model.setIsDel(true);
        sysDeptRepo.updateObj(model);
        return R.successNotMsg();
    }

    /**
     * 校验上级部门：必须存在且未删除。
     * 校验通过返回 null，否则返回携带错误信息的 Result。
     */
    private Result checkParentDept(Long parentId) {
        SysDeptModel parent = sysDeptRepo.queryDeptById(parentId);
        if (Objects.isNull(parent) || Boolean.TRUE.equals(parent.getIsDel())) {
            return R.error("上级部门不存在");
        }
        return null;
    }

    /**
     * 收集指定部门的所有子孙部门ID（不含自身）。
     * 用迭代而非递归，避免脏数据成环时栈溢出。
     */
    private Set<Long> collectDescendantIds(List<SysDeptModel> all, Long rootId) {
        Set<Long> result = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            for (SysDeptModel dept : all) {
                if (Objects.equals(dept.getParentId(), current) && result.add(dept.getId())) {
                    queue.add(dept.getId());
                }
            }
        }
        return result;
    }
}
