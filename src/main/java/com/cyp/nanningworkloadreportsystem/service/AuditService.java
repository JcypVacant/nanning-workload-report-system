package com.cyp.nanningworkloadreportsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyp.nanningworkloadreportsystem.entity.*;
import com.cyp.nanningworkloadreportsystem.mapper.*;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 审核服务类
 * 负责车间审核工区数据（通过/退回）、段级锁定等操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRecordMapper auditRecordMapper;
    private final WorkReportMapper reportMapper;
    private final WorkReportItemMapper itemMapper;
    private final EmployeeMapper employeeMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final OperationLogService logService;

    /**
     * 查询待审核的填报记录（按工区汇总）
     * 车间管理员只能查看下属工区的数据
     */
    public List<WorkReport> getPending(Long periodId) {
        LambdaQueryWrapper<WorkReport> wrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(WorkReport::getStatus, "已提交");

        // 车间管理员只能查看本车间下属工区的数据
        if (UserContext.isWorkshopAdmin()) {
            wrapper.eq(WorkReport::getWorkshopId, UserContext.getOrgId());
        }

        wrapper.orderByAsc(WorkReport::getAreaId, WorkReport::getWorkDate);
        return reportMapper.selectList(wrapper);
    }

    /**
     * 审核通过
     */
    @Transactional
    public void approve(Long reportId, String comment) {
        WorkReport report = reportMapper.selectById(reportId);
        if (report == null) throw new RuntimeException("填报记录不存在");

        // 数据权限检查（最先执行，避免信息泄漏）
        if (UserContext.isWorkshopAdmin() && !report.getWorkshopId().equals(UserContext.getOrgId())) {
            throw new RuntimeException("无权审核其他车间的数据");
        }

        // 状态检查（在权限检查之后）
        if (!"已提交".equals(report.getStatus())) {
            throw new RuntimeException("只能审核已提交状态的数据");
        }

        report.setStatus("已审核");
        report.setUpdatedBy(UserContext.getUserId());
        reportMapper.updateById(report);

        // 创建审核记录
        createAuditRecord(report, "APPROVE", comment);
        log.info("审核通过: reportId={}, comment={}", reportId, comment);
        logService.record("车间审核", "APPROVE", String.valueOf(reportId), "审核通过: " + (comment != null ? comment : ""));
    }

    /**
     * 退回修改
     */
    @Transactional
    public void returnReport(Long reportId, String comment) {
        WorkReport report = reportMapper.selectById(reportId);
        if (report == null) throw new RuntimeException("填报记录不存在");

        // 数据权限检查（最先执行，避免信息泄漏）
        if (UserContext.isWorkshopAdmin() && !report.getWorkshopId().equals(UserContext.getOrgId())) {
            throw new RuntimeException("无权退回其他车间的数据");
        }

        // 状态检查（在权限检查之后）
        if (!"已提交".equals(report.getStatus())) {
            throw new RuntimeException("只能退回已提交状态的数据");
        }

        report.setStatus("已退回");
        report.setUpdatedBy(UserContext.getUserId());
        reportMapper.updateById(report);

        createAuditRecord(report, "RETURN", comment);
        log.info("退回修改: reportId={}, reason={}", reportId, comment);
        logService.record("车间审核", "RETURN", String.valueOf(reportId), "退回修改: " + (comment != null ? comment : ""));
    }

    /**
     * 创建审核记录
     */
    private void createAuditRecord(WorkReport report, String action, String comment) {
        AuditRecord audit = new AuditRecord();
        audit.setPeriodId(report.getPeriodId());
        audit.setReportId(report.getId());
        audit.setOrgId(report.getAreaId());
        audit.setAuditLevel("WORKSHOP");
        audit.setAction(action);
        audit.setComment(comment);
        audit.setOperatorId(UserContext.getUserId());
        auditRecordMapper.insert(audit);
    }

    /**
     * 查询审核记录
     */
    public IPage<AuditRecord> getAuditRecords(Integer pageNum, Integer pageSize, Long periodId) {
        Page<AuditRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<AuditRecord>()
                .eq(periodId != null, AuditRecord::getPeriodId, periodId)
                .orderByDesc(AuditRecord::getOperateTime);
        return auditRecordMapper.selectPage(page, wrapper);
    }
}
