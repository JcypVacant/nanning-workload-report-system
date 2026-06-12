package com.cyp.nanningworkloadreportsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.*;
import com.cyp.nanningworkloadreportsystem.mapper.*;
import com.cyp.nanningworkloadreportsystem.service.StatisticsService;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
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
    private final WorkReportMapper reportMapper;
    private final EmployeeTransferRecordMapper transferRecordMapper;
    private final EmployeeMapper employeeMapper;
    private final OrgUnitMapper orgUnitMapper;

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

    @Operation(summary = "获取待办提醒数量")
    @GetMapping("/dashboard/notifications")
    public Result<List<Map<String, Object>>> getNotifications() {
        List<Map<String, Object>> items = new ArrayList<>();
        Long orgId = UserContext.getOrgId();

        if (UserContext.isSectionAdmin()) {
            // 待审核调动申请
            Long pendingTransfers = transferRecordMapper.selectCount(
                    new LambdaQueryWrapper<EmployeeTransferRecord>().eq(EmployeeTransferRecord::getStatus, "待审核"));
            items.add(createNotif("待审核调动", String.valueOf(pendingTransfers), "#e6a23c", "调动申请等待审核"));

            // 待审核车间本级数据
            // 查询 WORKSHOP_LEVEL 工区 ID
            List<Long> wlIds = orgUnitMapper.selectList(
                    new LambdaQueryWrapper<OrgUnit>().eq(OrgUnit::getOrgType, "WORKSHOP_LEVEL")
            ).stream().map(OrgUnit::getId).toList();
            LambdaQueryWrapper<WorkReport> pendingWp = new LambdaQueryWrapper<WorkReport>().eq(WorkReport::getStatus, "已提交");
            if (!wlIds.isEmpty()) pendingWp.in(WorkReport::getAreaId, wlIds);
            else pendingWp.isNull(WorkReport::getAreaId);
            Long pendingSection = reportMapper.selectCount(pendingWp);
            items.add(createNotif("待审核车间数据", String.valueOf(pendingSection), "#409eff", "车间本级提交待审核"));
        } else if (UserContext.isWorkshopAdmin()) {
            // 待审核工区数据
            Long pending = reportMapper.selectCount(
                    new LambdaQueryWrapper<WorkReport>().eq(WorkReport::getStatus, "已提交").eq(WorkReport::getWorkshopId, orgId));
            items.add(createNotif("待审核", String.valueOf(pending), "#e6a23c", "工区数据等待审核"));

            // 未填报人数
            List<Long> reported = reportMapper.selectList(
                    new LambdaQueryWrapper<WorkReport>().select(WorkReport::getEmployeeId).eq(WorkReport::getWorkshopId, orgId).groupBy(WorkReport::getEmployeeId)
            ).stream().map(WorkReport::getEmployeeId).distinct().toList();
            Long totalEmp = employeeMapper.selectCount(
                    new LambdaQueryWrapper<Employee>().eq(Employee::getWorkshopId, orgId).eq(Employee::getEnabled, 1).eq(Employee::getEmployeeStatus, "在岗"));
            items.add(createNotif("未填报人数", String.valueOf(totalEmp - reported.size()), "#f56c6c", "本月尚未填报的人员"));
        } else {
            // 工区填报员：草稿 + 退回
            Long drafts = reportMapper.selectCount(
                    new LambdaQueryWrapper<WorkReport>().eq(WorkReport::getStatus, "草稿").eq(WorkReport::getAreaId, orgId));
            items.add(createNotif("草稿", String.valueOf(drafts), "#909399", "待提交的草稿记录"));

            Long returned = reportMapper.selectCount(
                    new LambdaQueryWrapper<WorkReport>().eq(WorkReport::getStatus, "已退回").eq(WorkReport::getAreaId, orgId));
            items.add(createNotif("已退回", String.valueOf(returned), "#f56c6c", "被退回需要修改的记录"));
        }

        return Result.ok(items);
    }

    private Map<String, Object> createNotif(String title, String value, String color, String desc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", title);
        m.put("value", value);
        m.put("color", color);
        m.put("desc", desc);
        return m;
    }

    private String formatNumber(double v) {
        if (v >= 10000) return String.format("%.1f万", v / 10000);
        if (v >= 1000) return String.format("%.0f", v);
        return String.format("%.1f", v);
    }
}
