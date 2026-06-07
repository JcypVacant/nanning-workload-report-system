package com.cyp.nanningworkloadreportsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyp.nanningworkloadreportsystem.common.PageResult;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.AuditRecord;
import com.cyp.nanningworkloadreportsystem.entity.WorkReport;
import com.cyp.nanningworkloadreportsystem.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审核管理控制器
 */
@Tag(name = "审核管理", description = "车间审核、退回操作")
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "查询待审核数据")
    @GetMapping("/pending")
    public Result<List<WorkReport>> getPending(@RequestParam(required = false) Long periodId) {
        return Result.ok(auditService.getPending(periodId));
    }

    @Operation(summary = "查询所有填报记录（含已审核、已退回、已提交）")
    @GetMapping("/reports")
    public Result<List<WorkReport>> getAllReports(
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) String status) {
        return Result.ok(auditService.getAllReports(periodId, status));
    }

    @Operation(summary = "分页查询所有填报记录")
    @GetMapping("/reports/page")
    public Result<PageResult<WorkReport>> getAllReportsPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long areaId) {
        IPage<WorkReport> page = auditService.getAllReportsPage(pageNum, pageSize, periodId, status, areaId);
        return Result.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/{reportId}/approve")
    public Result<Void> approve(@PathVariable Long reportId, @RequestBody(required = false) AuditRequest req) {
        auditService.approve(reportId, req != null ? req.getComment() : null);
        return Result.ok();
    }

    @Operation(summary = "批量审核通过")
    @PostMapping("/batch-approve")
    public Result<Void> batchApprove(@RequestBody BatchAuditRequest req) {
        auditService.batchApprove(req.getReportIds(), req.getComment());
        return Result.ok();
    }

    @Operation(summary = "退回修改")
    @PostMapping("/{reportId}/return")
    public Result<Void> returnReport(@PathVariable Long reportId, @RequestBody AuditRequest req) {
        if (req == null || req.getComment() == null || req.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("退回时必须填写退回原因");
        }
        auditService.returnReport(reportId, req.getComment());
        return Result.ok();
    }

    @Operation(summary = "提交到段级审核")
    @PostMapping("/submit-to-section/{periodId}")
    public Result<Void> submitToSection(@PathVariable Long periodId) {
        auditService.submitToSection(periodId);
        return Result.ok();
    }

    @Operation(summary = "查询审核记录")
    @GetMapping("/records")
    public Result<PageResult<AuditRecord>> getRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long periodId) {
        IPage<AuditRecord> page = auditService.getAuditRecords(pageNum, pageSize, periodId);
        return Result.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @Data
    public static class AuditRequest {
        private String comment;
    }

    @Data
    public static class BatchAuditRequest {
        private List<Long> reportIds;
        private String comment;
    }
}
