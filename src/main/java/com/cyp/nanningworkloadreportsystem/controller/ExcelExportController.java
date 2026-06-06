package com.cyp.nanningworkloadreportsystem.controller;

import com.cyp.nanningworkloadreportsystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Excel 导出控制器
 * 提供工区、车间、段级三个级别的 Excel 导出
 */
@Tag(name = "Excel导出", description = "工区/车间/段级 Excel 报表导出")
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExcelExportController {

    @Operation(summary = "导出工区数据")
    @GetMapping("/area/{areaId}")
    public Result<String> exportArea(@PathVariable Long areaId,
                                      @RequestParam Long periodId) {
        // TODO: 集成 EasyExcel 实现多级表头导出
        return Result.ok("工区Excel导出功能开发中，areaId=" + areaId + ", periodId=" + periodId);
    }

    @Operation(summary = "导出车间数据")
    @GetMapping("/workshop/{workshopId}")
    public Result<String> exportWorkshop(@PathVariable Long workshopId,
                                          @RequestParam Long periodId) {
        return Result.ok("车间Excel导出功能开发中，workshopId=" + workshopId + ", periodId=" + periodId);
    }

    @Operation(summary = "导出全段数据")
    @GetMapping("/section")
    public Result<String> exportSection(@RequestParam Long periodId,
                                         @RequestParam(required = false) String workshopIds) {
        return Result.ok("段级Excel导出功能开发中，periodId=" + periodId);
    }
}
