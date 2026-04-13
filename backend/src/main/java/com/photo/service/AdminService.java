package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.common.PageResult;
import com.photo.dto.UserQueryDTO;
import com.photo.entity.Photo;
import com.photo.entity.User;
import com.photo.mapper.PhotoMapper;
import com.photo.mapper.UserMapper;
import com.photo.vo.StorageStatisticsVO;
import com.photo.vo.UserManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService extends ServiceImpl<UserMapper, User> {

    private final UserMapper userMapper;
    private final PhotoMapper photoMapper;

    public PageResult<UserManageVO> getUserList(UserQueryDTO query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, query.getKeyword())
                    .or().like(User::getEmail, query.getKeyword())
                    .or().like(User::getNickname, query.getKeyword()));
        }

        if (query.getRole() != null && !query.getRole().isEmpty()) {
            wrapper.eq(User::getRole, query.getRole());
        }

        if (query.getStatus() != null) {
            wrapper.eq(User::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        List<UserManageVO> userVOList = userPage.getRecords().stream()
                .map(this::convertToManageVO)
                .collect(Collectors.toList());

        return new PageResult<>(userVOList, userPage.getTotal(), userPage.getSize(), userPage.getCurrent());
    }

    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("不能修改管理员状态");
        }

        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("不能删除管理员账号");
        }

        LambdaQueryWrapper<Photo> photoWrapper = new LambdaQueryWrapper<>();
        photoWrapper.eq(Photo::getUserId, userId);
        photoMapper.delete(photoWrapper);

        userMapper.deleteById(userId);
    }

    public StorageStatisticsVO getStorageStatistics() {
        StorageStatisticsVO vo = new StorageStatisticsVO();

        LambdaQueryWrapper<Photo> photoWrapper = new LambdaQueryWrapper<>();
        photoWrapper.eq(Photo::getIsDeleted, 0);
        Long photoCount = photoMapper.selectCount(photoWrapper);
        vo.setPhotoCount(photoCount.intValue());

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        Long userCount = userMapper.selectCount(userWrapper);
        vo.setUserCount(userCount.intValue());

        vo.setTotalStorage(50L * 1024 * 1024 * 1024);
        vo.setUsedStorage(0L);
        vo.setAvailableStorage(vo.getTotalStorage());

        return vo;
    }

    private UserManageVO convertToManageVO(User user) {
        UserManageVO vo = new UserManageVO();
        BeanUtils.copyProperties(user, vo);

        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Photo::getUserId, user.getId());
        wrapper.eq(Photo::getIsDeleted, 0);
        Long photoCount = photoMapper.selectCount(wrapper);
        vo.setPhotoCount(photoCount);

        return vo;
    }
}
