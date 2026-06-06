package com.cyp.nanningworkloadreportsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyp.nanningworkloadreportsystem.entity.SysUserOrgScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户组织权限绑定 Mapper
 */
@Mapper
public interface SysUserOrgScopeMapper extends BaseMapper<SysUserOrgScope> {

    /**
     * 根据用户ID查询用户的角色列表
     */
    @Select("SELECT DISTINCT role_code FROM sys_user_org_scope WHERE user_id = #{userId}")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询用户的所有绑定关系（含组织信息）
     */
    @Select("SELECT s.*, o.org_name as org_name FROM sys_user_org_scope s " +
            "LEFT JOIN org_unit o ON s.org_id = o.id WHERE s.user_id = #{userId}")
    List<SysUserOrgScope> selectByUserId(@Param("userId") Long userId);
}
