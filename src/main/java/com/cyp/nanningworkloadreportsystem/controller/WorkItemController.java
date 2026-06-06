package com.cyp.nanningworkloadreportsystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.WorkItem;
import com.cyp.nanningworkloadreportsystem.service.WorkItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用工项目字典控制器
 */
@Tag(name = "用工项目字典", description = "树形用工项目字典的增删改查")
@RestController
@RequestMapping("/api/v1/work-items")
@RequiredArgsConstructor
public class WorkItemController {

    private final WorkItemService workItemService;

    @Operation(summary = "获取用工项目树")
    @GetMapping("/tree")
    public Result<List<WorkItem>> getTree() {
        return Result.ok(workItemService.getTree());
    }

    @Operation(summary = "获取叶子节点列表")
    @GetMapping("/leaves")
    public Result<List<WorkItem>> getLeaves(@RequestParam(required = false) String reportType) {
        return Result.ok(workItemService.getLeaves(reportType));
    }

    @Operation(summary = "新增用工项目")
    @SaCheckRole("SECTION_ADMIN")
    @PostMapping
    public Result<WorkItem> create(@RequestBody WorkItem item) {
        return Result.ok(workItemService.create(item));
    }

    @Operation(summary = "更新用工项目")
    @SaCheckRole("SECTION_ADMIN")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody WorkItem item) {
        item.setId(id);
        workItemService.update(item);
        return Result.ok();
    }

    @Operation(summary = "删除用工项目")
    @SaCheckRole("SECTION_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workItemService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "启停用工项目")
    @SaCheckRole("SECTION_ADMIN")
    @PatchMapping("/{id}/toggle")
    public Result<Void> toggleEnabled(@PathVariable Long id) {
        workItemService.toggleEnabled(id);
        return Result.ok();
    }
}
