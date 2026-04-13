package com.photo.controller;

import com.photo.common.Result;
import com.photo.service.TagService;
import com.photo.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public Result<TagVO> createTag(
            @RequestAttribute Long userId,
            @RequestBody Map<String, String> params) {
        String name = params.get("name");
        String color = params.get("color");
        TagVO tag = tagService.createTag(userId, name, color);
        return Result.success("创建成功", tag);
    }

    @GetMapping
    public Result<List<TagVO>> getTagList(@RequestAttribute Long userId) {
        List<TagVO> tags = tagService.getTagsByUserId(userId);
        return Result.success(tags);
    }

    @PutMapping("/{id}")
    public Result<TagVO> updateTag(
            @RequestAttribute Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        String name = params.get("name");
        String color = params.get("color");
        TagVO tag = tagService.updateTag(userId, id, name, color);
        return Result.success("更新成功", tag);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        tagService.deleteTag(userId, id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/photos/{photoId}/tags")
    public Result<Void> addTagToPhoto(
            @RequestAttribute Long userId,
            @PathVariable Long photoId,
            @RequestBody Map<String, Long> params) {
        Long tagId = params.get("tagId");
        tagService.addTagToPhoto(userId, photoId, tagId);
        return Result.success("添加成功", null);
    }

    @DeleteMapping("/photos/{photoId}/tags/{tagId}")
    public Result<Void> removeTagFromPhoto(
            @RequestAttribute Long userId,
            @PathVariable Long photoId,
            @PathVariable Long tagId) {
        tagService.removeTagFromPhoto(userId, photoId, tagId);
        return Result.success("移除成功", null);
    }
}
