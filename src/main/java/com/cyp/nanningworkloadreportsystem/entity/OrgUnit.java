package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyp.nanningworkloadreportsystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 组织架构实体类
 * 对应数据库表 org_unit
 * 采用树形结构存储段-车间-工区-车间本级四级组织
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_unit")
public class OrgUnit extends BaseEntity {

    /** 上级组织ID，0表示根节点 */
    private Long parentId;

    /** 组织名称（如：贺州通信车间、梧州通信工区） */
    private String orgName;

    /** 组织类型：SECTION-段, WORKSHOP-车间, WORK_AREA-工区, WORKSHOP_LEVEL-车间本级 */
    private String orgType;

    /** Excel工作表映射代码（A/B/C/D/E/F/G/H） */
    private String areaCode;

    /** 排序号 */
    private Integer sortOrder;

    /** 是否启用：1-启用, 0-停用 */
    private Integer enabled;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ==================== 非数据库字段（树形结构辅助） ====================

    /** 子组织列表（树形结构，不映射到数据库） */
    @TableField(exist = false)
    private List<OrgUnit> children = new ArrayList<>();
}
