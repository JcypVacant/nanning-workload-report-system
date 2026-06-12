package com.cyp.nanningworkloadreportsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyp.nanningworkloadreportsystem.common.PageResult;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.OperationLog;
import com.cyp.nanningworkloadreportsystem.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志", description = "查询系统操作日志")
@RestController
@RequestMapping("/api/v1/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/page")
    public Result<PageResult<OperationLog>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        // 未指定时间范围时默认最近90天，防止全表扫描
        if ((startTime == null || startTime.isEmpty()) && (endTime == null || endTime.isEmpty())) {
            startTime = LocalDate.now().minusDays(90).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            endTime = LocalDate.now().plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .eq(moduleName != null, OperationLog::getModuleName, moduleName)
                .like(keyword != null && !keyword.isEmpty(), OperationLog::getUsername, keyword)
                .ge(startTime != null && !startTime.isEmpty(), OperationLog::getOperateTime, startTime)
                .le(endTime != null && !endTime.isEmpty(), OperationLog::getOperateTime, endTime)
                .orderByDesc(OperationLog::getOperateTime);
        // 手动分页（MyBatis Plus 3.5.15 无 PaginationInnerInterceptor）
        Long total = operationLogMapper.selectCount(wrapper);
        List<OperationLog> records = operationLogMapper.selectList(
                wrapper.last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize));
        return Result.ok(PageResult.of(total, pageNum, pageSize, records));
    }
}
