package com.cyp.nanningworkloadreportsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyp.nanningworkloadreportsystem.entity.WorkReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工时工分填报主表 Mapper
 * 复杂查询通过 XML 映射文件实现
 */
@Mapper
public interface WorkReportMapper extends BaseMapper<WorkReport> {
}
