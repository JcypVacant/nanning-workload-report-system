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
 * 用工项目字典实体类
 * 对应数据库表 work_item
 * 以树形结构存储所有可填报的用工项目
 *
 * 三种类型：
 * - CATEGORY（分类项）：仅作为分类节点，不可填写
 * - NUMBER（数值项）：可填写数值（分钟或工分）
 * - TEXT（文本项）：可填写文字内容
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_item")
public class WorkItem extends BaseEntity {

    /** 上级项目ID，0表示根项目 */
    private Long parentId;

    /** 项目名称 */
    private String itemName;

    /** 完整路径（如：培训/适应性培训/局内） */
    private String itemPath;

    /** 层级（1=一级分类, 2=二级, 3=三级...） */
    private Integer itemLevel;

    /** 输入类型：CATEGORY-分类项, NUMBER-数值项, TEXT-文本项 */
    private String inputType;

    /** 适用报表类型：HOURS-工时, POINTS-工分, BOTH-通用 */
    private String reportType;

    /** 单位：分钟/分/文本 */
    private String unit;

    /** 是否仅为分类节点（不可填写）：1-是, 0-否 */
    private Integer isCategory;

    /** 是否为可填写的最终项：1-是, 0-否 */
    private Integer isInputItem;

    /** 是否需要填写备注/文本：1-需要, 0-不需要 */
    private Integer needRemark;

    /** 对应Excel列标识 */
    private String excelColumn;

    /** 排序号 */
    private Integer sortOrder;

    /** 是否启用：1-启用, 0-停用 */
    private Integer enabled;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ==================== 非数据库字段（树形结构辅助） ====================

    /** 子项目列表（树形结构） */
    @TableField(exist = false)
    private List<WorkItem> children = new ArrayList<>();
}
