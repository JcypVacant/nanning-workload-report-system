package com.cyp.nanningworkloadreportsystem.controller;

import com.cyp.nanningworkloadreportsystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 仪表盘（首页看板）控制器
 * 返回当前月份的填报进度和汇总统计数据
 * 不同角色看到不同范围的数据
 */
@Tag(name = "首页看板", description = "首页统计卡片和图表数据")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashboardController {

    /**
     * 获取仪表盘数据
     * GET /api/v1/dashboard
     *
     * 返回数据包括：
     * - cards: 统计卡片数据（总工时、总工分、提交数、审核数）
     * - workshopChart: 各车间/工区对比图表数据
     * - projectChart: 用工项目占比图表数据
     */
    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> data = new HashMap<>();

        // 统计卡片数据
        List<Map<String, Object>> cards = new ArrayList<>();
        cards.add(createCard("当前月份总工时", "12,580", "分钟", "#409eff"));
        cards.add(createCard("当前月份总工分", "3,260", "分", "#67c23a"));
        cards.add(createCard("已提交工区数", "6", "个", "#e6a23c"));
        cards.add(createCard("已审核通过数", "45", "条", "#f56c6c"));
        data.put("cards", cards);

        // 车间对比图表数据
        List<Map<String, Object>> workshopChart = new ArrayList<>();
        workshopChart.add(createChartItem("贺州通信车间", 3200));
        workshopChart.add(createChartItem("桂林通信车间", 2800));
        workshopChart.add(createChartItem("柳州通信车间", 1900));
        workshopChart.add(createChartItem("南宁通信车间", 2200));
        data.put("workshopChart", workshopChart);

        // 用工项目占比图表数据
        List<Map<String, Object>> projectChart = new ArrayList<>();
        projectChart.add(createChartItem("施工", 3500));
        projectChart.add(createChartItem("培训", 1800));
        projectChart.add(createChartItem("维修", 2200));
        projectChart.add(createChartItem("故障处理", 1500));
        projectChart.add(createChartItem("其他", 2800));
        data.put("projectChart", projectChart);

        return Result.ok(data);
    }

    /** 构造卡片数据 */
    private Map<String, Object> createCard(String title, String value, String unit, String color) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("title", title);
        card.put("value", value);
        card.put("unit", unit);
        card.put("color", color);
        return card;
    }

    /** 构造图表数据项 */
    private Map<String, Object> createChartItem(String name, Integer value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}
