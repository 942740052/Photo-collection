package com.photo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserManageVO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private Long photoCount;
    private Long storageUsed;
}
