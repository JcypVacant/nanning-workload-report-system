package com.cyp.nanningworkloadreportsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyp.nanningworkloadreportsystem.entity.MonthlyPeriod;
import com.cyp.nanningworkloadreportsystem.mapper.MonthlyPeriodMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 月度填报期间服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyPeriodService {

    private final MonthlyPeriodMapper periodMapper;

    /** 分页查询 */
    public IPage<MonthlyPeriod> getPage(Integer pageNum, Integer pageSize) {
        return periodMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MonthlyPeriod>().orderByDesc(MonthlyPeriod::getYear, MonthlyPeriod::getMonth));
    }

    /** 获取当前活跃月份 */
    public MonthlyPeriod getActive() {
        return periodMapper.selectOne(
                new LambdaQueryWrapper<MonthlyPeriod>()
                        .ne(MonthlyPeriod::getStatus, "已归档")
                        .ne(MonthlyPeriod::getStatus, "已锁定")
                        .orderByDesc(MonthlyPeriod::getYear, MonthlyPeriod::getMonth)
                        .last("LIMIT 1"));
    }

    /** 创建月度期间 */
    @Transactional
    public MonthlyPeriod create(Integer year, Integer month, LocalDate startDate, LocalDate endDate, LocalDate auditDeadline) {
        // 检查是否已存在
        Long count = periodMapper.selectCount(
                new LambdaQueryWrapper<MonthlyPeriod>()
                        .eq(MonthlyPeriod::getYear, year)
                        .eq(MonthlyPeriod::getMonth, month));
        if (count > 0) throw new RuntimeException("该月份已存在，不可重复创建");

        MonthlyPeriod period = new MonthlyPeriod();
        period.setPeriodName(year + "年" + month + "月");
        period.setYear(year);
        period.setMonth(month);
        period.setStatus("填报中");
        period.setStartDate(startDate);
        period.setEndDate(endDate);
        period.setAuditDeadline(auditDeadline);
        period.setLocked(0);
        periodMapper.insert(period);
        log.info("创建月度期间: {}", period.getPeriodName());
        return period;
    }

    /** 更新月度期间 */
    @Transactional
    public void update(MonthlyPeriod period) {
        periodMapper.updateById(period);
    }

    /**
     * 修改期间状态（含状态流转校验）
     * 未开始 -> 填报中 -> 车间审核中 -> 段级汇总中 -> 已锁定 -> 已归档
     */
    @Transactional
    public void changeStatus(Long id, String newStatus) {
        MonthlyPeriod period = periodMapper.selectById(id);
        if (period == null) throw new RuntimeException("月度期间不存在");

        // 校验状态流转是否合法
        String currentStatus = period.getStatus();
        boolean valid = switch (currentStatus) {
            case "未开始" -> "填报中".equals(newStatus);
            case "填报中" -> "车间审核中".equals(newStatus);
            case "车间审核中" -> "段级汇总中".equals(newStatus);
            case "段级汇总中" -> "已锁定".equals(newStatus) || "填报中".equals(newStatus); // 可回退
            case "已锁定" -> "已归档".equals(newStatus);
            default -> false;
        };

        if (!valid) {
            throw new RuntimeException("状态流转不合法：不能从 " + currentStatus + " 直接变更为 " + newStatus);
        }

        period.setStatus(newStatus);
        if ("已锁定".equals(newStatus)) period.setLocked(1);
        periodMapper.updateById(period);
        log.info("月度期间 {} 状态变更: {} -> {}", period.getPeriodName(), currentStatus, newStatus);
    }

    /** 锁定/解锁月份 */
    @Transactional
    public void toggleLock(Long id) {
        MonthlyPeriod period = periodMapper.selectById(id);
        if (period == null) throw new RuntimeException("月度期间不存在");
        period.setLocked(period.getLocked() == 1 ? 0 : 1);
        period.setStatus(period.getLocked() == 1 ? "已锁定" : "段级汇总中");
        periodMapper.updateById(period);
    }
}
