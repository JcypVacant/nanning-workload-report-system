package com.cyp.nanningworkloadreportsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyp.nanningworkloadreportsystem.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录 Mapper
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
}
