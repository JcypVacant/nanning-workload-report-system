package com.cyp.nanningworkloadreportsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyp.nanningworkloadreportsystem.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人员信息 Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
