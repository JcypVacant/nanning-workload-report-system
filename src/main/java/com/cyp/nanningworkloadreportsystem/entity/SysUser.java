package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyp.nanningworkloadreportsystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 对应数据库表 sys_user
 * 存储登录账号的核心信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 登录账号（唯一） */
    private String username;

    /** 登录密码（BCrypt加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 联系电话 */
    private String phone;

    /** 是否启用：1-启用, 0-禁用 */
    private Integer enabled;

    /** 是否首次登录：1-是, 0-否（用于强制修改密码） */
    private Integer firstLogin;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ==================== 非数据库字段（关联查询） ====================

    /** 角色编码（关联sys_user_org_scope表，不映射到数据库） */
    @TableField(exist = false)
    private String roleCode;

    /** 绑定的组织ID（关联sys_user_org_scope表） */
    @TableField(exist = false)
    private Long orgId;

    /** 绑定的组织名称（关联查询用） */
    @TableField(exist = false)
    private String orgName;
}
