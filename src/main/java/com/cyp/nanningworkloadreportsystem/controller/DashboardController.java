package com.cyp.nanningworkloadreportsystem.controller;

import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页看板控制器
 * 使用实时聚合统计数据（来自 StatisticsService），
 * 数据范围根据当前登录用户角色自动过滤
 */
@Tag(name = "首页看板", description = "首页统计卡片和图表数据")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashboardController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(
            @RequestParam(required = false) Long periodId) {
        Map<String, Object> data = new HashMap<>();

        // 状态统计
        Map<String, Object> statusStats = statisticsService.getStatusStats(periodId);
        long totalReports = statusStats.values().stream()
                .mapToLong(v -> ((Number) v).longValue()).sum();

        // 工区统计
        List<Map<String, Object>> areaStats = statisticsService.getByArea(periodId, null);

        // 项目统计
        List<Map<String, Object>> projectStats = statisticsService.getByProject(periodId);

        // 统计卡片
        double totalMinutes = areaStats.stream().mapToDouble(a -> ((Number) a.get("hours")).doubleValue()).sum();
        double totalPoints = areaStats.stream().mapToDouble(a -> ((Number) a.get("points")).doubleValue()).sum();

        List<Map<String, Object>> cards = new ArrayList<>();
        cards.add(createCard("工时合计", formatNumber(totalMinutes), "分钟", "#409eff"));
        cards.add(createCard("工分合计", formatNumber(totalPoints), "分", "#67c23a"));
        cards.add(createCard("涉及工区", String.valueOf(areaStats.size()), "个", "#e6a23c"));
        cards.add(createCard("填报记录", String.valueOf((long) totalReports), "条", "#f56c6c"));
        data.put("cards", cards);

        // 工区对比图表
        data.put("workshopChart", areaStats.stream().map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", a.get("name"));
            item.put("value", ((Number) a.get("hours")).doubleValue());
            return item;
        }).collect(Collectors.toList()));

        // 项目占比图表
        data.put("projectChart", projectStats.stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", p.get("name"));
            item.put("value", ((Number) p.get("hours")).doubleValue());
            return item;
        }).collect(Collectors.toList()));

        return Result.ok(data);
    }

    private Map<String, Object> createCard(String title, String value, String unit, String color) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("title", title);
        card.put("value", value);
        card.put("unit", unit);
        card.put("color", color);
        return card;
    }

    private String formatNumber(double v) {
        if (v >= 10000) return String.format("%.1f万", v / 10000);
        if (v >= 1000) return String.format("%.0f", v);
        return String.format("%.1f", v);
    }
}
