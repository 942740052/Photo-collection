package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.dto.BackupCreateDTO;
import com.photo.entity.*;
import com.photo.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class BackupService extends ServiceImpl<BackupMapper, Backup> {

    private final BackupMapper backupMapper;
    private final PhotoMapper photoMapper;
    private final AlbumMapper albumMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;

    @Value("${backup.path:./backups}")
    private String backupPath;

    @Transactional
    public Backup createBackup(Long userId, BackupCreateDTO dto) {
        User user = userMapper.selectById(userId);
        Backup backup = new Backup();
        backup.setUserId(userId);
        backup.setName(dto.getName() != null ? dto.getName() : "Backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        backup.setType(dto.getType());
        backup.setStatus("processing");
        backup.setStartedAt(LocalDateTime.now());
        backup.setCreatedAt(LocalDateTime.now());
        backupMapper.insert(backup);

        try {
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            String fileName = "backup_" + userId + "_" + backup.getId() + "_" + System.currentTimeMillis() + ".zip";
            Path filePath = backupDir.resolve(fileName);

            long photoCount = 0;
            long albumCount = 0;
            long tagCount = 0;

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filePath.toFile()))) {
                if ("database".equals(dto.getType()) || "full".equals(dto.getType())) {
                    String jsonData = exportDatabaseToJson(userId);
                    addEntryToZip(zos, "database.json", jsonData);
                    
                    LambdaQueryWrapper<Photo> photoWrapper = new LambdaQueryWrapper<>();
                    photoWrapper.eq(Photo::getUserId, userId);
                    photoWrapper.eq(Photo::getIsDeleted, 0);
                    photoCount = photoMapper.selectCount(photoWrapper);

                    LambdaQueryWrapper<Album> albumWrapper = new LambdaQueryWrapper<>();
                    albumWrapper.eq(Album::getUserId, userId);
                    albumCount = albumMapper.selectCount(albumWrapper);

                    LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
                    tagWrapper.eq(Tag::getUserId, userId);
                    tagCount = tagMapper.selectCount(tagWrapper);
                }

                if ("photos".equals(dto.getType()) || "full".equals(dto.getType())) {
                    LambdaQueryWrapper<Photo> photoWrapper = new LambdaQueryWrapper<>();
                    photoWrapper.eq(Photo::getUserId, userId);
                    photoWrapper.eq(Photo::getIsDeleted, 0);
                    List<Photo> photos = photoMapper.selectList(photoWrapper);

                    for (Photo photo : photos) {
                        if (photo.getFilePath() != null) {
                            File photoFile = new File(photo.getFilePath().replace("/api", "."));
                            if (photoFile.exists()) {
                                addFileToZip(zos, "photos/" + photo.getId() + "_" + photoFile.getName(), photoFile);
                            }
                        }
                    }
                }
            }

            backup.setFilePath(filePath.toString());
            backup.setFileSize(filePath.toFile().length());
            backup.setPhotoCount((int) photoCount);
            backup.setAlbumCount((int) albumCount);
            backup.setTagCount((int) tagCount);
            backup.setStatus("completed");
            backup.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            backup.setStatus("failed");
            backup.setErrorMessage(e.getMessage());
            backup.setCompletedAt(LocalDateTime.now());
        }

        backupMapper.updateById(backup);
        return backup;
    }

    private String exportDatabaseToJson(Long userId) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        json.append("\"user\":");
        User user = userMapper.selectById(userId);
        json.append(user != null ? String.format("{\"id\":%d,\"username\":\"%s\",\"email\":\"%s\"}", user.getId(), user.getUsername(), user.getEmail()) : "null");
        json.append(",");

        json.append("\"albums\":[");
        LambdaQueryWrapper<Album> albumWrapper = new LambdaQueryWrapper<>();
        albumWrapper.eq(Album::getUserId, userId);
        List<Album> albums = albumMapper.selectList(albumWrapper);
        for (int i = 0; i < albums.size(); i++) {
            Album album = albums.get(i);
            if (i > 0) json.append(",");
            json.append(String.format("{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\"}", 
                album.getId(), album.getName(), album.getDescription() != null ? album.getDescription() : ""));
        }
        json.append("],");

        json.append("\"tags\":[");
        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(Tag::getUserId, userId);
        List<Tag> tags = tagMapper.selectList(tagWrapper);
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (i > 0) json.append(",");
            json.append(String.format("{\"id\":%d,\"name\":\"%s\",\"color\":\"%s\"}", 
                tag.getId(), tag.getName(), tag.getColor() != null ? tag.getColor() : ""));
        }
        json.append("],");

        json.append("\"photos\":[");
        LambdaQueryWrapper<Photo> photoWrapper = new LambdaQueryWrapper<>();
        photoWrapper.eq(Photo::getUserId, userId);
        photoWrapper.eq(Photo::getIsDeleted, 0);
        List<Photo> photos = photoMapper.selectList(photoWrapper);
        for (int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);
            if (i > 0) json.append(",");
            json.append(String.format("{\"id\":%d,\"title\":\"%s\",\"description\":\"%s\",\"albumId\":%s,\"shootTime\":\"%s\",\"cameraModel\":\"%s\",\"location\":\"%s\"}", 
                photo.getId(), 
                photo.getTitle() != null ? photo.getTitle() : "",
                photo.getDescription() != null ? photo.getDescription() : "",
                photo.getAlbumId() != null ? photo.getAlbumId().toString() : "null",
                photo.getShootTime() != null ? photo.getShootTime().toString() : "",
                photo.getCameraModel() != null ? photo.getCameraModel() : "",
                photo.getLocation() != null ? photo.getLocation() : ""));
        }
        json.append("]");

        json.append("}");
        return json.toString();
    }

    private void addEntryToZip(ZipOutputStream zos, String entryName, String content) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        zos.write(content.getBytes("UTF-8"));
        zos.closeEntry();
    }

    private void addFileToZip(ZipOutputStream zos, String entryName, File file) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        }
        zos.closeEntry();
    }

    public Page<Backup> getBackupList(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Backup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Backup::getUserId, userId);
        wrapper.orderByDesc(Backup::getCreatedAt);
        Page<Backup> page = new Page<>(pageNum, pageSize);
        return backupMapper.selectPage(page, wrapper);
    }

    public Backup getBackupById(Long userId, Long backupId) {
        Backup backup = backupMapper.selectById(backupId);
        if (backup == null || !backup.getUserId().equals(userId)) {
            throw new IllegalArgumentException("备份不存在");
        }
        return backup;
    }

    @Transactional
    public void deleteBackup(Long userId, Long backupId) {
        Backup backup = getBackupById(userId, backupId);
        
        if (backup.getFilePath() != null) {
            File file = new File(backup.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
        
        backupMapper.deleteById(backupId);
    }

    public File getBackupFile(Long userId, Long backupId) {
        Backup backup = getBackupById(userId, backupId);
        if (!"completed".equals(backup.getStatus())) {
            throw new IllegalArgumentException("备份未完成");
        }
        File file = new File(backup.getFilePath());
        if (!file.exists()) {
            throw new IllegalArgumentException("备份文件不存在");
        }
        return file;
    }
}
