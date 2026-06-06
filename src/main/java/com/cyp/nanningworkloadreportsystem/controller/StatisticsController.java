package com.cyp.nanningworkloadreportsystem.controller;

import com.cyp.nanningworkloadreportsystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 统计分析控制器
 * 提供多维度统计数据（按车间/工区/人员/项目/月份/状态等）
 */
@Tag(name = "统计分析", description = "多维度统计数据和图表数据")
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    @Operation(summary = "按车间统计")
    @GetMapping("/by-workshop")
    public Result<List<Map<String, Object>>> getByWorkshop(
            @RequestParam(required = false) Long periodId) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createItem("贺州通信车间", 3200, 850));
        list.add(createItem("桂林通信车间", 2800, 720));
        list.add(createItem("柳州通信车间", 1900, 510));
        list.add(createItem("南宁通信车间", 2200, 580));
        return Result.ok(list);
    }

    @Operation(summary = "按工区统计")
    @GetMapping("/by-area")
    public Result<List<Map<String, Object>>> getByArea(
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long workshopId) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createItem("梧州通信工区", 980, 260));
        list.add(createItem("道州通信工区", 750, 190));
        list.add(createItem("贺州通信工区", 840, 220));
        list.add(createItem("岑溪通信工区", 630, 180));
        return Result.ok(list);
    }

    @Operation(summary = "按人员统计（排名）")
    @GetMapping("/by-employee")
    public Result<List<Map<String, Object>>> getByEmployee(
            @RequestParam(required = false) Long periodId) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createItem("张三", 580, 150));
        list.add(createItem("李四", 520, 140));
        list.add(createItem("王五", 480, 130));
        list.add(createItem("赵六", 450, 120));
        list.add(createItem("陈七", 420, 110));
        return Result.ok(list);
    }

    @Operation(summary = "按用工项目统计（占比）")
    @GetMapping("/by-project")
    public Result<List<Map<String, Object>>> getByProject(
            @RequestParam(required = false) Long periodId) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createItem("施工", 3500, 0));
        list.add(createItem("培训", 1800, 0));
        list.add(createItem("维修", 2200, 0));
        list.add(createItem("故障处理", 1500, 0));
        list.add(createItem("防洪", 800, 0));
        list.add(createItem("值班", 600, 0));
        list.add(createItem("其他", 2180, 0));
        return Result.ok(list);
    }

    @Operation(summary = "按月份趋势统计")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("months", Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月"));
        data.put("hours", Arrays.asList(8900, 10200, 11500, 9800, 12400, 12580));
        data.put("points", Arrays.asList(2400, 2800, 3100, 2600, 3200, 3260));
        return Result.ok(data);
    }

    @Operation(summary = "按填报状态统计")
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatusStats(
            @RequestParam(required = false) Long periodId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("draft", 12);
        data.put("submitted", 8);
        data.put("returned", 3);
        data.put("approved", 45);
        data.put("locked", 0);
        return Result.ok(data);
    }

    private Map<String, Object> createItem(String name, Integer hours, Integer points) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("hours", hours);
        item.put("points", points);
        return item;
    }
}
