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
    public IPage<Employee> getPage(Integer pageNum, Integer pageSize, String keyword, Long workshopId, Long areaId, String status) {
        // 构建公用条件
        LambdaQueryWrapper<Employee> countWrapper = new LambdaQueryWrapper<Employee>()
                .like(keyword != null, Employee::getName, keyword)
                .eq(workshopId != null, Employee::getWorkshopId, workshopId)
                .eq(areaId != null, Employee::getAreaId, areaId)
                .eq(status != null && !status.isEmpty(), Employee::getEmployeeStatus, status)
                .eq(Employee::getEnabled, 1);

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

        if (UserContext.isWorkshopAdmin()) {
            dataWrapper.eq(Employee::getWorkshopId, UserContext.getOrgId());
        } else if (UserContext.isAreaReporter()) {
            dataWrapper.eq(Employee::getAreaId, UserContext.getOrgId());
        }

        List<Employee> records = employeeMapper.selectList(dataWrapper);

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
        transferRecordMapper.insert(record);

        // 更新人员当前归属
        emp.setWorkshopId(newWorkshopId);
        emp.setAreaId(newAreaId);
        if (newTeamName != null) emp.setTeamName(newTeamName);
        employeeMapper.updateById(emp);

        log.info("人员调动: employeeId={}, {}->{}", employeeId, record.getBeforeAreaId(), newAreaId);
        logService.record("人员管理", "TRANSFER", String.valueOf(employeeId),
                "人员调动: " + emp.getName() + " -> 工区" + newAreaId);
    }

    /** 查询调动记录 */
    public List<EmployeeTransferRecord> getTransferRecords(Long employeeId) {
        return transferRecordMapper.selectList(
                new LambdaQueryWrapper<EmployeeTransferRecord>()
                        .eq(EmployeeTransferRecord::getEmployeeId, employeeId)
                        .orderByDesc(EmployeeTransferRecord::getOperateTime));
    }
}
