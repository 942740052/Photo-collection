package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_tag")
public class Tag implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String color;

    private Integer photoCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
