package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 对应数据库表 operation_log
 * 记录系统中的关键操作，用于后期追溯
 */
@Data
@TableName("operation_log")
public class OperationLog {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID（关联sys_user.id） */
    private Long userId;

    /** 操作用户名（冗余字段，便于查询） */
    private String username;

    /** 操作模块（如：工区填报/车间审核/账号管理） */
    private String moduleName;

    /** 操作类型：LOGIN/CREATE/UPDATE/DELETE/SUBMIT/APPROVE/RETURN/EXPORT/LOCK/UNLOCK/TRANSFER */
    private String operationType;

    /** 操作目标ID */
    private String targetId;

    /** 操作摘要（人可读的描述） */
    private String summary;

    /** 操作前数据（JSON格式） */
    private String beforeJson;

    /** 操作后数据（JSON格式） */
    private String afterJson;

    /** 操作IP地址 */
    private String ip;

    /** 操作时间 */
    private LocalDateTime operateTime;
}
