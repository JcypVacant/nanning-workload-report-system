package com.cyp.nanningworkloadreportsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyp.nanningworkloadreportsystem.entity.Employee;
import com.cyp.nanningworkloadreportsystem.entity.EmployeeTransferRecord;
import com.cyp.nanningworkloadreportsystem.entity.OrgUnit;
import com.cyp.nanningworkloadreportsystem.mapper.EmployeeMapper;
import com.cyp.nanningworkloadreportsystem.mapper.EmployeeTransferRecordMapper;
import com.cyp.nanningworkloadreportsystem.mapper.OrgUnitMapper;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人员管理服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeTransferRecordMapper transferRecordMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final OperationLogService logService;

    /** 分页查询人员（手动分页：先COUNT再LIMIT） */
    public IPage<Employee> getPage(Integer pageNum, Integer pageSize, String keyword, Long workshopId, Long areaId, String status, Boolean workshopLevel) {
        // 构建公用条件
        LambdaQueryWrapper<Employee> countWrapper = new LambdaQueryWrapper<Employee>()
                .like(keyword != null, Employee::getName, keyword)
                .eq(workshopId != null, Employee::getWorkshopId, workshopId)
                .eq(areaId != null, Employee::getAreaId, areaId)
                .eq(status != null && !status.isEmpty(), Employee::getEmployeeStatus, status)
                .eq(Employee::getEnabled, 1);

        // 车间本级人员
        if (workshopLevel != null && workshopLevel) {
            countWrapper.isNull(Employee::getAreaId);
        }

        // 数据范围
        if (UserContext.isWorkshopAdmin()) {
            countWrapper.eq(Employee::getWorkshopId, UserContext.getOrgId());
        } else if (UserContext.isAreaReporter()) {
            countWrapper.eq(Employee::getAreaId, UserContext.getOrgId());
        }

        // 1. 查总数
        Long total = employeeMapper.selectCount(countWrapper);

        // 2. 查当前页
        LambdaQueryWrapper<Employee> dataWrapper = new LambdaQueryWrapper<Employee>()
                .like(keyword != null, Employee::getName, keyword)
                .eq(workshopId != null, Employee::getWorkshopId, workshopId)
                .eq(areaId != null, Employee::getAreaId, areaId)
                .eq(status != null && !status.isEmpty(), Employee::getEmployeeStatus, status)
                .eq(Employee::getEnabled, 1)
                .orderByDesc(Employee::getCreateTime)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);

        if (workshopLevel != null && workshopLevel) {
            dataWrapper.isNull(Employee::getAreaId);
        }

        if (UserContext.isWorkshopAdmin()) {
            dataWrapper.eq(Employee::getWorkshopId, UserContext.getOrgId());
        } else if (UserContext.isAreaReporter()) {
            dataWrapper.eq(Employee::getAreaId, UserContext.getOrgId());
        }

        List<Employee> records = employeeMapper.selectList(dataWrapper);

        // 填充关联字段（工区名称、车间名称）
        for (Employee emp : records) {
            if (emp.getAreaId() != null) {
                OrgUnit area = orgUnitMapper.selectById(emp.getAreaId());
                if (area != null) emp.setAreaName(area.getOrgName());
            }
            if (emp.getWorkshopId() != null) {
                OrgUnit workshop = orgUnitMapper.selectById(emp.getWorkshopId());
                if (workshop != null) emp.setWorkshopName(workshop.getOrgName());
            }
        }

        Page<Employee> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /** 根据工区ID查询人员（含数据权限校验） */
    public List<Employee> getByArea(Long areaId) {
        // 数据权限校验：工区填报员只能查看自己工区的人员
        if (UserContext.isAreaReporter()) {
            Long userAreaId = UserContext.getOrgId();
            if (userAreaId == null || !userAreaId.equals(areaId)) {
                throw new RuntimeException("无权查看其他工区的人员信息");
            }
        }
        // 车间管理员只能查看本车间下属工区的人员
        if (UserContext.isWorkshopAdmin()) {
            // 需要先验证该工区是否属于本车间
            OrgUnit area = orgUnitMapper.selectById(areaId);
            if (area == null || !UserContext.getOrgId().equals(area.getParentId())) {
                throw new RuntimeException("无权查看其他车间的人员信息");
            }
        }
        return employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>()
                        .eq(Employee::getAreaId, areaId)
                        .eq(Employee::getEnabled, 1)
                        .orderByAsc(Employee::getName));
    }

    /** 新增人员 */
    @Transactional
    public Employee create(Employee employee) {
        if (employee.getEmployeeStatus() == null) employee.setEmployeeStatus("在岗");
        if (employee.getEnabled() == null) employee.setEnabled(1);
        employeeMapper.insert(employee);
        log.info("新增人员: {}", employee.getName());
        logService.record("人员管理", "CREATE", String.valueOf(employee.getId()), "新增人员: " + employee.getName());
        return employee;
    }

    /** 更新人员信息 */
    @Transactional
    public void update(Employee employee) {
        employeeMapper.updateById(employee);
        log.info("更新人员: ID={}", employee.getId());
        logService.record("人员管理", "UPDATE", String.valueOf(employee.getId()), "更新人员: " + employee.getName());
    }

    /** 停用人员 */
    public void toggleEnabled(Long id) {
        Employee emp = employeeMapper.selectById(id);
        if (emp != null) {
            emp.setEnabled(emp.getEnabled() == 1 ? 0 : 1);
            employeeMapper.updateById(emp);
            logService.record("人员管理", "UPDATE", String.valueOf(id),
                    (emp.getEnabled() == 1 ? "启用" : "停用") + "人员: " + emp.getName());
        }
    }

    /** 人员调动 */
    @Transactional
    public void transfer(Long employeeId, Long newWorkshopId, Long newAreaId, String newTeamName, String reason) {
        Employee emp = employeeMapper.selectById(employeeId);
        if (emp == null) throw new RuntimeException("人员不存在");

        // 权限校验：车间管理员只能调动本车间的人员
        if (UserContext.isWorkshopAdmin()) {
            if (!UserContext.getOrgId().equals(emp.getWorkshopId())) {
                throw new RuntimeException("无权调动其他车间的人员");
            }
            if (!UserContext.getOrgId().equals(newWorkshopId)) {
                throw new RuntimeException("只能调动到本车间内");
            }
            if (newAreaId != null && newAreaId != 0L) {
                OrgUnit targetArea = orgUnitMapper.selectById(newAreaId);
                if (targetArea == null || !UserContext.getOrgId().equals(targetArea.getParentId())) {
                    throw new RuntimeException("目标工区不属于本车间");
                }
            }
        }

        if (newAreaId != null && newAreaId == 0L) newAreaId = null;

        // 创建调动记录
        EmployeeTransferRecord record = new EmployeeTransferRecord();
        record.setEmployeeId(employeeId);
        record.setBeforeWorkshopId(emp.getWorkshopId());
        record.setBeforeAreaId(emp.getAreaId());
        record.setBeforeTeamName(emp.getTeamName());
        record.setAfterWorkshopId(newWorkshopId);
        record.setAfterAreaId(newAreaId);
        record.setAfterTeamName(newTeamName);
        record.setTransferReason(reason);
        record.setOperatorId(UserContext.getUserId());
        record.setOperateTime(LocalDateTime.now());

        if (UserContext.isWorkshopAdmin()) {
            // 车间管理员提交 → 待段级审核
            record.setStatus("待审核");
            transferRecordMapper.insert(record);
            log.info("人员调动申请: employeeId={}, pending", employeeId);
            logService.record("人员管理", "TRANSFER_REQUEST", String.valueOf(employeeId),
                    "调动申请: " + emp.getName());
        } else {
            // 段级管理员直接调动
            record.setStatus("已通过");
            transferRecordMapper.insert(record);
            // 立即更新人员归属
            emp.setWorkshopId(newWorkshopId);
            emp.setAreaId(newAreaId);
            if (newTeamName != null) emp.setTeamName(newTeamName);
            employeeMapper.updateById(emp);
            log.info("人员调动: employeeId={}, {}->{}", employeeId, record.getBeforeAreaId(), newAreaId);
            logService.record("人员管理", "TRANSFER", String.valueOf(employeeId),
                    "人员调动: " + emp.getName());
        }
    }

    /** 段级管理员审核调动申请 */
    @Transactional
    public void approveTransfer(Long recordId, boolean approved, String comment) {
        EmployeeTransferRecord record = transferRecordMapper.selectById(recordId);
        if (record == null) throw new RuntimeException("调动记录不存在");
        if (!"待审核".equals(record.getStatus())) throw new RuntimeException("该调动已处理");

        record.setStatus(approved ? "已通过" : "已退回");
        record.setApprovedBy(UserContext.getUserId());
        record.setApproveTime(LocalDateTime.now());
        record.setApproveComment(comment);
        transferRecordMapper.updateById(record);

        if (approved) {
            Employee emp = employeeMapper.selectById(record.getEmployeeId());
            if (emp != null) {
                emp.setWorkshopId(record.getAfterWorkshopId());
                emp.setAreaId(record.getAfterAreaId());
                if (record.getAfterTeamName() != null) emp.setTeamName(record.getAfterTeamName());
                employeeMapper.updateById(emp);
            }
        }

        log.info("调动审核: recordId={}, approved={}", recordId, approved);
        logService.record("人员管理", approved ? "TRANSFER_APPROVE" : "TRANSFER_REJECT",
                String.valueOf(recordId), "调动审核: " + (approved ? "通过" : "退回"));
    }

    /** 查询调动记录（车间管理员看本车间，段级看全部） */
    public IPage<EmployeeTransferRecord> getTransferRecordsPage(Integer pageNum, Integer pageSize, String status) {
        LambdaQueryWrapper<EmployeeTransferRecord> countWrapper = new LambdaQueryWrapper<EmployeeTransferRecord>()
                .eq(status != null && !status.isEmpty(), EmployeeTransferRecord::getStatus, status);

        if (UserContext.isWorkshopAdmin()) {
            // 只查本车间人员的调动记录
            List<Long> empIds = employeeMapper.selectList(
                    new LambdaQueryWrapper<Employee>().eq(Employee::getWorkshopId, UserContext.getOrgId())
            ).stream().map(Employee::getId).toList();
            if (empIds.isEmpty()) {
                Page<EmployeeTransferRecord> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0L); empty.setRecords(List.of()); return empty;
            }
            countWrapper.in(EmployeeTransferRecord::getEmployeeId, empIds);
        }

        Long total = transferRecordMapper.selectCount(countWrapper);

        LambdaQueryWrapper<EmployeeTransferRecord> dataWrapper = new LambdaQueryWrapper<EmployeeTransferRecord>()
                .eq(status != null && !status.isEmpty(), EmployeeTransferRecord::getStatus, status);
        if (UserContext.isWorkshopAdmin()) {
            List<Long> empIds = employeeMapper.selectList(
                    new LambdaQueryWrapper<Employee>().eq(Employee::getWorkshopId, UserContext.getOrgId())
            ).stream().map(Employee::getId).toList();
            if (!empIds.isEmpty()) dataWrapper.in(EmployeeTransferRecord::getEmployeeId, empIds);
        }
        dataWrapper.orderByDesc(EmployeeTransferRecord::getOperateTime)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);
        List<EmployeeTransferRecord> records = transferRecordMapper.selectList(dataWrapper);

        // 填充关联信息
        for (EmployeeTransferRecord r : records) {
            Employee e = employeeMapper.selectById(r.getEmployeeId());
            if (e != null) r.setRemark(e.getName());
            // 填充调动前后工区/车间名称
            if (r.getBeforeAreaId() != null) {
                OrgUnit ba = orgUnitMapper.selectById(r.getBeforeAreaId());
                if (ba != null) r.setRemark(r.getRemark() + "|" + ba.getOrgName());
            }
            if (r.getAfterAreaId() != null) {
                OrgUnit aa = orgUnitMapper.selectById(r.getAfterAreaId());
                if (aa != null) r.setRemark(r.getRemark() + "|" + aa.getOrgName());
            }
        }

        Page<EmployeeTransferRecord> page = new Page<>(pageNum, pageSize);
        page.setTotal(total); page.setRecords(records);
        return page;
    }

    /** 查询调动记录 */
    public List<EmployeeTransferRecord> getTransferRecords(Long employeeId) {
        return transferRecordMapper.selectList(
                new LambdaQueryWrapper<EmployeeTransferRecord>()
                        .eq(EmployeeTransferRecord::getEmployeeId, employeeId)
                        .orderByDesc(EmployeeTransferRecord::getOperateTime));
    }
}
