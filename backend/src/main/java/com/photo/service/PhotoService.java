package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.common.PageResult;
import com.photo.dto.PhotoQueryDTO;
import com.photo.dto.PhotoUpdateDTO;
import com.photo.dto.PhotoBatchUpdateDTO;
import com.photo.entity.Photo;
import com.photo.entity.PhotoTag;
import com.photo.mapper.PhotoMapper;
import com.photo.mapper.PhotoTagMapper;
import com.photo.vo.PhotoVO;
import com.photo.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoService extends ServiceImpl<PhotoMapper, Photo> {

    private final PhotoMapper photoMapper;
    private final PhotoTagMapper photoTagMapper;
    private final TagService tagService;

    public PageResult<PhotoVO> getPhotoList(Long userId, PhotoQueryDTO query) {
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Photo::getUserId, userId);
        wrapper.eq(Photo::getIsDeleted, query.getIsDeleted() != null ? query.getIsDeleted() : 0);

        if (query.getAlbumId() != null) {
            wrapper.eq(Photo::getAlbumId, query.getAlbumId());
        }

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Photo::getTitle, query.getKeyword())
                    .or().like(Photo::getDescription, query.getKeyword()));
        }

        if (query.getStartTime() != null) {
            wrapper.ge(Photo::getCreatedAt, query.getStartTime());
        }

        if (query.getEndTime() != null) {
            wrapper.le(Photo::getCreatedAt, query.getEndTime());
        }

        if (query.getShootStartTime() != null) {
            wrapper.ge(Photo::getShootTime, query.getShootStartTime());
        }

        if (query.getShootEndTime() != null) {
            wrapper.le(Photo::getShootTime, query.getShootEndTime());
        }

        if (query.getIsFavorite() != null) {
            wrapper.eq(Photo::getIsFavorite, query.getIsFavorite());
        }

        if (query.getCameraModel() != null && !query.getCameraModel().isEmpty()) {
            wrapper.eq(Photo::getCameraModel, query.getCameraModel());
        }

        if (query.getLocation() != null && !query.getLocation().isEmpty()) {
            if ("exact".equals(query.getLocationFilter())) {
                wrapper.eq(Photo::getLocation, query.getLocation());
            } else {
                wrapper.like(Photo::getLocation, query.getLocation());
            }
        }

        if (query.getMinFileSize() != null) {
            wrapper.ge(Photo::getFileSize, query.getMinFileSize());
        }

        if (query.getMaxFileSize() != null) {
            wrapper.le(Photo::getFileSize, query.getMaxFileSize());
        }

        if (query.getWidthMin() != null) {
            wrapper.ge(Photo::getWidth, query.getWidthMin());
        }

        if (query.getWidthMax() != null) {
            wrapper.le(Photo::getWidth, query.getWidthMax());
        }

        if (query.getHeightMin() != null) {
            wrapper.ge(Photo::getHeight, query.getHeightMin());
        }

        if (query.getHeightMax() != null) {
            wrapper.le(Photo::getHeight, query.getHeightMax());
        }

        if (query.getGpsLatitude() != null && query.getGpsLongitude() != null && query.getGpsRadius() != null) {
            double lat = query.getGpsLatitude();
            double lng = query.getGpsLongitude();
            double radius = query.getGpsRadius();
            wrapper.ge(Photo::getGpsLatitude, lat - radius);
            wrapper.le(Photo::getGpsLatitude, lat + radius);
            wrapper.ge(Photo::getGpsLongitude, lng - radius);
            wrapper.le(Photo::getGpsLongitude, lng + radius);
        }

        List<Long> tagIdList = query.getTagIdList();
        if (tagIdList != null && !tagIdList.isEmpty()) {
            wrapper.inSql(Photo::getId, 
                "SELECT DISTINCT photo_id FROM t_photo_tag WHERE tag_id IN (" + 
                tagIdList.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")");
        }

        String sortBy = query.getSortBy();
        boolean isAsc = "ASC".equalsIgnoreCase(query.getSortOrder());
        
        switch (sortBy) {
            case "shoot_time":
                wrapper.orderBy(true, !isAsc, Photo::getShootTime);
                break;
            case "file_size":
                wrapper.orderBy(true, !isAsc, Photo::getFileSize);
                break;
            case "title":
                wrapper.orderBy(true, isAsc, Photo::getTitle);
                break;
            case "view_count":
                wrapper.orderBy(true, !isAsc, Photo::getViewCount);
                break;
            default:
                wrapper.orderBy(true, !isAsc, Photo::getCreatedAt);
        }

        Page<Photo> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Photo> photoPage = photoMapper.selectPage(page, wrapper);

        List<PhotoVO> photoVOList = photoPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(photoVOList, photoPage.getTotal(), photoPage.getSize(), photoPage.getCurrent());
    }

    public PhotoVO getPhotoById(Long userId, Long photoId) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("照片不存在");
        }
        return convertToVO(photo);
    }

    @Transactional
    public PhotoVO updatePhoto(Long userId, PhotoUpdateDTO dto) {
        Photo photo = photoMapper.selectById(dto.getId());
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("照片不存在");
        }

        boolean hasOtherUpdates = false;
        
        if (dto.getTitle() != null) {
            photo.setTitle(dto.getTitle());
            hasOtherUpdates = true;
        }
        if (dto.getDescription() != null) {
            photo.setDescription(dto.getDescription());
            hasOtherUpdates = true;
        }
        
        if (dto.getTagIds() != null) {
            updatePhotoTags(photo.getId(), dto.getTagIds());
            hasOtherUpdates = true;
        }
        
        if (dto.getAlbumId() != null) {
            photo.setAlbumId(dto.getAlbumId());
            hasOtherUpdates = true;
        } else if (dto.getAlbumId() == null && !hasOtherUpdates) {
            photoMapper.removeAlbum(dto.getId());
            return convertToVO(photoMapper.selectById(dto.getId()));
        }

        if (hasOtherUpdates) {
            photoMapper.updateById(photo);
        }

        return convertToVO(photo);
    }

    @Transactional
    public void batchUpdate(Long userId, PhotoBatchUpdateDTO dto) {
        for (Long photoId : dto.getPhotoIds()) {
            Photo photo = photoMapper.selectById(photoId);
            if (photo != null && photo.getUserId().equals(userId)) {
                boolean needUpdate = false;
                if (dto.getTitle() != null) {
                    photo.setTitle(dto.getTitle());
                    needUpdate = true;
                }
                if (dto.getDescription() != null) {
                    photo.setDescription(dto.getDescription());
                    needUpdate = true;
                }
                
                if (dto.getAlbumId() != null) {
                    photo.setAlbumId(dto.getAlbumId());
                    needUpdate = true;
                }
                
                if (needUpdate) {
                    photoMapper.updateById(photo);
                } else if (dto.getAlbumId() == null) {
                    photoMapper.removeAlbum(photoId);
                }

                if (dto.getTagIds() != null) {
                    updatePhotoTags(photoId, dto.getTagIds());
                }
            }
        }
    }

    @Transactional
    public void deletePhoto(Long userId, Long photoId) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("照片不存在");
        }
        photo.setIsDeleted(1);
        photoMapper.updateById(photo);
    }

    @Transactional
    public void permanentDeletePhoto(Long userId, Long photoId) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("照片不存在");
        }

        LambdaQueryWrapper<PhotoTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoTag::getPhotoId, photoId);
        photoTagMapper.delete(wrapper);

        photoMapper.deleteById(photoId);
    }

    @Transactional
    public void toggleFavorite(Long userId, Long photoId) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("照片不存在");
        }

        photo.setIsFavorite(photo.getIsFavorite() == 1 ? 0 : 1);
        photoMapper.updateById(photo);
    }

    @Transactional
    public void batchOperation(Long userId, List<Long> photoIds, String operation) {
        for (Long photoId : photoIds) {
            Photo photo = photoMapper.selectById(photoId);
            if (photo != null && photo.getUserId().equals(userId)) {
                switch (operation) {
                    case "delete":
                        photo.setIsDeleted(1);
                        photoMapper.updateById(photo);
                        break;
                    case "restore":
                        photo.setIsDeleted(0);
                        photoMapper.updateById(photo);
                        break;
                    case "favorite":
                        photo.setIsFavorite(1);
                        photoMapper.updateById(photo);
                        break;
                    case "unfavorite":
                        photo.setIsFavorite(0);
                        photoMapper.updateById(photo);
                        break;
                    case "permanent_delete":
                        permanentDeletePhoto(userId, photoId);
                        break;
                }
            }
        }
    }

    @Transactional
    public void batchMoveToAlbum(Long userId, List<Long> photoIds, Long albumId) {
        for (Long photoId : photoIds) {
            Photo photo = photoMapper.selectById(photoId);
            if (photo != null && photo.getUserId().equals(userId)) {
                photo.setAlbumId(albumId);
                photoMapper.updateById(photo);
            }
        }
    }

    private void updatePhotoTags(Long photoId, List<Long> tagIds) {
        LambdaQueryWrapper<PhotoTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoTag::getPhotoId, photoId);
        photoTagMapper.delete(wrapper);

        for (Long tagId : tagIds) {
            PhotoTag photoTag = new PhotoTag();
            photoTag.setPhotoId(photoId);
            photoTag.setTagId(tagId);
            photoTagMapper.insert(photoTag);
        }
    }

    public PhotoVO convertToVO(Photo photo) {
        PhotoVO vo = new PhotoVO();
        BeanUtils.copyProperties(photo, vo);

        List<TagVO> tags = tagService.getTagsByPhotoId(photo.getId());
        vo.setTags(tags);

        return vo;
    }

    public long countByUserId(Long userId) {
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Photo::getUserId, userId);
        wrapper.eq(Photo::getIsDeleted, 0);
        return photoMapper.selectCount(wrapper);
    }

    public List<String> getCameraModels(Long userId) {
        return photoMapper.selectDistinctCameraModels(userId);
    }

    @Transactional
    public void protectPhoto(Long userId, Long photoId, String encodedPassword) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("照片不存在");
        }
        photo.setIsPrivate(1);
        photo.setPrivatePassword(encodedPassword);
        photoMapper.updateById(photo);
    }

    @Transactional
    public void unprotectPhoto(Long userId, Long photoId) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("照片不存在");
        }
        photo.setIsPrivate(0);
        photo.setPrivatePassword(null);
        photoMapper.updateById(photo);
    }

    public boolean verifyPhotoPassword(Long photoId, String password) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || photo.getIsPrivate() != 1) {
            return true;
        }
        if (photo.getPrivatePassword() == null) {
            return true;
        }
        return org.springframework.security.crypto.bcrypt.BCrypt.checkpw(password, photo.getPrivatePassword());
    }

    @Transactional
    public void restorePhoto(Long userId, Long photoId) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("照片不存在");
        }
        photo.setIsDeleted(0);
        photoMapper.updateById(photo);
    }

    @Transactional
    public void emptyTrash(Long userId) {
        List<Photo> deletedPhotos = photoMapper.selectList(
            new LambdaQueryWrapper<Photo>()
                .eq(Photo::getUserId, userId)
                .eq(Photo::getIsDeleted, 1)
        );
        for (Photo photo : deletedPhotos) {
            photoTagMapper.delete(new LambdaQueryWrapper<PhotoTag>()
                .eq(PhotoTag::getPhotoId, photo.getId()));
            photoMapper.deleteById(photo.getId());
        }
    }
}
