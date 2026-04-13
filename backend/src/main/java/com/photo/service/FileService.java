package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.entity.PhotoMetadata;
import com.photo.entity.Photo;
import com.photo.mapper.PhotoMapper;
import com.photo.mapper.PhotoMetadataMapper;
import com.photo.vo.TagVO;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.max-size}")
    private Long maxSize;

    @Value("${file.allowed-types}")
    private String allowedTypes;

    private final PhotoMapper photoMapper;
    private final PhotoMetadataMapper photoMetadataMapper;
    private final TagService tagService;
    private final AlbumService albumService;

    public Photo uploadPhoto(Long userId, MultipartFile file, String title, String description, Long albumId) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = generateFileName(extension);

        String datePath = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fullPath = uploadPath + datePath + "/";
        String thumbnailPath = uploadPath + "thumbnails/" + datePath + "/";

        try {
            Path directory = Paths.get(fullPath);
            Files.createDirectories(directory);

            Path thumbnailDirectory = Paths.get(thumbnailPath);
            Files.createDirectories(thumbnailDirectory);

            Path filePath = directory.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String thumbnailFilename = "thumb_" + newFilename;
            Path thumbnailFilePath = thumbnailDirectory.resolve(thumbnailFilename);
            createThumbnail(filePath.toFile(), thumbnailFilePath.toFile());

            Photo photo = new Photo();
            photo.setUserId(userId);
            photo.setAlbumId(albumId);
            photo.setTitle(title != null ? title : originalFilename);
            photo.setDescription(description);
            photo.setFilePath("/uploads/" + datePath + "/" + newFilename);
            photo.setThumbnailPath("/uploads/thumbnails/" + datePath + "/" + thumbnailFilename);
            photo.setFileSize(file.getSize());
            photo.setFileType(extension.toLowerCase());
            photo.setIsFavorite(0);
            photo.setIsDeleted(0);
            photo.setViewCount(0);

            extractImageMetadata(filePath.toFile(), photo);

            photoMapper.insert(photo);

            if (albumId != null) {
                albumService.addPhotosToAlbum(userId, albumId, Arrays.asList(photo.getId()));
            }

            return photo;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        if (!allowedTypeList.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private void createThumbnail(File sourceFile, File targetFile) throws IOException {
        Thumbnails.of(sourceFile)
                .size(300, 300)
                .keepAspectRatio(true)
                .toFile(targetFile);
    }

    private void extractImageMetadata(File imageFile, Photo photo) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

            Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIFD0Directory != null) {
                if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_DATETIME)) {
                    Date date = exifIFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
                    if (date != null) {
                        photo.setShootTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                    }
                }

                if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_MODEL)) {
                    photo.setCameraModel(exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL));
                }
            }

            Directory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            PhotoMetadata photoMetadata = new PhotoMetadata();
            photoMetadata.setPhotoId(photo.getId());

            if (exifSubIFDDirectory != null) {
                if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)) {
                    photoMetadata.setIso(exifSubIFDDirectory.getInteger(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                }

                if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FNUMBER)) {
                    photoMetadata.setAperture(exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_FNUMBER));
                }

                if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_EXPOSURE_TIME)) {
                    photoMetadata.setShutterSpeed(exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                }

                if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH)) {
                    photoMetadata.setFocalLength(exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
                }

                if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FLASH)) {
                    photoMetadata.setFlash(exifSubIFDDirectory.getInteger(ExifSubIFDDirectory.TAG_FLASH));
                }
            }

            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null) {
                if (gpsDirectory.containsTag(GpsDirectory.TAG_LATITUDE) && gpsDirectory.containsTag(GpsDirectory.TAG_LONGITUDE)) {
                    com.drew.metadata.exif.GpsDescriptor gpsDescriptor = new com.drew.metadata.exif.GpsDescriptor(gpsDirectory);
                    Double latitude = gpsDirectory.getDouble(GpsDirectory.TAG_LATITUDE);
                    Double longitude = gpsDirectory.getDouble(GpsDirectory.TAG_LONGITUDE);
                    if (latitude != null && longitude != null) {
                        photo.setGpsLatitude(BigDecimal.valueOf(latitude));
                        photo.setGpsLongitude(BigDecimal.valueOf(longitude));
                    }
                }
            }

            photoMetadataMapper.insert(photoMetadata);

        } catch (Exception e) {
            log.warn("提取EXIF信息失败: {}", e.getMessage());
        }
    }

    public List<Photo> uploadPhotos(Long userId, MultipartFile[] files) {
        List<Photo> photos = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Photo photo = uploadPhoto(userId, file, null, null, null);
                photos.add(photo);
            } catch (Exception e) {
                log.error("上传文件 {} 失败: {}", file.getOriginalFilename(), e.getMessage());
            }
        }
        return photos;
    }
}
