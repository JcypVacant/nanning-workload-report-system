package com.cyp.nanningworkloadreportsystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.OrgUnit;
import com.cyp.nanningworkloadreportsystem.service.OrgUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织架构管理控制器
 */
@Tag(name = "组织架构管理", description = "组织树的增删改查")
@RestController
@RequestMapping("/api/v1/org")
@RequiredArgsConstructor
public class OrgUnitController {

    private final OrgUnitService orgUnitService;

    @Operation(summary = "获取组织树")
    @GetMapping("/tree")
    public Result<List<OrgUnit>> getTree() {
        return Result.ok(orgUnitService.getTree());
    }

    @Operation(summary = "获取车间列表")
    @GetMapping("/workshops")
    public Result<List<OrgUnit>> getWorkshops() {
        return Result.ok(orgUnitService.getWorkshops());
    }

    @Operation(summary = "获取车间下属工区")
    @GetMapping("/workshops/{workshopId}/areas")
    public Result<List<OrgUnit>> getAreas(@PathVariable Long workshopId) {
        return Result.ok(orgUnitService.getAreasByWorkshopId(workshopId));
    }

    @Operation(summary = "新增组织")
    @SaCheckRole("SECTION_ADMIN")
    @PostMapping
    public Result<OrgUnit> create(@RequestBody OrgUnit org) {
        return Result.ok(orgUnitService.create(org));
    }

    @Operation(summary = "更新组织")
    @SaCheckRole("SECTION_ADMIN")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody OrgUnit org) {
        org.setId(id);
        orgUnitService.update(org);
        return Result.ok();
    }

    @Operation(summary = "删除组织")
    @SaCheckRole("SECTION_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orgUnitService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "启停组织")
    @SaCheckRole("SECTION_ADMIN")
    @PatchMapping("/{id}/toggle")
    public Result<Void> toggleEnabled(@PathVariable Long id) {
        orgUnitService.toggleEnabled(id);
        return Result.ok();
    }
}
