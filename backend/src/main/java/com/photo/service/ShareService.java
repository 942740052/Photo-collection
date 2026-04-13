package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.dto.ShareCreateDTO;
import com.photo.entity.Photo;
import com.photo.entity.Share;
import com.photo.entity.ShareAccess;
import com.photo.entity.Album;
import com.photo.mapper.PhotoMapper;
import com.photo.mapper.ShareMapper;
import com.photo.mapper.ShareAccessMapper;
import com.photo.mapper.AlbumMapper;
import com.photo.util.ShareCodeUtil;
import com.photo.vo.PhotoVO;
import com.photo.vo.ShareContentVO;
import com.photo.vo.ShareVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareService extends ServiceImpl<ShareMapper, Share> {

    private final ShareMapper shareMapper;
    private final ShareAccessMapper shareAccessMapper;
    private final PhotoMapper photoMapper;
    private final AlbumMapper albumMapper;
    private final PhotoService photoService;

    @Transactional
    public ShareVO createShare(Long userId, ShareCreateDTO dto) {
        if (dto.getPhotoId() == null && dto.getAlbumId() == null) {
            throw new IllegalArgumentException("必须指定照片或相册");
        }

        Share share = new Share();
        share.setUserId(userId);
        share.setPhotoId(dto.getPhotoId());
        share.setAlbumId(dto.getAlbumId());
        share.setShareCode(ShareCodeUtil.generateShareCode());
        share.setPassword(dto.getPassword());
        share.setViewCount(0);
        share.setIsActive(1);

        if (dto.getExpireHours() != null && dto.getExpireHours() > 0) {
            share.setExpireAt(LocalDateTime.now().plusHours(dto.getExpireHours()));
        }

        shareMapper.insert(share);
        return convertToVO(share);
    }

    public ShareContentVO getShareContent(String shareCode, String ipAddress, String userAgent) {
        Share share = getActiveShareByCode(shareCode);
        if (share == null) {
            throw new IllegalArgumentException("分享不存在或已过期");
        }

        recordAccess(share.getId(), ipAddress, userAgent);

        ShareContentVO contentVO = new ShareContentVO();
        
        if (share.getPhotoId() != null) {
            contentVO.setType("photo");
            PhotoVO photo = photoService.getPhotoById(share.getUserId(), share.getPhotoId());
            contentVO.setContent(photo);
        } else if (share.getAlbumId() != null) {
            contentVO.setType("album");
            Album album = albumMapper.selectById(share.getAlbumId());
            if (album != null) {
                LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Photo::getAlbumId, share.getAlbumId());
                wrapper.eq(Photo::getIsDeleted, 0);
                List<Photo> photos = photoMapper.selectList(wrapper);
                List<PhotoVO> photoVOs = photos.stream()
                        .map(photoService::convertToVO)
                        .collect(Collectors.toList());
                contentVO.setPhotos(photoVOs);
            }
        }

        share.setViewCount(share.getViewCount() + 1);
        shareMapper.updateById(share);

        return contentVO;
    }

    public ShareContentVO accessShare(String shareCode, String password, String ipAddress, String userAgent) {
        Share share = getActiveShareByCode(shareCode);
        if (share == null) {
            throw new IllegalArgumentException("分享不存在或已过期");
        }

        if (share.getPassword() != null && !share.getPassword().isEmpty()) {
            if (password == null || !password.equals(share.getPassword())) {
                throw new IllegalArgumentException("访问密码错误");
            }
        }

        return getShareContent(shareCode, ipAddress, userAgent);
    }

    public List<ShareVO> getShareList(Long userId) {
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Share::getUserId, userId);
        wrapper.orderByDesc(Share::getCreatedAt);
        List<Share> shares = shareMapper.selectList(wrapper);
        return shares.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelShare(Long userId, String shareCode) {
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Share::getShareCode, shareCode);
        wrapper.eq(Share::getUserId, userId);
        Share share = shareMapper.selectOne(wrapper);

        if (share == null) {
            throw new IllegalArgumentException("分享不存在");
        }

        share.setIsActive(0);
        shareMapper.updateById(share);
    }

    public ShareVO getShareStatistics(Long userId, Long shareId) {
        Share share = shareMapper.selectById(shareId);
        if (share == null || !share.getUserId().equals(userId)) {
            throw new IllegalArgumentException("分享不存在");
        }
        return convertToVO(share);
    }

    private Share getActiveShareByCode(String shareCode) {
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Share::getShareCode, shareCode);
        wrapper.eq(Share::getIsActive, 1);
        Share share = shareMapper.selectOne(wrapper);

        if (share == null) {
            return null;
        }

        if (share.getExpireAt() != null && share.getExpireAt().isBefore(LocalDateTime.now())) {
            share.setIsActive(0);
            shareMapper.updateById(share);
            return null;
        }

        return share;
    }

    private void recordAccess(Long shareId, String ipAddress, String userAgent) {
        ShareAccess access = new ShareAccess();
        access.setShareId(shareId);
        access.setIpAddress(ipAddress);
        access.setUserAgent(userAgent);
        shareAccessMapper.insert(access);
    }

    private ShareVO convertToVO(Share share) {
        ShareVO vo = new ShareVO();
        BeanUtils.copyProperties(share, vo);
        vo.setHasPassword(share.getPassword() != null && !share.getPassword().isEmpty());

        if (share.getPhotoId() != null) {
            Photo photo = photoMapper.selectById(share.getPhotoId());
            if (photo != null) {
                vo.setPhotoTitle(photo.getTitle());
            }
        }

        if (share.getAlbumId() != null) {
            Album album = albumMapper.selectById(share.getAlbumId());
            if (album != null) {
                vo.setAlbumName(album.getName());
            }
        }

        return vo;
    }

    public long countByUserId(Long userId) {
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Share::getUserId, userId);
        wrapper.eq(Share::getIsActive, 1);
        return shareMapper.selectCount(wrapper);
    }
}
