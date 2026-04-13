package com.photo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BackupVO {
    private Long id;
    private String name;
    private String type;
    private String status;
    private Long fileSize;
    private Integer photoCount;
    private Integer albumCount;
    private Integer tagCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private LocalDateTime createdAt;
}
