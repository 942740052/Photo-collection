package com.photo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TagVO {
    private Long id;
    private Long userId;
    private String name;
    private String color;
    private Integer photoCount;
    private LocalDateTime createdAt;
}
