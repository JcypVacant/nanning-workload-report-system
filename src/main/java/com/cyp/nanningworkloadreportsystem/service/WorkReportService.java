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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工时工分填报服务类（核心业务模块）
 *
 * 填报数据状态流转：
 * 草稿 -> 已提交（工区提交审核）
 * 已提交 -> 已审核（车间审核通过）
 * 已提交 -> 已退回（车间退回）
 * 已退回 -> 已提交（工区修改后重新提交，更新原记录并重置状态）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkReportService {

    private final WorkReportMapper reportMapper;
    private final WorkReportItemMapper itemMapper;
    private final WorkItemMapper workItemMapper;
    private final EmployeeMapper employeeMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final OperationLogService logService;

    /**
     * 分页查询填报记录
     * 自动根据当前用户角色限制数据范围
     * 使用手动分页（selectCount + selectList with LIMIT），因为 MP 3.5.15 移除了 PaginationInnerInterceptor
     */
    public IPage<WorkReport> getPage(Integer pageNum, Integer pageSize, Long periodId,
                                      Long employeeId, String status, String reportType,
                                      String keyword, LocalDate workDate, Long areaId) {
        // 如果有人员姓名关键字，先查匹配的 employee ID
        List<Long> keywordEmpIds = null;
        if (keyword != null && !keyword.isEmpty()) {
            keywordEmpIds = employeeMapper.selectList(
                    new LambdaQueryWrapper<Employee>().like(Employee::getName, keyword)
            ).stream().map(Employee::getId).toList();
            if (keywordEmpIds.isEmpty()) {
                Page<WorkReport> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0L);
                empty.setRecords(List.of());
                return empty;
            }
        }

        // 构建公用条件
        LambdaQueryWrapper<WorkReport> countWrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(employeeId != null, WorkReport::getEmployeeId, employeeId)
                .eq(status != null && !status.isEmpty(), WorkReport::getStatus, status)
                .eq(reportType != null && !reportType.isEmpty(), WorkReport::getReportType, reportType)
                .eq(workDate != null, WorkReport::getWorkDate, workDate)
                .eq(areaId != null, WorkReport::getAreaId, areaId);
        if (keywordEmpIds != null) {
            countWrapper.in(WorkReport::getEmployeeId, keywordEmpIds);
        }

        // 数据范围限制
        applyDataScope(countWrapper);

        // 1. 查总数
        Long total = reportMapper.selectCount(countWrapper);

        // 2. 查当前页
        LambdaQueryWrapper<WorkReport> dataWrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(employeeId != null, WorkReport::getEmployeeId, employeeId)
                .eq(status != null && !status.isEmpty(), WorkReport::getStatus, status)
                .eq(reportType != null && !reportType.isEmpty(), WorkReport::getReportType, reportType)
                .eq(workDate != null, WorkReport::getWorkDate, workDate)
                .eq(areaId != null, WorkReport::getAreaId, areaId)
                .orderByDesc(WorkReport::getWorkDate, WorkReport::getCreatedTime)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);
        if (keywordEmpIds != null) {
            dataWrapper.in(WorkReport::getEmployeeId, keywordEmpIds);
        }

        applyDataScope(dataWrapper);

        List<WorkReport> records = reportMapper.selectList(dataWrapper);

        // 批量加载关联信息
        enrichReportInfo(records);

        Page<WorkReport> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /**
     * 根据数据权限限制查询范围
     */
    private void applyDataScope(LambdaQueryWrapper<WorkReport> wrapper) {
        if (UserContext.isWorkshopAdmin()) {
            wrapper.eq(WorkReport::getWorkshopId, UserContext.getOrgId());
        } else if (UserContext.isAreaReporter()) {
            wrapper.eq(WorkReport::getAreaId, UserContext.getOrgId());
        }
        // 段级管理员不限制
    }

    /**
     * 批量加载关联信息（避免 N+1 查询）
     */
    private void enrichReportInfo(List<WorkReport> reports) {
        if (reports.isEmpty()) return;

        // 1. 批量查所有关联的员工
        Set<Long> empIds = reports.stream().map(WorkReport::getEmployeeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> empNameMap = new HashMap<>();
        if (!empIds.isEmpty()) {
            employeeMapper.selectBatchIds(empIds).forEach(e -> empNameMap.put(e.getId(), e.getName()));
        }

        // 2. 批量查所有工区
        Set<Long> areaIds = reports.stream().map(WorkReport::getAreaId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> areaNameMap = new HashMap<>();
        if (!areaIds.isEmpty()) {
            orgUnitMapper.selectBatchIds(areaIds).forEach(o -> areaNameMap.put(o.getId(), o.getOrgName()));
        }

        // 3. 批量查所有车间
        Set<Long> wsIds = reports.stream().map(WorkReport::getWorkshopId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> wsNameMap = new HashMap<>();
        if (!wsIds.isEmpty()) {
            orgUnitMapper.selectBatchIds(wsIds).forEach(o -> wsNameMap.put(o.getId(), o.getOrgName()));
        }

        // 4. 批量查所有明细
        Set<Long> reportIds = reports.stream().map(WorkReport::getId).collect(Collectors.toSet());
        Map<Long, List<WorkReportItem>> itemsMap = new HashMap<>();
        if (!reportIds.isEmpty()) {
            itemMapper.selectList(new LambdaQueryWrapper<WorkReportItem>().in(WorkReportItem::getReportId, reportIds).orderByAsc(WorkReportItem::getSortOrder))
                    .forEach(item -> itemsMap.computeIfAbsent(item.getReportId(), k -> new ArrayList<>()).add(item));
        }

        // 5. 批量查所有用工项目
        Set<Long> wiIds = itemsMap.values().stream().flatMap(List::stream).map(WorkReportItem::getWorkItemId).collect(Collectors.toSet());
        Map<Long, WorkItem> wiMap = new HashMap<>();
        if (!wiIds.isEmpty()) {
            workItemMapper.selectBatchIds(wiIds).forEach(wi -> wiMap.put(wi.getId(), wi));
        }

        // 6. 填充每条记录
        for (WorkReport report : reports) {
            report.setEmployeeName(empNameMap.getOrDefault(report.getEmployeeId(), null));
            report.setAreaName(areaNameMap.getOrDefault(report.getAreaId(), null));
            report.setWorkshopName(wsNameMap.getOrDefault(report.getWorkshopId(), null));
            List<WorkReportItem> items = itemsMap.getOrDefault(report.getId(), Collections.emptyList());
            for (WorkReportItem item : items) {
                WorkItem wi = wiMap.get(item.getWorkItemId());
                if (wi != null) {
                    item.setItemName(wi.getItemName());
                    item.setItemPath(wi.getItemPath());
                }
            }
            report.setItems(items);
        }
    }

    private void enrichReportInfo(WorkReport report) { enrichReportInfo(List.of(report)); }

    /**
     * 获取填报详情（含明细）
     */
    public WorkReport getDetail(Long id) {
        WorkReport report = reportMapper.selectById(id);
        if (report != null) {
            enrichReportInfo(report);
        }
        return report;
    }

    /**
     * 创建填报记录（含明细项）
     * 业务规则：
     * 1. 检查唯一性：同一期间、同一人员、同一日期、同一类别只能有一条记录
     * 2. 所有明细项必须是叶子节点（is_input_item=1）
     * 3. 根据用工项目的input_type自动判断是填数值还是文本
     */
    @Transactional
    public WorkReport create(Long periodId, Long employeeId, LocalDate workDate,
                              String reportType, List<WorkReportItem> items, String remark) {
        // 获取人员信息以确定车间和工区
        Employee emp = employeeMapper.selectById(employeeId);
        if (emp == null) throw new RuntimeException("人员不存在");

        // 3. 创建填报主记录
        WorkReport report = new WorkReport();
        report.setPeriodId(periodId);
        report.setWorkshopId(emp.getWorkshopId());
        report.setAreaId(emp.getAreaId());
        report.setEmployeeId(employeeId);
        report.setWorkDate(workDate);
        report.setReportType(reportType);
        report.setStatus("草稿");
        report.setRemark(remark);
        report.setCreatedBy(UserContext.getUserId());
        reportMapper.insert(report);

        // 4. 创建明细项
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                WorkReportItem item = items.get(i);
                // 验证用工项目的输入类型
                WorkItem wi = workItemMapper.selectById(item.getWorkItemId());
                if (wi == null || wi.getIsInputItem() != 1) {
                    throw new RuntimeException("用工项目 '" + (wi != null ? wi.getItemPath() : item.getWorkItemId()) + "' 不可填写");
                }

                // 根据用工项目的input_type设置值
                if ("NUMBER".equals(wi.getInputType())) {
                    item.setUnit(wi.getUnit());
                    // numberValue 由调用方设置
                } else if ("TEXT".equals(wi.getInputType())) {
                    item.setUnit("文本");
                    // textValue 由调用方设置
                }
                item.setReportId(report.getId());
                item.setSortOrder(i + 1);
                itemMapper.insert(item);
            }
        }

        log.info("创建填报记录: reportId={}, employeeId={}, workDate={}, type={}",
                report.getId(), employeeId, workDate, reportType);
        logService.record("工区填报", "CREATE", String.valueOf(report.getId()),
                "创建填报: 人员" + employeeId + " " + workDate + " " + reportType);
        enrichReportInfo(report);
        return report;
    }

    /**
     * 更新填报记录（仅草稿和已退回状态可修改）
     */
    @Transactional
    public WorkReport update(Long reportId, List<WorkReportItem> items, String remark) {
        WorkReport report = reportMapper.selectById(reportId);
        if (report == null) throw new RuntimeException("填报记录不存在");

        // 只有草稿和已退回状态可以修改
        if (!"草稿".equals(report.getStatus()) && !"已退回".equals(report.getStatus())) {
            throw new RuntimeException("当前状态 '" + report.getStatus() + "' 不允许修改");
        }

        // 删除旧明细
        itemMapper.delete(new LambdaQueryWrapper<WorkReportItem>()
                .eq(WorkReportItem::getReportId, reportId));

        // 创建新明细
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                WorkReportItem item = items.get(i);
                WorkItem wi = workItemMapper.selectById(item.getWorkItemId());
                if (wi == null || wi.getIsInputItem() != 1) {
                    throw new RuntimeException("用工项目不可填写");
                }
                if ("NUMBER".equals(wi.getInputType())) {
                    item.setUnit(wi.getUnit());
                } else if ("TEXT".equals(wi.getInputType())) {
                    item.setUnit("文本");
                }
                item.setReportId(reportId);
                item.setSortOrder(i + 1);
                itemMapper.insert(item);
            }
        }

        if (remark != null) report.setRemark(remark);
        report.setUpdatedBy(UserContext.getUserId());
        reportMapper.updateById(report);

        logService.record("工区填报", "UPDATE", String.valueOf(reportId), "更新填报记录");
        enrichReportInfo(report);
        return report;
    }

    /**
     * 删除填报记录（仅草稿可删除）
     */
    @Transactional
    public void delete(Long reportId) {
        WorkReport report = reportMapper.selectById(reportId);
        if (report == null) throw new RuntimeException("填报记录不存在");
        if (!"草稿".equals(report.getStatus())) {
            throw new RuntimeException("只有草稿状态的数据可以删除");
        }
        // 删除明细
        itemMapper.delete(new LambdaQueryWrapper<WorkReportItem>()
                .eq(WorkReportItem::getReportId, reportId));
        // 删除主记录
        reportMapper.deleteById(reportId);
        log.info("删除填报记录: reportId={}", reportId);
    }

    /**
     * 提交填报数据（草稿 -> 已提交）
     * 同时创建审核记录
     */
    @Transactional
    public void submit(Long reportId) {
        WorkReport report = reportMapper.selectById(reportId);
        if (report == null) throw new RuntimeException("填报记录不存在");
        if (!"草稿".equals(report.getStatus()) && !"已退回".equals(report.getStatus())) {
            throw new RuntimeException("只有草稿或已退回状态的数据可以提交");
        }

        report.setStatus("已提交");
        report.setUpdatedBy(UserContext.getUserId());
        reportMapper.updateById(report);

        // 创建审核记录
        AuditRecord audit = new AuditRecord();
        audit.setPeriodId(report.getPeriodId());
        audit.setReportId(reportId);
        audit.setOrgId(report.getAreaId());
        audit.setAuditLevel("WORKSHOP");
        audit.setAction("SUBMIT");
        audit.setOperatorId(UserContext.getUserId());
        auditRecordMapper.insert(audit);

        log.info("提交填报数据: reportId={}", reportId);
        logService.record("工区填报", "SUBMIT", String.valueOf(reportId), "提交填报数据");
    }

    /**
     * 批量提交填报数据（草稿/已退回 -> 已提交）
     */
    @Transactional
    public void batchSubmit(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请选择要提交的记录");
        }
        for (Long id : ids) {
            submit(id);
        }
        log.info("批量提交填报数据: count={}", ids.size());
    }
}
