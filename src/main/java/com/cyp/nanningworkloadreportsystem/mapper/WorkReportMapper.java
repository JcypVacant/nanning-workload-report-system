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

    // ==================== 统计分析查询 ====================

    /** 按车间统计 */
    @Select("<script>" +
            "SELECT ou.org_name AS name, COALESCE(SUM(wri.number_value),0) AS hours, " +
            "  COALESCE(SUM(wri.points_value),0) AS points " +
            "FROM work_report wr " +
            "JOIN work_report_item wri ON wri.report_id = wr.id " +
            "JOIN org_unit ou ON ou.id = wr.workshop_id " +
            "WHERE wr.status IN ('已审核','已锁定') " +
            "<if test='periodId != null'>AND wr.period_id = #{periodId}</if> " +
            "<if test='workshopId != null'>AND wr.workshop_id = #{workshopId}</if> " +
            "<if test='areaId != null'>AND wr.area_id = #{areaId}</if> " +
            "GROUP BY wr.workshop_id, ou.org_name ORDER BY hours DESC" +
            "</script>")
    List<Map<String, Object>> statsByWorkshop(@Param("periodId") Long periodId,
                                               @Param("workshopId") Long workshopId,
                                               @Param("areaId") Long areaId);

    /** 按工区统计 */
    @Select("<script>" +
            "SELECT ou.org_name AS name, COALESCE(SUM(wri.number_value),0) AS hours, " +
            "  COALESCE(SUM(wri.points_value),0) AS points " +
            "FROM work_report wr " +
            "JOIN work_report_item wri ON wri.report_id = wr.id " +
            "JOIN org_unit ou ON ou.id = wr.area_id " +
            "WHERE wr.status IN ('已审核','已锁定') " +
            "<if test='periodId != null'>AND wr.period_id = #{periodId}</if> " +
            "<if test='workshopId != null'>AND wr.workshop_id = #{workshopId}</if> " +
            "<if test='areaId != null'>AND wr.area_id = #{areaId}</if> " +
            "GROUP BY wr.area_id, ou.org_name ORDER BY hours DESC" +
            "</script>")
    List<Map<String, Object>> statsByArea(@Param("periodId") Long periodId,
                                           @Param("workshopId") Long workshopId,
                                           @Param("areaId") Long areaId);

    /** 按人员排名（TOP 20） */
    @Select("<script>" +
            "SELECT e.name AS name, COALESCE(SUM(wri.number_value),0) AS hours, " +
            "  COALESCE(SUM(wri.points_value),0) AS points " +
            "FROM work_report wr " +
            "JOIN work_report_item wri ON wri.report_id = wr.id " +
            "JOIN employee e ON e.id = wr.employee_id " +
            "WHERE wr.status IN ('已审核','已锁定') " +
            "<if test='periodId != null'>AND wr.period_id = #{periodId}</if> " +
            "<if test='workshopId != null'>AND wr.workshop_id = #{workshopId}</if> " +
            "<if test='areaId != null'>AND wr.area_id = #{areaId}</if> " +
            "GROUP BY wr.employee_id, e.name ORDER BY hours DESC LIMIT 20" +
            "</script>")
    List<Map<String, Object>> statsByEmployee(@Param("periodId") Long periodId,
                                               @Param("workshopId") Long workshopId,
                                               @Param("areaId") Long areaId);

    /** 按用工项目顶级分类统计 */
    @Select("<script>" +
            "SELECT " +
            "  CASE " +
            "    WHEN wi.item_path LIKE '施工%' AND wi.item_path NOT LIKE '施工配合%' THEN '施工' " +
            "    WHEN wi.item_path LIKE '施工配合%' THEN '施工配合' " +
            "    WHEN wi.item_path LIKE '维修%' THEN '维修' " +
            "    WHEN wi.item_path LIKE '核心网%' THEN '核心网/升级优化' " +
            "    WHEN wi.item_path LIKE '故障处理%' AND wi.input_type='NUMBER' THEN '故障处理' " +
            "    WHEN wi.item_path LIKE '任务%' AND wi.input_type='NUMBER' THEN '任务' " +
            "    WHEN wi.item_path LIKE '防洪%' AND wi.input_type='NUMBER' THEN '防洪' " +
            "    WHEN wi.item_path LIKE '防台%' AND wi.input_type='NUMBER' THEN '防台' " +
            "    WHEN wi.item_path LIKE '培训%' AND wi.input_type='NUMBER' THEN '培训' " +
            "    WHEN wi.item_path LIKE '休假%' AND wi.input_type='NUMBER' THEN '休假' " +
            "    WHEN wi.item_path LIKE '值班%' THEN '值班' " +
            "    ELSE '其他' " +
            "  END AS name, " +
            "  COALESCE(SUM(wri.number_value),0) AS hours, " +
            "  COALESCE(SUM(wri.points_value),0) AS points " +
            "FROM work_report wr " +
            "JOIN work_report_item wri ON wri.report_id = wr.id " +
            "JOIN work_item wi ON wi.id = wri.work_item_id " +
            "WHERE wr.status IN ('已审核','已锁定') AND wi.input_type = 'NUMBER' " +
            "<if test='periodId != null'>AND wr.period_id = #{periodId}</if> " +
            "<if test='workshopId != null'>AND wr.workshop_id = #{workshopId}</if> " +
            "<if test='areaId != null'>AND wr.area_id = #{areaId}</if> " +
            "GROUP BY name ORDER BY hours DESC" +
            "</script>")
    List<Map<String, Object>> statsByProject(@Param("periodId") Long periodId,
                                              @Param("workshopId") Long workshopId,
                                              @Param("areaId") Long areaId);

    /** 按月趋势统计（最近12个月） */
    @Select("<script>" +
            "SELECT DATE_FORMAT(wr.work_date, '%Y年%m月') AS month, " +
            "  COALESCE(SUM(wri.number_value),0) AS total_minutes, " +
            "  COALESCE(SUM(wri.points_value),0) AS total_points " +
            "FROM work_report wr " +
            "JOIN work_report_item wri ON wri.report_id = wr.id " +
            "WHERE wr.status IN ('已审核','已锁定') " +
            "  AND wr.work_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "<if test='workshopId != null'>AND wr.workshop_id = #{workshopId}</if> " +
            "<if test='areaId != null'>AND wr.area_id = #{areaId}</if> " +
            "GROUP BY DATE_FORMAT(wr.work_date, '%Y%m'), month ORDER BY month" +
            "</script>")
    List<Map<String, Object>> statsByMonth(@Param("workshopId") Long workshopId,
                                            @Param("areaId") Long areaId);

    /** 按填报状态统计数量 */
    @Select("<script>" +
            "SELECT wr.status, COUNT(*) AS cnt " +
            "FROM work_report wr " +
            "WHERE 1=1 " +
            "<if test='periodId != null'>AND wr.period_id = #{periodId}</if> " +
            "<if test='workshopId != null'>AND wr.workshop_id = #{workshopId}</if> " +
            "<if test='areaId != null'>AND wr.area_id = #{areaId}</if> " +
            "GROUP BY wr.status" +
            "</script>")
    List<Map<String, Object>> statsByStatus(@Param("periodId") Long periodId,
                                             @Param("workshopId") Long workshopId,
                                             @Param("areaId") Long areaId);
}
