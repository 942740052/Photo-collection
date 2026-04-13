package com.photo.dto;

import lombok.Data;

@Data
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private Integer status;
}
