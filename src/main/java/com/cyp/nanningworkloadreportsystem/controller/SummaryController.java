package com.cyp.nanningworkloadreportsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.dto.SectionSummaryDTO;
import com.cyp.nanningworkloadreportsystem.dto.WorkshopSummaryDTO;
import com.cyp.nanningworkloadreportsystem.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 汇总统计控制器
 * 提供车间级和段级的工时工分汇总查询
 */
@Tag(name = "汇总统计", description = "车间级和段级的工时工分汇总查询")
@RestController
@RequestMapping("/api/v1/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @Operation(summary = "车间汇总 — 按工区分组统计")
    @GetMapping("/workshop")
    public Result<List<WorkshopSummaryDTO>> workshopSummary(
            @RequestParam Long periodId,
            @RequestParam(required = false) Long workshopId) {
        return Result.ok(summaryService.getWorkshopSummary(periodId, workshopId));
    }

    @Operation(summary = "段级汇总 — 按车间分组统计")
    @GetMapping("/section")
    public Result<List<SectionSummaryDTO>> sectionSummary(
            @RequestParam Long periodId) {
        return Result.ok(summaryService.getSectionSummary(periodId));
    }
}
