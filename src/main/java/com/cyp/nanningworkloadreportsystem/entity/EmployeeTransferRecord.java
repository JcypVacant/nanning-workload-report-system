package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人员调动记录实体类
 * 对应数据库表 employee_transfer_record
 * 记录人员所属车间或工区的变更历史
 */
@Data
@TableName("employee_transfer_record")
public class EmployeeTransferRecord {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 人员ID（关联employee.id） */
    private Long employeeId;

    /** 调动前车间ID */
    private Long beforeWorkshopId;

    /** 调动前工区ID */
    private Long beforeAreaId;

    /** 调动前班组名称 */
    private String beforeTeamName;

    /** 调动后车间ID */
    private Long afterWorkshopId;

    /** 调动后工区ID */
    private Long afterAreaId;

    /** 调动后班组名称 */
    private String afterTeamName;

    /** 调动原因 */
    private String transferReason;

    /** 操作人ID（关联sys_user.id） */
    private Long operatorId;

    /** 操作时间 */
    private LocalDateTime operateTime;

    /** 备注 */
    private String remark;

    /** 审核状态：待审核/已通过/已退回 */
    private String status;

    /** 审批人ID */
    private Long approvedBy;

    /** 审批时间 */
    private LocalDateTime approveTime;

    /** 审批意见 */
    private String approveComment;
}
