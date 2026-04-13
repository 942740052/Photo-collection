package com.photo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareCreateDTO {
    private Long photoId;
    private Long albumId;
    private String password;
    private Integer expireHours;
}
