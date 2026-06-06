package com.cyp.nanningworkloadreportsystem.service;

import com.cyp.nanningworkloadreportsystem.entity.OperationLog;
import com.cyp.nanningworkloadreportsystem.mapper.OperationLogMapper;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务类
 * 负责记录系统中的所有关键操作到 operation_log 表
 * 所有写操作（增删改、提交、审核、导出等）都应调用此服务记录日志
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final HttpServletRequest request;

    /**
     * 记录操作日志
     *
     * @param moduleName    操作模块（如：工区填报/车间审核/账号管理）
     * @param operationType 操作类型（LOGIN/CREATE/UPDATE/DELETE/SUBMIT/APPROVE/RETURN/EXPORT）
     * @param targetId      操作目标ID（可为null）
     * @param summary       操作摘要（人可读的描述）
     */
    public void record(String moduleName, String operationType, String targetId, String summary) {
        record(moduleName, operationType, targetId, summary, null, null);
    }

    /**
     * 记录操作日志（含操作前后数据对比）
     */
    public void record(String moduleName, String operationType, String targetId,
                       String summary, String beforeJson, String afterJson) {
        try {
            OperationLog logEntry = new OperationLog();
            logEntry.setUserId(UserContext.getUserId());
            logEntry.setUsername(UserContext.getUsername());
            logEntry.setModuleName(moduleName);
            logEntry.setOperationType(operationType);
            logEntry.setTargetId(targetId);
            logEntry.setSummary(summary);
            logEntry.setBeforeJson(beforeJson);
            logEntry.setAfterJson(afterJson);
            logEntry.setIp(getClientIp());
            logEntry.setOperateTime(LocalDateTime.now());
            operationLogMapper.insert(logEntry);
        } catch (Exception e) {
            // 日志记录失败不应影响主业务，仅打印错误
            log.error("操作日志写入失败: module={}, type={}, summary={}", moduleName, operationType, summary, e);
        }
    }

    /** 获取客户端真实IP */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
