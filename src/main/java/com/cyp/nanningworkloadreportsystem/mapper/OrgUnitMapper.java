package com.cyp.nanningworkloadreportsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyp.nanningworkloadreportsystem.entity.OrgUnit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 组织架构 Mapper
 * 继承 MyBatis Plus BaseMapper，自动获得基础 CRUD 方法
 */
@Mapper
public interface OrgUnitMapper extends BaseMapper<OrgUnit> {

    /**
     * 根据组织类型查询组织列表
     *
     * @param orgType 组织类型编码
     * @return 组织列表
     */
    @Select("SELECT * FROM org_unit WHERE org_type = #{orgType} AND enabled = 1 ORDER BY sort_order")
    List<OrgUnit> selectByOrgType(@Param("orgType") String orgType);

    /**
     * 根据父级ID查询子组织列表
     *
     * @param parentId 父级组织ID
     * @return 子组织列表
     */
    @Select("SELECT * FROM org_unit WHERE parent_id = #{parentId} AND enabled = 1 ORDER BY sort_order")
    List<OrgUnit> selectByParentId(@Param("parentId") Long parentId);
}
