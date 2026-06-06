package com.cyp.nanningworkloadreportsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyp.nanningworkloadreportsystem.entity.WorkItem;
import com.cyp.nanningworkloadreportsystem.mapper.WorkItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用工项目字典服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkItemService {

    private final WorkItemMapper workItemMapper;
    private final OperationLogService logService;

    /** 获取完整用工项目树 */
    public List<WorkItem> getTree() {
        List<WorkItem> all = workItemMapper.selectList(
                new LambdaQueryWrapper<WorkItem>()
                        .eq(WorkItem::getEnabled, 1)
                        .orderByAsc(WorkItem::getSortOrder));
        return buildTree(all, 0L);
    }

    /** 获取叶子节点（可填写的最终项目） */
    public List<WorkItem> getLeaves(String reportType) {
        if (reportType != null && !reportType.isEmpty()) {
            return workItemMapper.selectLeavesByReportType(reportType);
        }
        return workItemMapper.selectLeaves();
    }

    /** 递归构建树 */
    private List<WorkItem> buildTree(List<WorkItem> all, Long parentId) {
        List<WorkItem> tree = new ArrayList<>();
        for (WorkItem item : all) {
            if (item.getParentId().equals(parentId)) {
                item.setChildren(buildTree(all, item.getId()));
                tree.add(item);
            }
        }
        return tree;
    }

    /** 新增用工项目 */
    @Transactional
    public WorkItem create(WorkItem item) {
        if (item.getSortOrder() == null) item.setSortOrder(0);
        if (item.getEnabled() == null) item.setEnabled(1);
        // 构建完整路径
        buildItemPath(item);
        workItemMapper.insert(item);
        log.info("新增用工项目: {}", item.getItemPath());
        logService.record("用工项目字典", "CREATE", String.valueOf(item.getId()), "新增项目: " + item.getItemPath());
        return item;
    }

    /** 构建项目的完整路径 */
    private void buildItemPath(WorkItem item) {
        if (item.getParentId() != null && item.getParentId() > 0) {
            WorkItem parent = workItemMapper.selectById(item.getParentId());
            if (parent != null && parent.getItemPath() != null) {
                item.setItemPath(parent.getItemPath() + "/" + item.getItemName());
            } else {
                item.setItemPath(item.getItemName());
            }
        } else {
            item.setItemPath(item.getItemName());
        }
    }

    /** 更新用工项目 */
    @Transactional
    public void update(WorkItem item) {
        buildItemPath(item);
        workItemMapper.updateById(item);
    }

    /** 删除用工项目 */
    @Transactional
    public void delete(Long id) {
        List<WorkItem> children = workItemMapper.selectList(
                new LambdaQueryWrapper<WorkItem>().eq(WorkItem::getParentId, id));
        if (children != null && !children.isEmpty()) {
            throw new RuntimeException("该用工项目下存在子项目，请先删除子项目");
        }
        WorkItem item = new WorkItem();
        item.setId(id);
        item.setEnabled(0);
        workItemMapper.updateById(item);
    }

    /** 启停用工项目 */
    public void toggleEnabled(Long id) {
        WorkItem item = workItemMapper.selectById(id);
        if (item != null) {
            item.setEnabled(item.getEnabled() == 1 ? 0 : 1);
            workItemMapper.updateById(item);
        }
    }
}
