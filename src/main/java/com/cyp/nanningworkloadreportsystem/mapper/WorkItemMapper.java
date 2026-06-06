package com.cyp.nanningworkloadreportsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyp.nanningworkloadreportsystem.entity.WorkItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用工项目字典 Mapper
 */
@Mapper
public interface WorkItemMapper extends BaseMapper<WorkItem> {

    /**
     * 查询所有叶子节点（可填写的最终项目）
     */
    @Select("SELECT * FROM work_item WHERE is_input_item = 1 AND enabled = 1 ORDER BY sort_order")
    List<WorkItem> selectLeaves();

    /**
     * 根据报表类型查询叶子节点
     */
    @Select("SELECT * FROM work_item WHERE is_input_item = 1 AND enabled = 1 " +
            "AND (report_type = #{reportType} OR report_type = 'BOTH') ORDER BY sort_order")
    List<WorkItem> selectLeavesByReportType(@Param("reportType") String reportType);
}
