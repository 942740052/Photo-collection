package com.photo.controller;

import com.photo.common.PageResult;
import com.photo.common.Result;
import com.photo.dto.UserQueryDTO;
import com.photo.service.AdminService;
import com.photo.service.SystemConfigService;
import com.photo.vo.StorageStatisticsVO;
import com.photo.vo.UserManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final SystemConfigService systemConfigService;

    @GetMapping("/users")
    public Result<PageResult<UserManageVO>> getUserList(UserQueryDTO query) {
        PageResult<UserManageVO> result = adminService.getUserList(query);
        return Result.success(result);
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        adminService.updateUserStatus(id, status);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/config")
    public Result<Map<String, String>> getSystemConfig() {
        Map<String, String> configs = systemConfigService.getAllConfigs();
        return Result.success(configs);
    }

    @PutMapping("/config")
    public Result<Void> updateSystemConfig(@RequestBody Map<String, String> configs) {
        systemConfigService.setConfigs(configs);
        return Result.success("更新成功", null);
    }

    @GetMapping("/storage")
    public Result<StorageStatisticsVO> getStorageStatistics() {
        StorageStatisticsVO statistics = adminService.getStorageStatistics();
        return Result.success(statistics);
    }
}
