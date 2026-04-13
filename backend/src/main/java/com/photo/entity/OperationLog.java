package com.photo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String operation;
    
    private String module;
    
    private String description;
    
    private String ipAddress;
    
    private String userAgent;
    
    private String requestMethod;
    
    private String requestUrl;
    
    private String requestParams;
    
    private Integer responseStatus;
    
    private Long executionTime;
    
    private LocalDateTime createdAt;
}
