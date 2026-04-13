package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_share_access")
public class ShareAccess implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shareId;

    private String ipAddress;

    private String userAgent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime accessedAt;
}
