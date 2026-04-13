package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_ai_recognition")
public class AiRecognition implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long photoId;

    private String type;

    private String result;

    private BigDecimal confidence;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
