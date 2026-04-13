package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.entity.OperationLog;
import com.photo.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OperationLogService extends ServiceImpl<OperationLogMapper, OperationLog> {

    private final OperationLogMapper operationLogMapper;

    @Async
    public void log(Long userId, String operation, String module, String description, 
                    String ipAddress, String userAgent, String requestMethod, 
                    String requestUrl, String requestParams, Integer responseStatus, Long executionTime) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setOperation(operation);
        log.setModule(module);
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setRequestMethod(requestMethod);
        log.setRequestUrl(requestUrl);
        log.setRequestParams(requestParams);
        log.setResponseStatus(responseStatus);
        log.setExecutionTime(executionTime);
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    public Page<OperationLog> getLogList(Long userId, Integer pageNum, Integer pageSize, 
                                          String module, String operation, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getUserId, userId);
        
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        
        if (operation != null && !operation.isEmpty()) {
            wrapper.like(OperationLog::getOperation, operation);
        }
        
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreatedAt, startTime);
        }
        
        if (endTime != null) {
            wrapper.le(OperationLog::getCreatedAt, endTime);
        }
        
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        return operationLogMapper.selectPage(page, wrapper);
    }

    public List<Map<String, Object>> getOperationStats(Long userId) {
        return operationLogMapper.selectOperationStats(userId);
    }

    public void clearLogs(Long userId, LocalDateTime before) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getUserId, userId);
        if (before != null) {
            wrapper.lt(OperationLog::getCreatedAt, before);
        }
        operationLogMapper.delete(wrapper);
    }
}
