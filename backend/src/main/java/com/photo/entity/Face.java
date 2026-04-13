package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_face")
public class Face implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long photoId;

    private String faceToken;

    private String faceGroupId;

    private String personName;

    private String position;

    private BigDecimal confidence;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
