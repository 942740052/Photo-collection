package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.entity.Tag;
import com.photo.entity.PhotoTag;
import com.photo.mapper.TagMapper;
import com.photo.mapper.PhotoTagMapper;
import com.photo.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService extends ServiceImpl<TagMapper, Tag> {

    private final TagMapper tagMapper;
    private final PhotoTagMapper photoTagMapper;

    public List<TagVO> getTagsByUserId(Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId);
        wrapper.orderByDesc(Tag::getPhotoCount);

        List<Tag> tags = tagMapper.selectList(wrapper);
        return tags.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public List<TagVO> getTagsByPhotoId(Long photoId) {
        LambdaQueryWrapper<PhotoTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoTag::getPhotoId, photoId);
        List<PhotoTag> photoTags = photoTagMapper.selectList(wrapper);

        return photoTags.stream()
                .map(photoTag -> {
                    Tag tag = tagMapper.selectById(photoTag.getTagId());
                    return convertToVO(tag);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TagVO createTag(Long userId, String name, String color) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId);
        wrapper.eq(Tag::getName, name);
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("标签已存在");
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name);
        tag.setColor(color != null ? color : "#409EFF");
        tag.setPhotoCount(0);

        tagMapper.insert(tag);
        return convertToVO(tag);
    }

    @Transactional
    public TagVO updateTag(Long userId, Long tagId, String name, String color) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new IllegalArgumentException("标签不存在");
        }

        if (name != null && !name.equals(tag.getName())) {
            LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Tag::getUserId, userId);
            wrapper.eq(Tag::getName, name);
            wrapper.ne(Tag::getId, tagId);
            if (tagMapper.selectCount(wrapper) > 0) {
                throw new IllegalArgumentException("标签名称已存在");
            }
            tag.setName(name);
        }

        if (color != null) {
            tag.setColor(color);
        }

        tagMapper.updateById(tag);
        return convertToVO(tag);
    }

    @Transactional
    public void deleteTag(Long userId, Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new IllegalArgumentException("标签不存在");
        }

        LambdaQueryWrapper<PhotoTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoTag::getTagId, tagId);
        photoTagMapper.delete(wrapper);

        tagMapper.deleteById(tagId);
    }

    @Transactional
    public void addTagToPhoto(Long userId, Long photoId, Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new IllegalArgumentException("标签不存在");
        }

        LambdaQueryWrapper<PhotoTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoTag::getPhotoId, photoId);
        wrapper.eq(PhotoTag::getTagId, tagId);
        if (photoTagMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("照片已有此标签");
        }

        PhotoTag photoTag = new PhotoTag();
        photoTag.setPhotoId(photoId);
        photoTag.setTagId(tagId);
        photoTagMapper.insert(photoTag);

        tag.setPhotoCount(tag.getPhotoCount() + 1);
        tagMapper.updateById(tag);
    }

    @Transactional
    public void removeTagFromPhoto(Long userId, Long photoId, Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new IllegalArgumentException("标签不存在");
        }

        LambdaQueryWrapper<PhotoTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoTag::getPhotoId, photoId);
        wrapper.eq(PhotoTag::getTagId, tagId);
        photoTagMapper.delete(wrapper);

        if (tag.getPhotoCount() > 0) {
            tag.setPhotoCount(tag.getPhotoCount() - 1);
            tagMapper.updateById(tag);
        }
    }

    private TagVO convertToVO(Tag tag) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }
}
