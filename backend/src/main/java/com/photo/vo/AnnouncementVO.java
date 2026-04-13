package com.photo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementVO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
