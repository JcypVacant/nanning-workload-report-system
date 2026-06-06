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
import java.util.List;

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
     */
    public IPage<WorkReport> getPage(Integer pageNum, Integer pageSize, Long periodId,
                                      Long employeeId, String status, String reportType) {
        Page<WorkReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WorkReport> wrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(employeeId != null, WorkReport::getEmployeeId, employeeId)
                .eq(status != null, WorkReport::getStatus, status)
                .eq(reportType != null, WorkReport::getReportType, reportType);

        // 数据范围限制
        applyDataScope(wrapper);

        wrapper.orderByDesc(WorkReport::getWorkDate, WorkReport::getCreatedTime);
        IPage<WorkReport> result = reportMapper.selectPage(page, wrapper);

        // 加载关联信息
        result.getRecords().forEach(this::enrichReportInfo);

        return result;
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
     * 为填报记录加载关联信息（人员姓名、工区名称等）
     */
    private void enrichReportInfo(WorkReport report) {
        // 人员姓名
        Employee emp = employeeMapper.selectById(report.getEmployeeId());
        if (emp != null) report.setEmployeeName(emp.getName());

        // 工区名称
        OrgUnit area = orgUnitMapper.selectById(report.getAreaId());
        if (area != null) report.setAreaName(area.getOrgName());

        // 车间名称
        OrgUnit workshop = orgUnitMapper.selectById(report.getWorkshopId());
        if (workshop != null) report.setWorkshopName(workshop.getOrgName());

        // 加载明细
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
        // 1. 检查唯一性约束
        Long exists = reportMapper.selectCount(
                new LambdaQueryWrapper<WorkReport>()
                        .eq(WorkReport::getPeriodId, periodId)
                        .eq(WorkReport::getEmployeeId, employeeId)
                        .eq(WorkReport::getWorkDate, workDate)
                        .eq(WorkReport::getReportType, reportType));
        if (exists > 0) {
            throw new RuntimeException("该人员在该日期已存在" + ("HOURS".equals(reportType) ? "工时" : "工分") + "填报记录，请勿重复创建");
        }

        // 2. 获取人员信息以确定车间和工区
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
}
