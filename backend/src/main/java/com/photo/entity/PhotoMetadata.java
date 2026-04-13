package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_photo_metadata")
public class PhotoMetadata implements Serializable {
    @TableId(type = IdType.AUTO)
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
