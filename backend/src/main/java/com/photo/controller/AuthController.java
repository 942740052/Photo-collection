package com.photo.controller;

import com.photo.common.Result;
import com.photo.dto.LoginDTO;
import com.photo.dto.RegisterDTO;
import com.photo.service.AuthService;
import com.photo.service.PhotoService;
import com.photo.service.AlbumService;
import com.photo.service.ShareService;
import com.photo.vo.UserVO;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PhotoService photoService;
    private final AlbumService albumService;
    private final ShareService shareService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        Map<String, Object> result = authService.register(dto);
        return Result.success("注册成功", result);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        Map<String, Object> result = authService.login(dto);
        return Result.success("登录成功", result);
    }

    @GetMapping("/profile")
    public Result<UserVO> getProfile(@RequestAttribute Long userId) {
        UserVO userVO = authService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestAttribute Long userId, @RequestBody UserVO userVO) {
        UserVO result = authService.updateUserInfo(userId, userVO);
        return Result.success("更新成功", result);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(
            @RequestAttribute Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        authService.updatePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats(@RequestAttribute Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("photoCount", photoService.countByUserId(userId));
        stats.put("albumCount", albumService.countByUserId(userId));
        stats.put("shareCount", shareService.countByUserId(userId));
        return Result.success(stats);
    }
}
