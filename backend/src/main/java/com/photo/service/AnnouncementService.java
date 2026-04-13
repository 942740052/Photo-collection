package com.photo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.dto.AnnouncementDTO;
import com.photo.entity.Announcement;
import com.photo.mapper.AnnouncementMapper;
import com.photo.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {

    private final AnnouncementMapper announcementMapper;

    public List<AnnouncementVO> getActiveAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1);
        wrapper.orderByDesc(Announcement::getCreatedAt);
        List<Announcement> announcements = announcementMapper.selectList(wrapper);
        return announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementVO> getAllAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getCreatedAt);
        List<Announcement> announcements = announcementMapper.selectList(wrapper);
        return announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementVO createAnnouncement(AnnouncementDTO dto) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(dto, announcement);
        announcementMapper.insert(announcement);
        return convertToVO(announcement);
    }

    @Transactional
    public AnnouncementVO updateAnnouncement(Long id, AnnouncementDTO dto) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new IllegalArgumentException("公告不存在");
        }

        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setType(dto.getType());
        if (dto.getStatus() != null) {
            announcement.setStatus(dto.getStatus());
        }

        announcementMapper.updateById(announcement);
        return convertToVO(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    private AnnouncementVO convertToVO(Announcement announcement) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(announcement, vo);
        return vo;
    }
}
