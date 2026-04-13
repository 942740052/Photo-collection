package com.photo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_backup")
public class Backup {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String name;
    
    private String type;
    
    private String status;
    
    private String filePath;
    
    private Long fileSize;
    
    private Integer photoCount;
    
    private Integer albumCount;
    
    private Integer tagCount;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    private String errorMessage;
    
    private LocalDateTime createdAt;
}
