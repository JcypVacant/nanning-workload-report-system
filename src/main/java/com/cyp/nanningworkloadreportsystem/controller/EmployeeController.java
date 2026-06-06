package com.cyp.nanningworkloadreportsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyp.nanningworkloadreportsystem.common.PageResult;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.Employee;
import com.cyp.nanningworkloadreportsystem.entity.EmployeeTransferRecord;
import com.cyp.nanningworkloadreportsystem.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 人员管理控制器
 */
@Tag(name = "人员管理", description = "人员信息增删改查、调动")
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "分页查询人员")
    @GetMapping("/page")
    public Result<PageResult<Employee>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long workshopId,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String status) {
        IPage<Employee> page = employeeService.getPage(pageNum, pageSize, keyword, workshopId, areaId, status);
        return Result.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @Operation(summary = "根据工区查询人员")
    @GetMapping("/by-area/{areaId}")
    public Result<List<Employee>> getByArea(@PathVariable Long areaId) {
        return Result.ok(employeeService.getByArea(areaId));
    }

    @Operation(summary = "新增人员")
    @PostMapping
    public Result<Employee> create(@RequestBody Employee employee) {
        return Result.ok(employeeService.create(employee));
    }

    @Operation(summary = "更新人员")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        employeeService.update(employee);
        return Result.ok();
    }

    @Operation(summary = "启停人员")
    @PatchMapping("/{id}/toggle")
    public Result<Void> toggleEnabled(@PathVariable Long id) {
        employeeService.toggleEnabled(id);
        return Result.ok();
    }

    @Operation(summary = "人员调动")
    @PostMapping("/transfer")
    public Result<Void> transfer(@RequestBody TransferRequest req) {
        employeeService.transfer(req.getEmployeeId(), req.getAfterWorkshopId(),
                req.getAfterAreaId(), req.getAfterTeamName(), req.getTransferReason());
        return Result.ok();
    }

    @Operation(summary = "查看调动记录")
    @GetMapping("/{id}/transfer-records")
    public Result<List<EmployeeTransferRecord>> getTransferRecords(@PathVariable Long id) {
        return Result.ok(employeeService.getTransferRecords(id));
    }

    @Data
    public static class TransferRequest {
        private Long employeeId;
        private Long afterWorkshopId;
        private Long afterAreaId;
        private String afterTeamName;
        private String transferReason;
    }
}
