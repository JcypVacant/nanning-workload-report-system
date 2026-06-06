package com.cyp.nanningworkloadreportsystem.service;

import com.cyp.nanningworkloadreportsystem.mapper.WorkReportMapper;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 统计分析服务类
 * 提供多维度实时聚合统计数据（按车间/工区/人员/项目/状态等）
 * 仅统计状态为"已审核"或"已锁定"的填报数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final WorkReportMapper workReportMapper;

    /**
     * 按车间统计工时工分
     */
    public List<Map<String, Object>> getByWorkshop(Long periodId) {
        List<Map<String, Object>> rows = workReportMapper.statsByWorkshop(periodId,
                UserContext.isWorkshopAdmin() ? UserContext.getOrgId() : null,
                UserContext.isAreaReporter() ? UserContext.getOrgId() : null);
        return formatRows(rows);
    }

    /**
     * 按工区统计工时工分
     */
    public List<Map<String, Object>> getByArea(Long periodId, Long workshopId) {
        if (UserContext.isWorkshopAdmin()) workshopId = UserContext.getOrgId();
        if (UserContext.isAreaReporter()) workshopId = null; // 工区只看自己
        List<Map<String, Object>> rows = workReportMapper.statsByArea(periodId, workshopId,
                UserContext.isAreaReporter() ? UserContext.getOrgId() : null);
        return formatRows(rows);
    }

    /**
     * 按人员排名统计（TOP 20）
     */
    public List<Map<String, Object>> getByEmployee(Long periodId) {
        List<Map<String, Object>> rows = workReportMapper.statsByEmployee(periodId,
                UserContext.isWorkshopAdmin() ? UserContext.getOrgId() : null,
                UserContext.isAreaReporter() ? UserContext.getOrgId() : null);
        return formatRows(rows);
    }

    /**
     * 按用工项目顶级分类统计（占比）
     */
    public List<Map<String, Object>> getByProject(Long periodId) {
        List<Map<String, Object>> rows = workReportMapper.statsByProject(periodId,
                UserContext.isWorkshopAdmin() ? UserContext.getOrgId() : null,
                UserContext.isAreaReporter() ? UserContext.getOrgId() : null);
        return formatRows(rows);
    }

    /**
     * 按月趋势统计
     */
    public Map<String, Object> getTrend() {
        List<Map<String, Object>> rows = workReportMapper.statsByMonth(
                UserContext.isWorkshopAdmin() ? UserContext.getOrgId() : null,
                UserContext.isAreaReporter() ? UserContext.getOrgId() : null);
        List<String> months = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<Number> points = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            months.add((String) row.get("month"));
            hours.add((Number) row.getOrDefault("total_minutes", 0));
            points.add((Number) row.getOrDefault("total_points", 0));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("months", months);
        result.put("hours", hours);
        result.put("points", points);
        return result;
    }

    /**
     * 按填报状态统计数量
     */
    public Map<String, Object> getStatusStats(Long periodId) {
        List<Map<String, Object>> rows = workReportMapper.statsByStatus(periodId,
                UserContext.isWorkshopAdmin() ? UserContext.getOrgId() : null,
                UserContext.isAreaReporter() ? UserContext.getOrgId() : null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("draft", 0L);
        result.put("submitted", 0L);
        result.put("returned", 0L);
        result.put("approved", 0L);
        result.put("locked", 0L);
        for (Map<String, Object> row : rows) {
            String status = (String) row.get("status");
            Object cnt = row.get("cnt");
            long count = cnt instanceof Number ? ((Number) cnt).longValue() : 0L;
            switch (status) {
                case "草稿": result.put("draft", count); break;
                case "已提交": result.put("submitted", count); break;
                case "已退回": result.put("returned", count); break;
                case "已审核": result.put("approved", count); break;
                case "已锁定": result.put("locked", count); break;
            }
        }
        return result;
    }

    /** 统一格式化：将数值转为合适的类型 */
    private List<Map<String, Object>> formatRows(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            // 确保数值字段是 BigDecimal 或合适的类型
            for (String key : new String[]{"total_minutes", "total_points", "hours", "points"}) {
                Object val = row.get(key);
                if (val instanceof BigDecimal) {
                    row.put(key, ((BigDecimal) val).doubleValue());
                }
            }
        }
        return rows;
    }
}
