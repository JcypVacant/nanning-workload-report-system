package com.cyp.nanningworkloadreportsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyp.nanningworkloadreportsystem.entity.WorkReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 工时工分填报主表 Mapper
 */
@Mapper
public interface WorkReportMapper extends BaseMapper<WorkReport> {

    /**
     * 按工区分组汇总（车间汇总用）
     * 统计指定期间、指定车间下各工区的工时工分合计
     * 仅统计状态为"已审核"或"已锁定"的数据
     */
    @Select("SELECT wr.area_id, ou.org_name AS area_name, " +
            "       ou2.org_name AS workshop_name, " +
            "       COUNT(DISTINCT wr.id) AS report_count, " +
            "       COUNT(DISTINCT wr.employee_id) AS employee_count, " +
            "       COALESCE(SUM(wri.number_value), 0) AS total_minutes, " +
            "       COALESCE(SUM(wri.points_value), 0) AS total_points, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path LIKE '施工%' AND wi.item_path NOT LIKE '施工配合%' THEN wri.number_value ELSE 0 END), 0) AS construction_minutes, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path LIKE '施工配合%' THEN wri.number_value ELSE 0 END), 0) AS cooperation_minutes, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path LIKE '维修%' THEN wri.number_value ELSE 0 END), 0) AS maintenance_minutes, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path NOT LIKE '施工%' AND wi.item_path NOT LIKE '施工配合%' AND wi.item_path NOT LIKE '维修%' THEN wri.number_value ELSE 0 END), 0) AS other_minutes " +
            "FROM work_report wr " +
            "JOIN work_report_item wri ON wri.report_id = wr.id " +
            "JOIN work_item wi ON wi.id = wri.work_item_id " +
            "JOIN org_unit ou ON ou.id = wr.area_id " +
            "JOIN org_unit ou2 ON ou2.id = wr.workshop_id " +
            "WHERE wr.period_id = #{periodId} AND wr.workshop_id = #{workshopId} " +
            "  AND wr.status IN ('已审核', '已锁定') " +
            "GROUP BY wr.area_id, ou.org_name, ou2.org_name " +
            "ORDER BY ou.org_name")
    List<Map<String, Object>> summaryByArea(@Param("periodId") Long periodId,
                                             @Param("workshopId") Long workshopId);

    /**
     * 按车间分组汇总（段级汇总用）
     * 统计指定期间下各车间的工时工分合计
     * 仅统计状态为"已审核"或"已锁定"的数据
     */
    @Select("SELECT wr.workshop_id, ou.org_name AS workshop_name, " +
            "       COUNT(DISTINCT wr.area_id) AS area_count, " +
            "       COUNT(DISTINCT wr.id) AS report_count, " +
            "       COUNT(DISTINCT wr.employee_id) AS employee_count, " +
            "       COALESCE(SUM(wri.number_value), 0) AS total_minutes, " +
            "       COALESCE(SUM(wri.points_value), 0) AS total_points, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path LIKE '施工%' AND wi.item_path NOT LIKE '施工配合%' THEN wri.number_value ELSE 0 END), 0) AS construction_minutes, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path LIKE '施工配合%' THEN wri.number_value ELSE 0 END), 0) AS cooperation_minutes, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path LIKE '维修%' THEN wri.number_value ELSE 0 END), 0) AS maintenance_minutes, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path NOT LIKE '施工%' AND wi.item_path NOT LIKE '施工配合%' AND wi.item_path NOT LIKE '维修%' THEN wri.number_value ELSE 0 END), 0) AS other_minutes " +
            "FROM work_report wr " +
            "JOIN work_report_item wri ON wri.report_id = wr.id " +
            "JOIN work_item wi ON wi.id = wri.work_item_id " +
            "JOIN org_unit ou ON ou.id = wr.workshop_id " +
            "WHERE wr.period_id = #{periodId} " +
            "  AND wr.status IN ('已审核', '已锁定') " +
            "GROUP BY wr.workshop_id, ou.org_name " +
            "ORDER BY ou.org_name")
    List<Map<String, Object>> summaryByWorkshop(@Param("periodId") Long periodId);

    /**
     * 所有工区汇总（段级展开用，不限制车间）
     */
    @Select("SELECT wr.workshop_id, wr.area_id, ou.org_name AS area_name, " +
            "       ou2.org_name AS workshop_name, " +
            "       COUNT(DISTINCT wr.id) AS report_count, " +
            "       COUNT(DISTINCT wr.employee_id) AS employee_count, " +
            "       COALESCE(SUM(wri.number_value), 0) AS total_minutes, " +
            "       COALESCE(SUM(wri.points_value), 0) AS total_points, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path LIKE '施工%' AND wi.item_path NOT LIKE '施工配合%' THEN wri.number_value ELSE 0 END), 0) AS construction_minutes, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path LIKE '施工配合%' THEN wri.number_value ELSE 0 END), 0) AS cooperation_minutes, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path LIKE '维修%' THEN wri.number_value ELSE 0 END), 0) AS maintenance_minutes, " +
            "       COALESCE(SUM(CASE WHEN wi.item_path NOT LIKE '施工%' AND wi.item_path NOT LIKE '施工配合%' AND wi.item_path NOT LIKE '维修%' THEN wri.number_value ELSE 0 END), 0) AS other_minutes " +
            "FROM work_report wr " +
            "JOIN work_report_item wri ON wri.report_id = wr.id " +
            "JOIN work_item wi ON wi.id = wri.work_item_id " +
            "JOIN org_unit ou ON ou.id = wr.area_id " +
            "JOIN org_unit ou2 ON ou2.id = wr.workshop_id " +
            "WHERE wr.period_id = #{periodId} " +
            "  AND wr.status IN ('已审核', '已锁定') " +
            "GROUP BY wr.workshop_id, wr.area_id, ou.org_name, ou2.org_name " +
            "ORDER BY ou2.org_name, ou.org_name")
    List<Map<String, Object>> summaryAllAreas(@Param("periodId") Long periodId);
}
