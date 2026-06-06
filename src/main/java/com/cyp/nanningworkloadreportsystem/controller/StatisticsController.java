package com.cyp.nanningworkloadreportsystem.controller;

import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计分析控制器
 * 提供多维度实时统计数据（按车间/工区/人员/项目/月份/状态等）
 * 数据范围根据当前登录用户角色自动过滤
 */
@Tag(name = "统计分析", description = "多维度统计数据和图表数据")
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "按车间统计")
    @GetMapping("/by-workshop")
    public Result<List<Map<String, Object>>> getByWorkshop(
            @RequestParam(required = false) Long periodId) {
        return Result.ok(statisticsService.getByWorkshop(periodId));
    }

    @Operation(summary = "按工区统计")
    @GetMapping("/by-area")
    public Result<List<Map<String, Object>>> getByArea(
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long workshopId) {
        return Result.ok(statisticsService.getByArea(periodId, workshopId));
    }

    @Operation(summary = "按人员排名统计")
    @GetMapping("/by-employee")
    public Result<List<Map<String, Object>>> getByEmployee(
            @RequestParam(required = false) Long periodId) {
        return Result.ok(statisticsService.getByEmployee(periodId));
    }

    @Operation(summary = "按用工项目统计（占比）")
    @GetMapping("/by-project")
    public Result<List<Map<String, Object>>> getByProject(
            @RequestParam(required = false) Long periodId) {
        return Result.ok(statisticsService.getByProject(periodId));
    }

    @Operation(summary = "按月份趋势统计")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend() {
        return Result.ok(statisticsService.getTrend());
    }

    @Operation(summary = "按填报状态统计")
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatusStats(
            @RequestParam(required = false) Long periodId) {
        return Result.ok(statisticsService.getStatusStats(periodId));
    }
}
