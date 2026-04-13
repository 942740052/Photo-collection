package com.photo.controller;

import com.photo.common.Result;
import com.photo.dto.ShareCreateDTO;
import com.photo.service.ShareService;
import com.photo.vo.ShareContentVO;
import com.photo.vo.ShareVO;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/photo/{id}")
    public Result<ShareVO> sharePhoto(
            @RequestAttribute Long userId,
            @PathVariable Long id,
            @RequestBody(required = false) ShareCreateDTO dto) {
        if (dto == null) {
            dto = new ShareCreateDTO();
        }
        dto.setPhotoId(id);
        ShareVO share = shareService.createShare(userId, dto);
        return Result.success("分享成功", share);
    }

    @PostMapping("/album/{id}")
    public Result<ShareVO> shareAlbum(
            @RequestAttribute Long userId,
            @PathVariable Long id,
            @RequestBody(required = false) ShareCreateDTO dto) {
        if (dto == null) {
            dto = new ShareCreateDTO();
        }
        dto.setAlbumId(id);
        ShareVO share = shareService.createShare(userId, dto);
        return Result.success("分享成功", share);
    }

    @GetMapping("/{code}")
    public Result<ShareContentVO> getShare(
            @PathVariable String code,
            HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        ShareContentVO content = shareService.getShareContent(code, ipAddress, userAgent);
        return Result.success(content);
    }

    @PostMapping("/{code}/verify")
    public Result<ShareContentVO> verifyShare(
            @PathVariable String code,
            @RequestBody(required = false) String password,
            HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        ShareContentVO content = shareService.accessShare(code, password, ipAddress, userAgent);
        return Result.success(content);
    }

    @GetMapping
    public Result<List<ShareVO>> getShareList(@RequestAttribute Long userId) {
        List<ShareVO> shares = shareService.getShareList(userId);
        return Result.success(shares);
    }

    @DeleteMapping("/{code}")
    public Result<Void> cancelShare(
            @RequestAttribute Long userId,
            @PathVariable String code) {
        shareService.cancelShare(userId, code);
        return Result.success("取消分享成功", null);
    }

    @GetMapping("/{id}/statistics")
    public Result<ShareVO> getShareStatistics(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        ShareVO share = shareService.getShareStatistics(userId, id);
        return Result.success(share);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
