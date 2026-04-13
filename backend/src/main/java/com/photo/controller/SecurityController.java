package com.photo.controller;

import com.photo.common.Result;
import com.photo.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/security")
@RequiredArgsConstructor
public class SecurityController {

    private final PhotoService photoService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/photos/{photoId}/protect")
    public Result<Void> protectPhoto(
            @RequestAttribute Long userId,
            @PathVariable Long photoId,
            @RequestBody Map<String, String> params) {
        String password = params.get("password");
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        photoService.protectPhoto(userId, photoId, passwordEncoder.encode(password));
        return Result.success("照片已加密保护", null);
    }

    @PostMapping("/photos/{photoId}/unprotect")
    public Result<Void> unprotectPhoto(
            @RequestAttribute Long userId,
            @PathVariable Long photoId) {
        photoService.unprotectPhoto(userId, photoId);
        return Result.success("已取消保护", null);
    }

    @PostMapping("/photos/{photoId}/verify")
    public Result<Boolean> verifyPhotoPassword(
            @PathVariable Long photoId,
            @RequestBody Map<String, String> params) {
        String password = params.get("password");
        boolean valid = photoService.verifyPhotoPassword(photoId, password);
        return Result.success(valid);
    }

    @PostMapping("/photos/batch-protect")
    public Result<Void> batchProtectPhotos(
            @RequestAttribute Long userId,
            @RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        java.util.List<Long> photoIds = (java.util.List<Long>) params.get("photoIds");
        String password = (String) params.get("password");
        if (photoIds == null || photoIds.isEmpty()) {
            throw new IllegalArgumentException("请选择照片");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        String encodedPassword = passwordEncoder.encode(password);
        for (Long photoId : photoIds) {
            photoService.protectPhoto(userId, photoId, encodedPassword);
        }
        return Result.success("批量加密保护成功", null);
    }
}
