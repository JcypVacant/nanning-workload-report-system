package com.cyp.nanningworkloadreportsystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyp.nanningworkloadreportsystem.common.PageResult;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.MonthlyPeriod;
import com.cyp.nanningworkloadreportsystem.service.MonthlyPeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 月度填报管理控制器
 */
@Tag(name = "月度填报管理", description = "创建和管理填报月份")
@RestController
@RequestMapping("/api/v1/periods")
@RequiredArgsConstructor
public class MonthlyPeriodController {

    private final MonthlyPeriodService periodService;

    @Operation(summary = "分页查询月度期间")
    @GetMapping("/page")
    public Result<PageResult<MonthlyPeriod>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<MonthlyPeriod> page = periodService.getPage(pageNum, pageSize);
        return Result.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @Operation(summary = "获取当前活跃月份")
    @GetMapping("/active")
    public Result<MonthlyPeriod> getActive() {
        return Result.ok(periodService.getActive());
    }

    @Operation(summary = "创建月度期间")
    @SaCheckRole("SECTION_ADMIN")
    @PostMapping
    public Result<MonthlyPeriod> create(@RequestBody CreatePeriodRequest req) {
        return Result.ok(periodService.create(req.getYear(), req.getMonth(),
                req.getStartDate(), req.getEndDate(), req.getAuditDeadline()));
    }

    @Operation(summary = "更新月度期间")
    @SaCheckRole("SECTION_ADMIN")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MonthlyPeriod period) {
        period.setId(id);
        periodService.update(period);
        return Result.ok();
    }

    @Operation(summary = "修改期间状态")
    @SaCheckRole("SECTION_ADMIN")
    @PatchMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody ChangeStatusRequest req) {
        periodService.changeStatus(id, req.getStatus());
        return Result.ok();
    }

    @Operation(summary = "锁定/解锁月份")
    @SaCheckRole("SECTION_ADMIN")
    @PatchMapping("/{id}/lock")
    public Result<Void> toggleLock(@PathVariable Long id) {
        periodService.toggleLock(id);
        return Result.ok();
    }

    @Data
    public static class CreatePeriodRequest {
        private Integer year;
        private Integer month;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate auditDeadline;
    }

    @Data
    public static class ChangeStatusRequest {
        private String status;
    }
}
