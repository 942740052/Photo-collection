package com.photo.controller;

import com.photo.common.PageResult;
import com.photo.common.Result;
import com.photo.dto.PhotoQueryDTO;
import com.photo.dto.PhotoUpdateDTO;
import com.photo.dto.PhotoBatchUpdateDTO;
import com.photo.entity.Photo;
import com.photo.service.FileService;
import com.photo.service.PhotoService;
import com.photo.vo.PhotoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;
    private final FileService fileService;

    @PostMapping
    public Result<PhotoVO> uploadPhoto(
            @RequestAttribute Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "albumId", required = false) Long albumId) {
        Photo photo = fileService.uploadPhoto(userId, file, title, description, albumId);
        return Result.success("上传成功", photoService.getPhotoById(userId, photo.getId()));
    }

    @PostMapping("/batch")
    public Result<List<PhotoVO>> uploadPhotos(
            @RequestAttribute Long userId,
            @RequestParam("files") MultipartFile[] files) {
        List<Photo> photos = fileService.uploadPhotos(userId, files);
        List<PhotoVO> photoVOList = photos.stream()
                .map(photo -> photoService.getPhotoById(userId, photo.getId()))
                .collect(Collectors.toList());
        return Result.success("批量上传成功", photoVOList);
    }

    @GetMapping
    public Result<PageResult<PhotoVO>> getPhotoList(
            @RequestAttribute Long userId,
            PhotoQueryDTO query) {
        PageResult<PhotoVO> result = photoService.getPhotoList(userId, query);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<PhotoVO> getPhotoById(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        PhotoVO photo = photoService.getPhotoById(userId, id);
        return Result.success(photo);
    }

    @PutMapping("/{id}")
    public Result<PhotoVO> updatePhoto(
            @RequestAttribute Long userId,
            @PathVariable Long id,
            @RequestBody PhotoUpdateDTO dto) {
        dto.setId(id);
        PhotoVO photo = photoService.updatePhoto(userId, dto);
        return Result.success("更新成功", photo);
    }

    @PutMapping("/batch")
    public Result<Void> batchUpdate(
            @RequestAttribute Long userId,
            @RequestBody PhotoBatchUpdateDTO dto) {
        photoService.batchUpdate(userId, dto);
        return Result.success("批量更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePhoto(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        photoService.deletePhoto(userId, id);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/{id}/permanent")
    public Result<Void> permanentDeletePhoto(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        photoService.permanentDeletePhoto(userId, id);
        return Result.success("永久删除成功", null);
    }

    @PostMapping("/{id}/restore")
    public Result<Void> restorePhoto(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        photoService.restorePhoto(userId, id);
        return Result.success("恢复成功", null);
    }

    @DeleteMapping("/trash")
    public Result<Void> emptyTrash(@RequestAttribute Long userId) {
        photoService.emptyTrash(userId);
        return Result.success("清空回收站成功", null);
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> addFavorite(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        photoService.toggleFavorite(userId, id);
        return Result.success("收藏成功", null);
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> removeFavorite(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        photoService.toggleFavorite(userId, id);
        return Result.success("取消收藏成功", null);
    }

    @PostMapping("/batch-operation")
    public Result<Void> batchOperation(
            @RequestAttribute Long userId,
            @RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<?> rawIds = (List<?>) params.get("ids");
        List<Long> ids = rawIds.stream()
                .map(id -> {
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    }
                    return Long.parseLong(id.toString());
                })
                .collect(java.util.stream.Collectors.toList());
        String operation = (String) params.get("operation");
        photoService.batchOperation(userId, ids, operation);
        return Result.success("批量操作成功", null);
    }

    @PostMapping("/batch-move")
    public Result<Void> batchMoveToAlbum(
            @RequestAttribute Long userId,
            @RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<?> rawIds = (List<?>) params.get("ids");
        List<Long> ids = rawIds.stream()
                .map(id -> {
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    }
                    return Long.parseLong(id.toString());
                })
                .collect(java.util.stream.Collectors.toList());
        Long albumId = Long.valueOf(params.get("albumId").toString());
        photoService.batchMoveToAlbum(userId, ids, albumId);
        return Result.success("移动成功", null);
    }

    @GetMapping("/cameras")
    public Result<List<String>> getCameraModels(@RequestAttribute Long userId) {
        List<String> cameras = photoService.getCameraModels(userId);
        return Result.success(cameras);
    }
}
