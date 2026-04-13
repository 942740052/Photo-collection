package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_photo_tag")
public class PhotoTag implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long photoId;

    private Long tagId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
