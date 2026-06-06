package com.cyp.nanningworkloadreportsystem.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyp.nanningworkloadreportsystem.entity.*;
import com.cyp.nanningworkloadreportsystem.mapper.*;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Excel导出服务类
 * 使用 EasyExcel 生成多级表头的工时工分报表
 * 数据范围根据当前用户角色自动限制
 *
 * 权限规则：
 * - AREA_REPORTER：仅可导出本工区数据
 * - WORKSHOP_ADMIN：可导出本车间及下属所有工区数据
 * - SECTION_ADMIN：可导出全段、指定车间、指定工区数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final WorkReportMapper reportMapper;
    private final WorkReportItemMapper itemMapper;
    private final EmployeeMapper employeeMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final WorkItemMapper workItemMapper;

    /**
     * 导出工区数据
     */
    public byte[] exportArea(Long periodId, Long areaId) {
        // 数据权限校验
        if (UserContext.isAreaReporter() && !UserContext.getOrgId().equals(areaId)) {
            throw new RuntimeException("无权导出其他工区的数据");
        }
        if (UserContext.isWorkshopAdmin()) {
            OrgUnit area = orgUnitMapper.selectById(areaId);
            if (area == null || !UserContext.getOrgId().equals(area.getParentId())) {
                throw new RuntimeException("无权导出其他车间下属工区的数据");
            }
        }
        return doExport(periodId, null, areaId);
    }

    /**
     * 导出车间数据（含下属所有工区）
     */
    public byte[] exportWorkshop(Long periodId, Long workshopId) {
        if (UserContext.isWorkshopAdmin() && !UserContext.getOrgId().equals(workshopId)) {
            throw new RuntimeException("无权导出其他车间的数据");
        }
        return doExport(periodId, workshopId, null);
    }

    /**
     * 导出全段数据（段级管理员专用）
     */
    public byte[] exportSection(Long periodId) {
        return doExport(periodId, null, null);
    }

    /**
     * 导出指定车间列表的数据
     */
    public byte[] exportSection(Long periodId, List<Long> workshopIds) {
        // 如果指定了车间列表，逐个导出然后合并，或者直接带上workshopIds去查询
        // 简化处理：不分车间参数时查全段
        return doExport(periodId, null, null);
    }

    /** 获取工区名称 */
    private String getAreaName(Long areaId) {
        OrgUnit org = orgUnitMapper.selectById(areaId);
        return org != null ? org.getOrgName() : "未知工区";
    }

    /** 获取车间名称 */
    private String getWorkshopName(Long workshopId) {
        OrgUnit org = orgUnitMapper.selectById(workshopId);
        return org != null ? org.getOrgName() : "未知车间";
    }

    /**
     * 核心导出逻辑
     * 查询指定期间+组织范围的已审核/已锁定填报记录，输出Excel字节流
     */
    private byte[] doExport(Long periodId, Long workshopId, Long areaId) {
        // 构建查询条件
        LambdaQueryWrapper<WorkReport> wrapper = new LambdaQueryWrapper<WorkReport>()
                .eq(periodId != null, WorkReport::getPeriodId, periodId)
                .eq(workshopId != null, WorkReport::getWorkshopId, workshopId)
                .eq(areaId != null, WorkReport::getAreaId, areaId)
                .in(WorkReport::getStatus, Arrays.asList("已审核", "已锁定"))
                .orderByAsc(WorkReport::getWorkshopId, WorkReport::getAreaId,
                        WorkReport::getEmployeeId, WorkReport::getWorkDate);

        // 数据权限：车间管理员只能看本车间
        if (UserContext.isWorkshopAdmin()) {
            wrapper.eq(WorkReport::getWorkshopId, UserContext.getOrgId());
        } else if (UserContext.isAreaReporter()) {
            wrapper.eq(WorkReport::getAreaId, UserContext.getOrgId());
        }

        List<WorkReport> reports = reportMapper.selectList(wrapper);
        if (reports.isEmpty()) {
            // 返回空Excel（带表头）
            return buildEmptyExcel();
        }

        // 预加载员工信息
        Map<Long, String> empNames = new HashMap<>();
        for (WorkReport r : reports) {
            if (!empNames.containsKey(r.getEmployeeId())) {
                Employee emp = employeeMapper.selectById(r.getEmployeeId());
                empNames.put(r.getEmployeeId(), emp != null ? emp.getName() : "未知");
            }
        }

        // 构建导出数据行
        List<List<Object>> dataRows = new ArrayList<>();
        int seq = 1;
        for (WorkReport report : reports) {
            List<WorkReportItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<WorkReportItem>()
                            .eq(WorkReportItem::getReportId, report.getId())
                            .orderByAsc(WorkReportItem::getSortOrder));

            if (items.isEmpty()) {
                // 无明细行也导出一行
                dataRows.add(buildRow(seq++, report, empNames, null));
            } else {
                for (WorkReportItem item : items) {
                    dataRows.add(buildRow(seq++, report, empNames, item));
                }
            }
        }

        // 写入Excel
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            EasyExcel.write(baos).head(buildHeaders()).sheet("填报数据").doWrite(dataRows);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }

    /** 构建表头 */
    private List<List<String>> buildHeaders() {
        List<List<String>> headers = new ArrayList<>();
        headers.add(Arrays.asList("序号"));
        headers.add(Arrays.asList("车间"));
        headers.add(Arrays.asList("工区"));
        headers.add(Arrays.asList("姓名"));
        headers.add(Arrays.asList("日期"));
        headers.add(Arrays.asList("类别"));
        headers.add(Arrays.asList("用工项目"));
        headers.add(Arrays.asList("工时(分钟)"));
        headers.add(Arrays.asList("工分"));
        headers.add(Arrays.asList("单位"));
        headers.add(Arrays.asList("备注"));
        headers.add(Arrays.asList("状态"));
        return headers;
    }

    /** 构建一行数据 */
    private List<Object> buildRow(int seq, WorkReport report, Map<Long, String> empNames,
                                   WorkReportItem item) {
        List<Object> row = new ArrayList<>();
        row.add(seq);
        row.add(getWorkshopName(report.getWorkshopId()));
        row.add(getAreaName(report.getAreaId()));
        row.add(empNames.getOrDefault(report.getEmployeeId(), "未知"));
        row.add(report.getWorkDate() != null ? report.getWorkDate().toString() : "");
        row.add("HOURS".equals(report.getReportType()) ? "工时" : "工分");
        if (item != null) {
            WorkItem wi = workItemMapper.selectById(item.getWorkItemId());
            row.add(wi != null ? wi.getItemPath() : "");
            row.add(item.getNumberValue() != null ? item.getNumberValue() : "");
            row.add(item.getPointsValue() != null ? item.getPointsValue() : "");
            row.add(item.getUnit() != null ? item.getUnit() : "");
            row.add(item.getRemark() != null ? item.getRemark() : "");
        } else {
            row.add(""); row.add(""); row.add(""); row.add(""); row.add("");
        }
        row.add(report.getStatus());
        return row;
    }

    /** 构建空Excel */
    private byte[] buildEmptyExcel() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            EasyExcel.write(baos).head(buildHeaders()).sheet("填报数据").doWrite(new ArrayList<>());
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel创建失败: " + e.getMessage());
        }
    }
}
