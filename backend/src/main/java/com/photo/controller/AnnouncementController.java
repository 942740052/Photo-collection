package com.photo.controller;

import com.photo.common.Result;
import com.photo.dto.AnnouncementDTO;
import com.photo.service.AnnouncementService;
import com.photo.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/announcements")
    public Result<List<AnnouncementVO>> getActiveAnnouncements() {
        List<AnnouncementVO> announcements = announcementService.getActiveAnnouncements();
        return Result.success(announcements);
    }

    @GetMapping("/admin/announcements")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<AnnouncementVO>> getAllAnnouncements() {
        List<AnnouncementVO> announcements = announcementService.getAllAnnouncements();
        return Result.success(announcements);
    }

    @PostMapping("/admin/announcements")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AnnouncementVO> createAnnouncement(@RequestBody AnnouncementDTO dto) {
        AnnouncementVO announcement = announcementService.createAnnouncement(dto);
        return Result.success("创建成功", announcement);
    }

    @PutMapping("/admin/announcements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AnnouncementVO> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody AnnouncementDTO dto) {
        AnnouncementVO announcement = announcementService.updateAnnouncement(id, dto);
        return Result.success("更新成功", announcement);
    }

    @DeleteMapping("/admin/announcements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success("删除成功", null);
    }
}
