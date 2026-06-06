package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核记录实体类
 * 对应数据库表 audit_record
 * 记录工区提交、车间审核、退回修改、段级锁定等流程操作
 */
@Data
@TableName("audit_record")
public class AuditRecord {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 月度期间ID（关联monthly_period.id） */
    private Long periodId;

    /** 填报记录ID（关联work_report.id，可为空表示批量操作） */
    private Long reportId;

    /** 目标组织ID（关联org_unit.id） */
    private Long orgId;

    /** 审核层级：WORKSHOP-车间级, SECTION-段级 */
    private String auditLevel;

    /** 操作类型：SUBMIT-提交, APPROVE-通过, RETURN-退回, LOCK-锁定, UNLOCK-解锁 */
    private String action;

    /** 审核意见（退回原因等） */
    private String comment;

    /** 操作人ID（关联sys_user.id） */
    private Long operatorId;

    /** 操作时间 */
    private LocalDateTime operateTime;

    // ==================== 非数据库字段（关联查询） ====================

    /** 操作人姓名 */
    @TableField(exist = false)
    private String operatorName;

    /** 组织名称 */
    @TableField(exist = false)
    private String orgName;
}
