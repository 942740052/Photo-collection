package com.photo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {
    private Long id;
    private String operation;
    private String module;
    private String description;
    private String ipAddress;
    private String requestMethod;
    private String requestUrl;
    private Integer responseStatus;
    private Long executionTime;
    private LocalDateTime createdAt;
}
