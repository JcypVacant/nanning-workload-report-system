package com.cyp.nanningworkloadreportsystem.controller;

import com.cyp.nanningworkloadreportsystem.service.ExcelExportService;
import com.cyp.nanningworkloadreportsystem.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Excel 导出控制器
 * 提供工区、车间、段级三个级别的 Excel 导出
 * 数据范围根据当前登录用户角色自动限制
 */
@Tag(name = "Excel导出", description = "工区/车间/段级 Excel 报表导出")
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExcelExportController {

    private final ExcelExportService excelExportService;
    private final OperationLogService operationLogService;

    @Operation(summary = "导出工区数据")
    @GetMapping("/area/{areaId}")
    public ResponseEntity<byte[]> exportArea(@PathVariable Long areaId,
                                              @RequestParam Long periodId) {
        byte[] bytes = excelExportService.exportArea(periodId, areaId);
        operationLogService.record("Excel导出", "EXPORT", "area:" + areaId,
                "导出工区数据: periodId=" + periodId + ", areaId=" + areaId);
        return buildFileResponse(bytes, "工区填报数据_" + timestamp() + ".xlsx");
    }

    @Operation(summary = "导出车间数据")
    @GetMapping("/workshop/{workshopId}")
    public ResponseEntity<byte[]> exportWorkshop(@PathVariable Long workshopId,
                                                  @RequestParam Long periodId) {
        byte[] bytes = excelExportService.exportWorkshop(periodId, workshopId);
        operationLogService.record("Excel导出", "EXPORT", "workshop:" + workshopId,
                "导出车间数据: periodId=" + periodId + ", workshopId=" + workshopId);
        return buildFileResponse(bytes, "车间填报数据_" + timestamp() + ".xlsx");
    }

    @Operation(summary = "导出全段数据")
    @GetMapping("/section")
    public ResponseEntity<byte[]> exportSection(@RequestParam Long periodId) {
        byte[] bytes = excelExportService.exportSection(periodId);
        operationLogService.record("Excel导出", "EXPORT", "section",
                "导出全段数据: periodId=" + periodId);
        return buildFileResponse(bytes, "全段填报数据_" + timestamp() + ".xlsx");
    }

    /** 构建文件下载响应 */
    private ResponseEntity<byte[]> buildFileResponse(byte[] bytes, String filename) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
