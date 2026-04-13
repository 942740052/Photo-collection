package com.photo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
