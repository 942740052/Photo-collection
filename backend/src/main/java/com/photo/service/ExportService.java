package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.photo.entity.Album;
import com.photo.entity.Photo;
import com.photo.mapper.AlbumMapper;
import com.photo.mapper.PhotoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
public class ExportService {

    private final PhotoMapper photoMapper;
    private final AlbumMapper albumMapper;

    @Value("${export.path:./exports}")
    private String exportPath;

    public File exportByAlbum(Long userId, Long albumId) {
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(userId)) {
            throw new IllegalArgumentException("相册不存在");
        }

        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Photo::getUserId, userId);
        wrapper.eq(Photo::getAlbumId, albumId);
        wrapper.eq(Photo::getIsDeleted, 0);
        wrapper.orderByDesc(Photo::getCreatedAt);
        List<Photo> photos = photoMapper.selectList(wrapper);

        return createExportZip(photos, "album_" + album.getName());
    }

    public File exportByDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Photo::getUserId, userId);
        wrapper.eq(Photo::getIsDeleted, 0);
        
        if (startTime != null) {
            wrapper.ge(Photo::getShootTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Photo::getShootTime, endTime);
        }
        
        wrapper.orderByDesc(Photo::getShootTime);
        List<Photo> photos = photoMapper.selectList(wrapper);

        String dateRange = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startTime != null && endTime != null) {
            dateRange = startTime.format(formatter) + "_" + endTime.format(formatter);
        } else {
            dateRange = LocalDateTime.now().format(formatter);
        }

        return createExportZip(photos, "photos_" + dateRange);
    }

    public File exportFavorites(Long userId) {
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Photo::getUserId, userId);
        wrapper.eq(Photo::getIsDeleted, 0);
        wrapper.eq(Photo::getIsFavorite, 1);
        wrapper.orderByDesc(Photo::getCreatedAt);
        List<Photo> photos = photoMapper.selectList(wrapper);

        return createExportZip(photos, "favorites");
    }

    public File exportAll(Long userId) {
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Photo::getUserId, userId);
        wrapper.eq(Photo::getIsDeleted, 0);
        wrapper.orderByDesc(Photo::getCreatedAt);
        List<Photo> photos = photoMapper.selectList(wrapper);

        return createExportZip(photos, "all_photos");
    }

    private File createExportZip(List<Photo> photos, String exportName) {
        try {
            Path exportDir = Paths.get(exportPath);
            if (!Files.exists(exportDir)) {
                Files.createDirectories(exportDir);
            }

            String fileName = exportName + "_" + System.currentTimeMillis() + ".zip";
            File zipFile = exportDir.resolve(fileName).toFile();

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                StringBuilder manifest = new StringBuilder();
                manifest.append("Export created at: ").append(LocalDateTime.now()).append("\n");
                manifest.append("Total photos: ").append(photos.size()).append("\n\n");

                for (int i = 0; i < photos.size(); i++) {
                    Photo photo = photos.get(i);
                    String entryName = String.format("%04d_%s", i + 1, 
                        photo.getTitle() != null ? sanitizeFileName(photo.getTitle()) : photo.getId());
                    
                    if (photo.getFilePath() != null) {
                        File photoFile = new File(photo.getFilePath().replace("/api", "."));
                        if (photoFile.exists()) {
                            String extension = getFileExtension(photoFile.getName());
                            addFileToZip(zos, entryName + extension, photoFile);
                            
                            manifest.append(String.format("%s - %s\n", entryName + extension, 
                                photo.getTitle() != null ? photo.getTitle() : "Untitled"));
                        }
                    }
                }

                addEntryToZip(zos, "manifest.txt", manifest.toString());
            }

            return zipFile;
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").substring(0, Math.min(name.length(), 50));
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
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
}
