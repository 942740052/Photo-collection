package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_share")
public class Share implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long photoId;

    private Long albumId;

    private String shareCode;

    private String password;

    private LocalDateTime expireAt;

    private Integer viewCount;

    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
