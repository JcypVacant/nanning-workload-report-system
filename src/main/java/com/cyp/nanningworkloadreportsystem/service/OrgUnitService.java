package com.cyp.nanningworkloadreportsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyp.nanningworkloadreportsystem.entity.OrgUnit;
import com.cyp.nanningworkloadreportsystem.mapper.OrgUnitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织架构服务类
 * 负责组织架构的树形CRUD及查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgUnitService {

    private final OrgUnitMapper orgUnitMapper;

    /**
     * 获取完整组织树（所有启用的组织）
     * 算法：
     * 1. 查询所有启用的组织
     * 2. 构建树形结构
     */
    public List<OrgUnit> getTree() {
        // 查询所有启用的组织，按排序号排列
        List<OrgUnit> allOrgs = orgUnitMapper.selectList(
                new LambdaQueryWrapper<OrgUnit>()
                        .eq(OrgUnit::getEnabled, 1)
                        .orderByAsc(OrgUnit::getSortOrder));

        // 构建树形结构：从根节点（parentId=0）开始递归组装
        return buildTree(allOrgs, 0L);
    }

    /**
     * 递归构建组织树
     */
    private List<OrgUnit> buildTree(List<OrgUnit> allOrgs, Long parentId) {
        List<OrgUnit> tree = new ArrayList<>();
        for (OrgUnit org : allOrgs) {
            if (org.getParentId().equals(parentId)) {
                // 递归查找子节点
                List<OrgUnit> children = buildTree(allOrgs, org.getId());
                org.setChildren(children);
                tree.add(org);
            }
        }
        return tree;
    }

    /**
     * 获取所有车间列表
     */
    public List<OrgUnit> getWorkshops() {
        return orgUnitMapper.selectByOrgType("WORKSHOP");
    }

    /**
     * 获取车间下属的工区列表
     */
    public List<OrgUnit> getAreasByWorkshopId(Long workshopId) {
        // 查询该车间下的所有工区和车间本级
        List<OrgUnit> children = orgUnitMapper.selectByParentId(workshopId);
        return children.stream()
                .filter(o -> "WORK_AREA".equals(o.getOrgType()) || "WORKSHOP_LEVEL".equals(o.getOrgType()))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有工区列表（用于人员管理下拉选择）
     */
    public List<OrgUnit> getAllAreas() {
        return orgUnitMapper.selectByOrgType("WORK_AREA");
    }

    /**
     * 新增组织
     */
    @Transactional
    public OrgUnit create(OrgUnit org) {
        if (org.getSortOrder() == null) org.setSortOrder(0);
        if (org.getEnabled() == null) org.setEnabled(1);
        orgUnitMapper.insert(org);
        log.info("创建组织: {}", org.getOrgName());
        return org;
    }

    /**
     * 更新组织
     */
    @Transactional
    public void update(OrgUnit org) {
        orgUnitMapper.updateById(org);
        log.info("更新组织: ID={}", org.getId());
    }

    /**
     * 删除组织（软删除：设置为停用）
     */
    @Transactional
    public void delete(Long id) {
        // 检查是否有子组织
        List<OrgUnit> children = orgUnitMapper.selectByParentId(id);
        if (children != null && !children.isEmpty()) {
            throw new RuntimeException("该组织下存在子组织，请先删除子组织");
        }
        OrgUnit org = new OrgUnit();
        org.setId(id);
        org.setEnabled(0);
        orgUnitMapper.updateById(org);
        log.info("停用组织: ID={}", id);
    }

    /**
     * 启用/停用组织
     */
    public void toggleEnabled(Long id) {
        OrgUnit org = orgUnitMapper.selectById(id);
        if (org == null) throw new RuntimeException("组织不存在");
        org.setEnabled(org.getEnabled() == 1 ? 0 : 1);
        orgUnitMapper.updateById(org);
    }
}
