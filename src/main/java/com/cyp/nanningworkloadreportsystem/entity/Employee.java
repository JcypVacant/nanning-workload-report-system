package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyp.nanningworkloadreportsystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人员信息实体类
 * 对应数据库表 employee
 * 存储参与工时工分填报的人员基础信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("employee")
public class Employee extends BaseEntity {

    /** 姓名 */
    private String name;

    /** 性别：男/女 */
    private String gender;

    /** 所属单位名称（如：南宁通信段） */
    private String unitName;

    /** 部门（车间）名称 */
    private String departmentName;

    /** 所属车间ID（关联org_unit.id） */
    private Long workshopId;

    /** 班组名称 */
    private String teamName;

    /** 所属工区ID（关联org_unit.id） */
    private Long areaId;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 职位名称 */
    private String positionName;

    /** 聘任专业职务工种 */
    private String professionalPostType;

    /** 工种 */
    private String workType;

    /** 职级分类 */
    private String rankCategory;

    /** 人员状态：在岗/调出/停用 */
    private String employeeStatus;

    /** 是否启用：1-启用, 0-停用 */
    private Integer enabled;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ==================== 非数据库字段（关联查询） ====================

    /** 车间名称（关联查询） */
    @TableField(exist = false)
    private String workshopName;

    /** 工区名称（关联查询） */
    @TableField(exist = false)
    private String areaName;
}
