package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.dto.AlbumDTO;
import com.photo.entity.Album;
import com.photo.entity.Photo;
import com.photo.mapper.AlbumMapper;
import com.photo.mapper.PhotoMapper;
import com.photo.vo.AlbumVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService extends ServiceImpl<AlbumMapper, Album> {

    private final AlbumMapper albumMapper;
    private final PhotoMapper photoMapper;

    public List<AlbumVO> getAlbumsByUserId(Long userId) {
        LambdaQueryWrapper<Album> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Album::getUserId, userId);
        wrapper.orderByDesc(Album::getCreatedAt);

        List<Album> albums = albumMapper.selectList(wrapper);
        return albums.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public AlbumVO getAlbumById(Long userId, Long albumId) {
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(userId)) {
            throw new IllegalArgumentException("相册不存在");
        }
        return convertToVO(album);
    }

    @Transactional
    public AlbumVO createAlbum(Long userId, AlbumDTO dto) {
        Album album = new Album();
        album.setUserId(userId);
        album.setName(dto.getName());
        album.setDescription(dto.getDescription());
        album.setCoverPhotoId(dto.getCoverPhotoId());
        album.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 0);
        album.setPhotoCount(0);
        album.setSortOrder(0);

        albumMapper.insert(album);
        return convertToVO(album);
    }

    @Transactional
    public AlbumVO updateAlbum(Long userId, Long albumId, AlbumDTO dto) {
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(userId)) {
            throw new IllegalArgumentException("相册不存在");
        }

        album.setName(dto.getName());
        album.setDescription(dto.getDescription());
        if (dto.getCoverPhotoId() != null) {
            album.setCoverPhotoId(dto.getCoverPhotoId());
        }
        if (dto.getIsPublic() != null) {
            album.setIsPublic(dto.getIsPublic());
        }

        albumMapper.updateById(album);
        return convertToVO(album);
    }

    @Transactional
    public void deleteAlbum(Long userId, Long albumId) {
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(userId)) {
            throw new IllegalArgumentException("相册不存在");
        }

        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Photo::getAlbumId, albumId);
        List<Photo> photos = photoMapper.selectList(wrapper);
        for (Photo photo : photos) {
            photo.setAlbumId(null);
            photoMapper.updateById(photo);
        }

        albumMapper.deleteById(albumId);
    }

    @Transactional
    public void addPhotosToAlbum(Long userId, Long albumId, List<Long> photoIds) {
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(userId)) {
            throw new IllegalArgumentException("相册不存在");
        }

        for (Long photoId : photoIds) {
            Photo photo = photoMapper.selectById(photoId);
            if (photo != null && photo.getUserId().equals(userId)) {
                photo.setAlbumId(albumId);
                photoMapper.updateById(photo);
            }
        }

        updatePhotoCount(albumId);
    }

    @Transactional
    public void removePhotosFromAlbum(Long userId, Long albumId, List<Long> photoIds) {
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(userId)) {
            throw new IllegalArgumentException("相册不存在");
        }

        for (Long photoId : photoIds) {
            Photo photo = photoMapper.selectById(photoId);
            if (photo != null && photo.getAlbumId() != null && photo.getAlbumId().equals(albumId)) {
                photo.setAlbumId(null);
                photoMapper.updateById(photo);
            }
        }

        updatePhotoCount(albumId);
    }

    private void updatePhotoCount(Long albumId) {
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Photo::getAlbumId, albumId);
        wrapper.eq(Photo::getIsDeleted, 0);
        Long count = photoMapper.selectCount(wrapper);

        Album album = albumMapper.selectById(albumId);
        if (album != null) {
            album.setPhotoCount(count.intValue());
            albumMapper.updateById(album);
        }
    }

    private AlbumVO convertToVO(Album album) {
        AlbumVO vo = new AlbumVO();
        BeanUtils.copyProperties(album, vo);

        if (album.getCoverPhotoId() != null) {
            Photo photo = photoMapper.selectById(album.getCoverPhotoId());
            if (photo != null) {
                vo.setCoverPhotoUrl(photo.getThumbnailPath());
            }
        }

        return vo;
    }

    public long countByUserId(Long userId) {
        LambdaQueryWrapper<Album> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Album::getUserId, userId);
        return albumMapper.selectCount(wrapper);
    }
}
