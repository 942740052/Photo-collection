package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.dto.LoginDTO;
import com.photo.dto.RegisterDTO;
import com.photo.entity.User;
import com.photo.mapper.UserMapper;
import com.photo.util.JwtUtil;
import com.photo.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService extends ServiceImpl<UserMapper, User> {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public Map<String, Object> register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, dto.getEmail());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole("USER");
        user.setStatus(1);

        userMapper.insert(user);

        return generateTokenMap(user);
    }

    public Map<String, Object> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        return generateTokenMap(user);
    }

    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return convertToVO(user);
    }

    public UserVO updateUserInfo(Long userId, UserVO userVO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (userVO.getNickname() != null) {
            user.setNickname(userVO.getNickname());
        }
        if (userVO.getAvatar() != null) {
            user.setAvatar(userVO.getAvatar());
        }

        userMapper.updateById(user);
        return convertToVO(user);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private Map<String, Object> generateTokenMap(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.generateToken(user.getId(), user.getUsername()));
        result.put("refreshToken", jwtUtil.generateRefreshToken(user.getId()));
        result.put("user", convertToVO(user));
        return result;
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setLastLoginAt(user.getLastLoginAt());
        return vo;
    }
}
