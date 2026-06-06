package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户组织权限绑定实体类
 * 对应数据库表 sys_user_org_scope
 * 记录用户与组织、角色的绑定关系，实现一个用户绑定多个组织
 */
@Data
@TableName("sys_user_org_scope")
public class SysUserOrgScope {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（关联sys_user.id） */
    private Long userId;

    /** 组织ID（关联org_unit.id，段级管理员为0） */
    private Long orgId;

    /** 角色编码：SECTION_ADMIN-段级管理员, WORKSHOP_ADMIN-车间管理员, AREA_REPORTER-工区填报员 */
    private String roleCode;

    /** 数据范围类型：ALL-全段, WORKSHOP-车间范围, AREA-工区范围 */
    private String scopeType;

    /** 创建时间 */
    private LocalDateTime createTime;
}
