package com.photo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareVO {
    private Long id;
    private Long userId;
    private Long photoId;
    private Long albumId;
    private String shareCode;
    private Boolean hasPassword;
    private LocalDateTime expireAt;
    private Integer viewCount;
    private Integer isActive;
    private LocalDateTime createdAt;
    private String photoTitle;
    private String albumName;
}
