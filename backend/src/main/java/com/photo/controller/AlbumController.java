package com.photo.controller;

import com.photo.common.Result;
import com.photo.dto.AlbumDTO;
import com.photo.service.AlbumService;
import com.photo.vo.AlbumVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping
    public Result<AlbumVO> createAlbum(
            @RequestAttribute Long userId,
            @RequestBody AlbumDTO dto) {
        AlbumVO album = albumService.createAlbum(userId, dto);
        return Result.success("创建成功", album);
    }

    @GetMapping
    public Result<List<AlbumVO>> getAlbumList(@RequestAttribute Long userId) {
        List<AlbumVO> albums = albumService.getAlbumsByUserId(userId);
        return Result.success(albums);
    }

    @GetMapping("/{id}")
    public Result<AlbumVO> getAlbumById(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        AlbumVO album = albumService.getAlbumById(userId, id);
        return Result.success(album);
    }

    @PutMapping("/{id}")
    public Result<AlbumVO> updateAlbum(
            @RequestAttribute Long userId,
            @PathVariable Long id,
            @RequestBody AlbumDTO dto) {
        AlbumVO album = albumService.updateAlbum(userId, id, dto);
        return Result.success("更新成功", album);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAlbum(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        albumService.deleteAlbum(userId, id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/photos")
    public Result<Void> addPhotosToAlbum(
            @RequestAttribute Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> params) {
        List<Long> photoIds = params.get("photoIds");
        albumService.addPhotosToAlbum(userId, id, photoIds);
        return Result.success("添加成功", null);
    }

    @DeleteMapping("/{id}/photos")
    public Result<Void> removePhotosFromAlbum(
            @RequestAttribute Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> params) {
        List<Long> photoIds = params.get("photoIds");
        albumService.removePhotosFromAlbum(userId, id, photoIds);
        return Result.success("移除成功", null);
    }
}
