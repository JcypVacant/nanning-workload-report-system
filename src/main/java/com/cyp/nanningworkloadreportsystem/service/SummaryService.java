package com.cyp.nanningworkloadreportsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyp.nanningworkloadreportsystem.dto.SectionSummaryDTO;
import com.cyp.nanningworkloadreportsystem.dto.WorkshopSummaryDTO;
import com.cyp.nanningworkloadreportsystem.entity.Employee;
import com.cyp.nanningworkloadreportsystem.entity.OrgUnit;
import com.cyp.nanningworkloadreportsystem.entity.WorkReport;
import com.cyp.nanningworkloadreportsystem.mapper.EmployeeMapper;
import com.cyp.nanningworkloadreportsystem.mapper.OrgUnitMapper;
import com.cyp.nanningworkloadreportsystem.mapper.WorkReportMapper;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 汇总统计服务类
 * 负责车间级和段级的工时工分汇总查询
 * 仅统计状态为"已审核"或"已锁定"的填报数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryService {

    private final WorkReportMapper workReportMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final EmployeeMapper employeeMapper;

    /**
     * 车间汇总：按工区分组统计
     * 车间管理员只能查看本车间数据，段级管理员可查看任意车间
     *
     * @param periodId   月度期间ID（必填）
     * @param workshopId 车间ID（车间管理员忽略此参数，自动取本车间；段级管理员可指定）
     */
    public List<WorkshopSummaryDTO> getWorkshopSummary(Long periodId, Long workshopId) {
        // 数据权限：车间管理员只能看自己的车间
        if (UserContext.isWorkshopAdmin()) {
            workshopId = UserContext.getOrgId();
        }

        // 如果指定了车间则按车间过滤，否则查询所有工区（段级管理员视角）
        List<Map<String, Object>> rows;
        if (workshopId != null) {
            rows = workReportMapper.summaryByArea(periodId, workshopId);
        } else {
            rows = workReportMapper.summaryAllAreas(periodId);
        }
        final Long finalWorkshopId = workshopId;  // 可能为null，供lambda使用
        return rows.stream().map(row -> {
            WorkshopSummaryDTO dto = new WorkshopSummaryDTO();
            dto.setAreaId(toLong(row.get("area_id")));
            dto.setAreaName((String) row.get("area_name"));
            dto.setWorkshopId(finalWorkshopId != null ? finalWorkshopId : toLong(row.get("workshop_id")));
            dto.setWorkshopName((String) row.get("workshop_name"));
            dto.setReportCount(toLong(row.get("report_count")));
            dto.setEmployeeCount(toLong(row.get("employee_count")));
            dto.setTotalMinutes(toBigDecimal(row.get("total_minutes")));
            dto.setTotalPoints(toBigDecimal(row.get("total_points")));
            dto.setConstructionMinutes(toBigDecimal(row.get("construction_minutes")));
            dto.setCooperationMinutes(toBigDecimal(row.get("cooperation_minutes")));
            dto.setMaintenanceMinutes(toBigDecimal(row.get("maintenance_minutes")));
            dto.setOtherMinutes(toBigDecimal(row.get("other_minutes")));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 段级汇总：按车间分组统计（仅段级管理员可用）
     *
     * @param periodId 月度期间ID（必填）
     */
    public List<SectionSummaryDTO> getSectionSummary(Long periodId) {
        List<Map<String, Object>> rows = workReportMapper.summaryByWorkshop(periodId);

        // 查询所有工区明细，按车间分组
        List<Map<String, Object>> allAreas = workReportMapper.summaryAllAreas(periodId);
        Map<Long, List<WorkshopSummaryDTO>> areaMap = allAreas.stream()
                .map(row -> {
                    WorkshopSummaryDTO dto = new WorkshopSummaryDTO();
                    dto.setAreaId(toLong(row.get("area_id")));
                    dto.setAreaName((String) row.get("area_name"));
                    dto.setWorkshopId(toLong(row.get("workshop_id")));
                    dto.setWorkshopName((String) row.get("workshop_name"));
                    dto.setReportCount(toLong(row.get("report_count")));
                    dto.setEmployeeCount(toLong(row.get("employee_count")));
                    dto.setTotalMinutes(toBigDecimal(row.get("total_minutes")));
                    dto.setTotalPoints(toBigDecimal(row.get("total_points")));
                    dto.setConstructionMinutes(toBigDecimal(row.get("construction_minutes")));
                    dto.setCooperationMinutes(toBigDecimal(row.get("cooperation_minutes")));
                    dto.setMaintenanceMinutes(toBigDecimal(row.get("maintenance_minutes")));
                    dto.setOtherMinutes(toBigDecimal(row.get("other_minutes")));
                    return dto;
                })
                .collect(Collectors.groupingBy(WorkshopSummaryDTO::getWorkshopId));

        List<SectionSummaryDTO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            SectionSummaryDTO dto = new SectionSummaryDTO();
            Long wsId = toLong(row.get("workshop_id"));
            dto.setWorkshopId(wsId);
            dto.setWorkshopName((String) row.get("workshop_name"));
            dto.setAreaCount(toLong(row.get("area_count")));
            dto.setReportCount(toLong(row.get("report_count")));
            dto.setEmployeeCount(toLong(row.get("employee_count")));
            dto.setTotalMinutes(toBigDecimal(row.get("total_minutes")));
            dto.setTotalPoints(toBigDecimal(row.get("total_points")));
            dto.setConstructionMinutes(toBigDecimal(row.get("construction_minutes")));
            dto.setCooperationMinutes(toBigDecimal(row.get("cooperation_minutes")));
            dto.setMaintenanceMinutes(toBigDecimal(row.get("maintenance_minutes")));
            dto.setOtherMinutes(toBigDecimal(row.get("other_minutes")));
            dto.setAreas(areaMap.getOrDefault(wsId, new ArrayList<>()));
            result.add(dto);
        }
        return result;
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    /** 各车间填报进度 */
    public List<Map<String, Object>> getProgress(Long periodId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<OrgUnit> workshops = orgUnitMapper.selectList(
                new LambdaQueryWrapper<OrgUnit>().eq(OrgUnit::getOrgType, "WORKSHOP").eq(OrgUnit::getEnabled, true));
        for (OrgUnit ws : workshops) {
            // 该车间总人数
            Long totalEmp = employeeMapper.selectCount(
                    new LambdaQueryWrapper<Employee>().eq(Employee::getWorkshopId, ws.getId()).eq(Employee::getEnabled, 1));
            // 已提交人数
            Long submitted = workReportMapper.selectCount(
                    new LambdaQueryWrapper<WorkReport>().eq(WorkReport::getPeriodId, periodId)
                            .eq(WorkReport::getWorkshopId, ws.getId())
                            .in(WorkReport::getStatus, Arrays.asList("已提交", "已审核", "已锁定")));
            // 已审核人数
            Long approved = workReportMapper.selectCount(
                    new LambdaQueryWrapper<WorkReport>().eq(WorkReport::getPeriodId, periodId)
                            .eq(WorkReport::getWorkshopId, ws.getId())
                            .in(WorkReport::getStatus, Arrays.asList("已审核", "已锁定")));

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("workshopId", ws.getId());
            item.put("workshopName", ws.getOrgName());
            item.put("totalEmployees", totalEmp);
            item.put("submitted", submitted);
            item.put("approved", approved);
            item.put("submittedPercent", totalEmp > 0 ? Math.round(submitted * 100.0 / totalEmp) : 0);
            item.put("approvedPercent", totalEmp > 0 ? Math.round(approved * 100.0 / totalEmp) : 0);
            result.add(item);
        }
        return result;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        return new BigDecimal(val.toString());
    }
}
