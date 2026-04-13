package com.photo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.Result;
import com.photo.dto.BackupCreateDTO;
import com.photo.entity.Backup;
import com.photo.entity.OperationLog;
import com.photo.service.BackupService;
import com.photo.service.OperationLogService;
import com.photo.vo.BackupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;
    private final OperationLogService operationLogService;

    @PostMapping
    public Result<BackupVO> createBackup(@RequestAttribute Long userId, @RequestBody BackupCreateDTO dto) {
        Backup backup = backupService.createBackup(userId, dto);
        operationLogService.log(userId, "CREATE_BACKUP", "backup", "创建备份: " + backup.getName(), 
            null, null, "POST", "/v1/backup", null, 200, 0L);
        return Result.success("备份创建成功", convertToVO(backup));
    }

    @GetMapping
    public Result<List<BackupVO>> getBackupList(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<Backup> page = backupService.getBackupList(userId, pageNum, pageSize);
        List<BackupVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @GetMapping("/{id}")
    public Result<BackupVO> getBackup(@RequestAttribute Long userId, @PathVariable Long id) {
        Backup backup = backupService.getBackupById(userId, id);
        return Result.success(convertToVO(backup));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBackup(@RequestAttribute Long userId, @PathVariable Long id) {
        backupService.deleteBackup(userId, id);
        operationLogService.log(userId, "DELETE_BACKUP", "backup", "删除备份: " + id, 
            null, null, "DELETE", "/v1/backup/" + id, null, 200, 0L);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadBackup(@RequestAttribute Long userId, @PathVariable Long id) {
        File file = backupService.getBackupFile(userId, id);
        Resource resource = new FileSystemResource(file);
        
        operationLogService.log(userId, "DOWNLOAD_BACKUP", "backup", "下载备份: " + id, 
            null, null, "GET", "/v1/backup/" + id + "/download", null, 200, 0L);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    private BackupVO convertToVO(Backup backup) {
        BackupVO vo = new BackupVO();
        BeanUtils.copyProperties(backup, vo);
        return vo;
    }
}
