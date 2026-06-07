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
    private final WorkItemMapper workItemMapper;
    private final EmployeeMapper employeeMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final MonthlyPeriodMapper periodMapper;
    private final OperationLogService logService;

    /**
     * 查询待审核的填报记录（仅"已提交"状态）
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
        List<WorkReport> reports = reportMapper.selectList(wrapper);
        reports.forEach(this::enrich);
        return reports;
    }

    /**
     * 查询所有状态的填报记录（已提交 + 已审核 + 已退回）
     * 车间管理员只能查看下属工区的数据，段级管理员可查看全部
     */
    public List<WorkReport> getAllReports(Long periodId, String status) {
        LambdaQueryWrapper<WorkReport> wrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(status != null && !status.isEmpty(), WorkReport::getStatus, status);

        // 车间管理员只能查看本车间下属工区的数据
        if (UserContext.isWorkshopAdmin()) {
            wrapper.eq(WorkReport::getWorkshopId, UserContext.getOrgId());
        }

        wrapper.orderByAsc(WorkReport::getAreaId, WorkReport::getWorkDate);
        List<WorkReport> reports = reportMapper.selectList(wrapper);
        reports.forEach(this::enrich);
        return reports;
    }

    /**
     * 分页查询所有状态的填报记录（已提交 + 已审核 + 已退回）
     * 手动分页：先 COUNT 再 LIMIT
     */
    public IPage<WorkReport> getAllReportsPage(Integer pageNum, Integer pageSize, Long periodId, String status, Long areaId) {
        LambdaQueryWrapper<WorkReport> countWrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(status != null && !status.isEmpty(), WorkReport::getStatus, status)
                .eq(areaId != null, WorkReport::getAreaId, areaId);

        if (UserContext.isWorkshopAdmin()) {
            countWrapper.eq(WorkReport::getWorkshopId, UserContext.getOrgId());
        }

        Long total = reportMapper.selectCount(countWrapper);

        LambdaQueryWrapper<WorkReport> dataWrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(status != null && !status.isEmpty(), WorkReport::getStatus, status)
                .eq(areaId != null, WorkReport::getAreaId, areaId);

        if (UserContext.isWorkshopAdmin()) {
            dataWrapper.eq(WorkReport::getWorkshopId, UserContext.getOrgId());
        }

        dataWrapper.orderByAsc(WorkReport::getAreaId, WorkReport::getWorkDate)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);
        List<WorkReport> records = reportMapper.selectList(dataWrapper);
        records.forEach(this::enrich);

        Page<WorkReport> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /** 为填报记录加载关联信息（人员姓名、工区名称、明细等） */
    private void enrich(WorkReport report) {
        Employee emp = employeeMapper.selectById(report.getEmployeeId());
        if (emp != null) report.setEmployeeName(emp.getName());
        OrgUnit area = orgUnitMapper.selectById(report.getAreaId());
        if (area != null) report.setAreaName(area.getOrgName());
        OrgUnit workshop = orgUnitMapper.selectById(report.getWorkshopId());
        if (workshop != null) report.setWorkshopName(workshop.getOrgName());
        List<WorkReportItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<WorkReportItem>()
                        .eq(WorkReportItem::getReportId, report.getId())
                        .orderByAsc(WorkReportItem::getSortOrder));
        for (WorkReportItem item : items) {
            WorkItem wi = workItemMapper.selectById(item.getWorkItemId());
            if (wi != null) {
                item.setItemName(wi.getItemName());
                item.setItemPath(wi.getItemPath());
            }
        }
        report.setItems(items);
    }

    /**
     * 批量审核通过
     */
    @Transactional
    public void batchApprove(List<Long> reportIds, String comment) {
        if (reportIds == null || reportIds.isEmpty()) {
            throw new RuntimeException("请选择要审核的记录");
        }
        for (Long id : reportIds) {
            approve(id, comment);
        }
        log.info("批量审核通过: count={}", reportIds.size());
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
     * 提交到段级：将本车间该月份所有"已审核"记录锁定，并更新月度期间状态
     */
    @Transactional
    public void submitToSection(Long periodId) {
        Long workshopId = UserContext.getOrgId();

        // 1. 检查是否还有待审核的记录
        Long pendingCount = reportMapper.selectCount(
                new LambdaQueryWrapper<WorkReport>()
                        .eq(WorkReport::getPeriodId, periodId)
                        .eq(WorkReport::getWorkshopId, workshopId)
                        .eq(WorkReport::getStatus, "已提交"));
        if (pendingCount > 0) {
            throw new RuntimeException("还有 " + pendingCount + " 条待审核记录，请先完成所有审核再提交");
        }

        // 2. 检查是否有已审核的记录
        Long approvedCount = reportMapper.selectCount(
                new LambdaQueryWrapper<WorkReport>()
                        .eq(WorkReport::getPeriodId, periodId)
                        .eq(WorkReport::getWorkshopId, workshopId)
                        .eq(WorkReport::getStatus, "已审核"));
        if (approvedCount == 0) {
            throw new RuntimeException("没有已审核的记录可提交");
        }

        // 3. 将所有"已审核"记录改为"已锁定"
        List<WorkReport> approvedReports = reportMapper.selectList(
                new LambdaQueryWrapper<WorkReport>()
                        .eq(WorkReport::getPeriodId, periodId)
                        .eq(WorkReport::getWorkshopId, workshopId)
                        .eq(WorkReport::getStatus, "已审核"));
        for (WorkReport report : approvedReports) {
            report.setStatus("已锁定");
            report.setUpdatedBy(UserContext.getUserId());
            reportMapper.updateById(report);
        }

        // 4. 更新月度期间状态
        MonthlyPeriod period = periodMapper.selectById(periodId);
        if (period != null) {
            period.setStatus("段级汇总中");
            periodMapper.updateById(period);
        }

        log.info("提交到段级: periodId={}, workshopId={}, lockedCount={}", periodId, workshopId, approvedReports.size());
        logService.record("车间审核", "SUBMIT_TO_SECTION", String.valueOf(periodId),
                "提交" + approvedReports.size() + "条记录到段级");
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
