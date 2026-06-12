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

import java.util.*;
import java.util.stream.Collectors;

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
    private final SysUserMapper sysUserMapper;
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

        dataWrapper.orderByAsc(WorkReport::getAreaId).orderByDesc(WorkReport::getWorkDate)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);
        List<WorkReport> records = reportMapper.selectList(dataWrapper);
        enrich(records);

        Page<WorkReport> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /** 批量加载关联信息（避免 N+1 查询） */
    private void enrich(List<WorkReport> reports) {
        if (reports.isEmpty()) return;
        Set<Long> empIds = new HashSet<>();
        Set<Long> areaIds = new HashSet<>();
        Set<Long> wsIds = new HashSet<>();
        Set<Long> reportIds = new HashSet<>();
        for (WorkReport r : reports) {
            if (r.getEmployeeId() != null) empIds.add(r.getEmployeeId());
            if (r.getAreaId() != null) areaIds.add(r.getAreaId());
            if (r.getWorkshopId() != null) wsIds.add(r.getWorkshopId());
            reportIds.add(r.getId());
        }

        Map<Long, String> empNameMap = new HashMap<>();
        if (!empIds.isEmpty()) employeeMapper.selectBatchIds(empIds).forEach(e -> empNameMap.put(e.getId(), e.getName()));
        Map<Long, String> areaNameMap = new HashMap<>();
        if (!areaIds.isEmpty()) orgUnitMapper.selectBatchIds(areaIds).forEach(o -> areaNameMap.put(o.getId(), o.getOrgName()));
        Map<Long, String> wsNameMap = new HashMap<>();
        if (!wsIds.isEmpty()) orgUnitMapper.selectBatchIds(wsIds).forEach(o -> wsNameMap.put(o.getId(), o.getOrgName()));

        Map<Long, List<WorkReportItem>> itemsMap = new HashMap<>();
        if (!reportIds.isEmpty()) {
            itemMapper.selectList(new LambdaQueryWrapper<WorkReportItem>().in(WorkReportItem::getReportId, reportIds).orderByAsc(WorkReportItem::getSortOrder))
                    .forEach(item -> itemsMap.computeIfAbsent(item.getReportId(), k -> new ArrayList<>()).add(item));
        }
        Set<Long> wiIds = itemsMap.values().stream().flatMap(List::stream).map(WorkReportItem::getWorkItemId).collect(Collectors.toSet());
        Map<Long, WorkItem> wiMap = new HashMap<>();
        if (!wiIds.isEmpty()) workItemMapper.selectBatchIds(wiIds).forEach(wi -> wiMap.put(wi.getId(), wi));

        for (WorkReport report : reports) {
            report.setEmployeeName(empNameMap.getOrDefault(report.getEmployeeId(), null));
            report.setAreaName(areaNameMap.getOrDefault(report.getAreaId(), null));
            report.setWorkshopName(wsNameMap.getOrDefault(report.getWorkshopId(), null));
            List<WorkReportItem> items = itemsMap.getOrDefault(report.getId(), Collections.emptyList());
            for (WorkReportItem item : items) {
                WorkItem wi = wiMap.get(item.getWorkItemId());
                if (wi != null) { item.setItemName(wi.getItemName()); item.setItemPath(wi.getItemPath()); }
            }
            report.setItems(items);
        }
    }

    private void enrich(WorkReport report) { enrich(List.of(report)); }

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

        // 数据权限检查（车间管理员只能审本车间，段级管理员可审全部）
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
        String level = UserContext.isSectionAdmin() ? "SECTION" : "WORKSHOP";
        AuditRecord audit = new AuditRecord();
        audit.setPeriodId(report.getPeriodId());
        audit.setReportId(report.getId());
        audit.setOrgId(report.getAreaId());
        audit.setAuditLevel(level);
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

    /** 段级审核：查询车间本级提交的记录（不含工区数据） */
    public IPage<WorkReport> getSectionPending(Integer pageNum, Integer pageSize, Long periodId, Long workshopId, String status) {
        // 查询所有"XX车间本级"的工区ID
        List<Long> workshopLevelAreaIds = orgUnitMapper.selectList(
                new LambdaQueryWrapper<OrgUnit>().eq(OrgUnit::getOrgType, "WORKSHOP_LEVEL")
        ).stream().map(OrgUnit::getId).toList();

        // 只查车间本级：areaId 为空 或 areaId 属于 WORKSHOP_LEVEL 类型工区
        LambdaQueryWrapper<WorkReport> countWrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(status != null && !status.isEmpty(), WorkReport::getStatus, status)
                .eq(workshopId != null, WorkReport::getWorkshopId, workshopId)
                .and(w -> w.isNull(WorkReport::getAreaId).or().in(WorkReport::getAreaId, workshopLevelAreaIds));
        Long total = reportMapper.selectCount(countWrapper);

        LambdaQueryWrapper<WorkReport> dataWrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(status != null && !status.isEmpty(), WorkReport::getStatus, status)
                .eq(workshopId != null, WorkReport::getWorkshopId, workshopId)
                .and(w -> w.isNull(WorkReport::getAreaId).or().in(WorkReport::getAreaId, workshopLevelAreaIds))
                .orderByAsc(WorkReport::getWorkshopId).orderByAsc(WorkReport::getAreaId).orderByDesc(WorkReport::getWorkDate)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);
        List<WorkReport> records = reportMapper.selectList(dataWrapper);
        enrich(records);

        Page<WorkReport> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /** 批量退回（与批量通过对称） */
    @Transactional
    public void batchReturn(List<Long> reportIds, String comment) {
        if (reportIds == null || reportIds.isEmpty()) {
            throw new RuntimeException("请选择要退回的记录");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new RuntimeException("批量退回必须填写退回原因");
        }
        for (Long id : reportIds) {
            returnReport(id, comment);
        }
        log.info("批量退回: count={}", reportIds.size());
    }

    /** 查询未填报的人员（该车间该月份没有任何填报记录的人员） */
    public List<Map<String, Object>> getUnsubmitted(Long periodId) {
        Long workshopId = UserContext.getOrgId();
        // 查询该车间下所有在岗人员
        List<Employee> allEmps = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>()
                        .eq(Employee::getWorkshopId, workshopId)
                        .eq(Employee::getEnabled, 1)
                        .eq(Employee::getEmployeeStatus, "在岗"));
        // 查询该月份有填报记录的人员ID
        List<Long> reportedEmpIds = reportMapper.selectList(
                new LambdaQueryWrapper<WorkReport>()
                        .select(WorkReport::getEmployeeId)
                        .eq(WorkReport::getPeriodId, periodId)
                        .eq(WorkReport::getWorkshopId, workshopId)
                        .groupBy(WorkReport::getEmployeeId)
        ).stream().map(WorkReport::getEmployeeId).distinct().toList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Employee emp : allEmps) {
            if (!reportedEmpIds.contains(emp.getId())) {
                Map<String, Object> item = new HashMap<>();
                item.put("employeeId", emp.getId());
                item.put("employeeName", emp.getName());
                if (emp.getAreaId() != null) {
                    OrgUnit area = orgUnitMapper.selectById(emp.getAreaId());
                    item.put("areaName", area != null ? area.getOrgName() : "");
                } else {
                    item.put("areaName", "车间本级");
                }
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 查询审核记录
     */
    public IPage<AuditRecord> getAuditRecords(Integer pageNum, Integer pageSize, Long periodId, String action, Long areaId) {
        // 构建条件
        LambdaQueryWrapper<AuditRecord> countWrapper = new LambdaQueryWrapper<AuditRecord>()
                .eq(periodId != null, AuditRecord::getPeriodId, periodId)
                .eq(action != null && !action.isEmpty(), AuditRecord::getAction, action)
                .eq(areaId != null, AuditRecord::getOrgId, areaId);

        // 数据范围：车间管理员只看本车间的审核记录
        if (UserContext.isWorkshopAdmin()) {
            // audit_record.orgId 是工区ID，需要筛选属于本车间的工区
            List<OrgUnit> areas = orgUnitMapper.selectList(
                    new LambdaQueryWrapper<OrgUnit>().eq(OrgUnit::getParentId, UserContext.getOrgId()));
            List<Long> areaIds = areas.stream().map(OrgUnit::getId).toList();
            if (areaIds.isEmpty()) {
                Page<AuditRecord> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0L);
                empty.setRecords(List.of());
                return empty;
            }
            countWrapper.in(AuditRecord::getOrgId, areaIds);
        }

        Long total = auditRecordMapper.selectCount(countWrapper);

        LambdaQueryWrapper<AuditRecord> dataWrapper = new LambdaQueryWrapper<AuditRecord>()
                .eq(periodId != null, AuditRecord::getPeriodId, periodId)
                .eq(action != null && !action.isEmpty(), AuditRecord::getAction, action)
                .eq(areaId != null, AuditRecord::getOrgId, areaId);
        if (UserContext.isWorkshopAdmin()) {
            List<OrgUnit> areas = orgUnitMapper.selectList(
                    new LambdaQueryWrapper<OrgUnit>().eq(OrgUnit::getParentId, UserContext.getOrgId()));
            List<Long> areaIds = areas.stream().map(OrgUnit::getId).toList();
            if (!areaIds.isEmpty()) {
                dataWrapper.in(AuditRecord::getOrgId, areaIds);
            }
        }
        dataWrapper.orderByDesc(AuditRecord::getOperateTime)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);
        List<AuditRecord> records = auditRecordMapper.selectList(dataWrapper);

        // 填充操作人姓名
        for (AuditRecord record : records) {
            if (record.getOperatorId() != null) {
                SysUser user = sysUserMapper.selectById(record.getOperatorId());
                if (user != null) record.setOperatorName(user.getRealName());
            }
        }

        Page<AuditRecord> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }
}
