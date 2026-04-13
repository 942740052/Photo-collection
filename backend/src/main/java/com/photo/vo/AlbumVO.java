package com.photo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlbumVO {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private Long coverPhotoId;
    private String coverPhotoUrl;
    private Integer photoCount;
    private Integer isPublic;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
