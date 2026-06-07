package com.cyp.nanningworkloadreportsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyp.nanningworkloadreportsystem.common.PageResult;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.WorkReport;
import com.cyp.nanningworkloadreportsystem.entity.WorkReportItem;
import com.cyp.nanningworkloadreportsystem.service.WorkReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 工时工分填报控制器（核心业务）
 */
@Tag(name = "工区填报", description = "工时工分填报的核心业务接口")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class WorkReportController {

    private final WorkReportService reportService;

    @Operation(summary = "分页查询填报记录")
    @GetMapping("/page")
    public Result<PageResult<WorkReport>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate workDate,
            @RequestParam(required = false) Long areaId) {
        IPage<WorkReport> page = reportService.getPage(pageNum, pageSize, periodId, employeeId, status, reportType, keyword, workDate, areaId);
        return Result.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @Operation(summary = "获取填报详情")
    @GetMapping("/{id}")
    public Result<WorkReport> getDetail(@PathVariable Long id) {
        return Result.ok(reportService.getDetail(id));
    }

    @Operation(summary = "创建填报记录")
    @PostMapping
    public Result<WorkReport> create(@RequestBody CreateReportRequest req) {
        return Result.ok(reportService.create(req.getPeriodId(), req.getEmployeeId(),
                req.getWorkDate(), req.getReportType(), req.getItems(), req.getRemark()));
    }

    @Operation(summary = "更新填报记录")
    @PutMapping("/{id}")
    public Result<WorkReport> update(@PathVariable Long id, @RequestBody UpdateReportRequest req) {
        return Result.ok(reportService.update(id, req.getItems(), req.getRemark()));
    }

    @Operation(summary = "删除填报记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reportService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "提交填报数据")
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        reportService.submit(id);
        return Result.ok();
    }

    @Operation(summary = "批量提交填报数据")
    @PostMapping("/batch-submit")
    public Result<Void> batchSubmit(@RequestBody BatchSubmitRequest req) {
        reportService.batchSubmit(req.getIds());
        return Result.ok();
    }

    @Data
    public static class CreateReportRequest {
        private Long periodId;
        private Long employeeId;
        private LocalDate workDate;
        private String reportType;
        private List<WorkReportItem> items;
        private String remark;
    }

    @Data
    public static class UpdateReportRequest {
        private List<WorkReportItem> items;
        private String remark;
    }

    @Data
    public static class BatchSubmitRequest {
        private List<Long> ids;
    }
}
