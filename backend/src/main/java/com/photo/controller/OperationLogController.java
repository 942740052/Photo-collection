package com.photo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.Result;
import com.photo.entity.OperationLog;
import com.photo.service.OperationLogService;
import com.photo.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public Result<List<OperationLogVO>> getLogList(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Page<OperationLog> page = operationLogService.getLogList(userId, pageNum, pageSize, module, operation, startTime, endTime);
        List<OperationLogVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> getOperationStats(@RequestAttribute Long userId) {
        return Result.success(operationLogService.getOperationStats(userId));
    }

    @DeleteMapping
    public Result<Void> clearLogs(
            @RequestAttribute Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before) {
        operationLogService.clearLogs(userId, before);
        return Result.success("清除成功", null);
    }

    private OperationLogVO convertToVO(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }
}
