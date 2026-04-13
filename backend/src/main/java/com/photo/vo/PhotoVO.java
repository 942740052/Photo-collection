package com.photo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PhotoVO {
    private Long id;
    private Long userId;
    private Long albumId;
    private String albumName;
    private String title;
    private String description;
    private String filePath;
    private String thumbnailPath;
    private Long fileSize;
    private String fileType;
    private Integer width;
    private Integer height;
    private LocalDateTime shootTime;
    private String cameraModel;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;
    private Integer isFavorite;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private List<TagVO> tags;
}
