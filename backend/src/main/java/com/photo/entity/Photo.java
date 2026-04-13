package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_photo")
public class Photo implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long albumId;

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

    private String location;

    private BigDecimal gpsLatitude;

    private BigDecimal gpsLongitude;

    private Integer isDeleted;

    private Integer isFavorite;

    private Integer isPrivate;

    private String privatePassword;

    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
