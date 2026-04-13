package com.photo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PhotoMetadataVO {
    private Long id;
    private Long photoId;
    private Integer iso;
    private String aperture;
    private String shutterSpeed;
    private String focalLength;
    private Integer flash;
    private String whiteBalance;
    private String exposureMode;
    private String meteringMode;
    private LocalDateTime createdAt;
}
