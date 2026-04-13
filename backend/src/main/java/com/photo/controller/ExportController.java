package com.photo.controller;

import com.photo.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/album/{albumId}")
    public ResponseEntity<Resource> exportByAlbum(@RequestAttribute Long userId, @PathVariable Long albumId) {
        File file = exportService.exportByAlbum(userId, albumId);
        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    @GetMapping("/date-range")
    public ResponseEntity<Resource> exportByDateRange(
            @RequestAttribute Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        File file = exportService.exportByDateRange(userId, startTime, endTime);
        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    @GetMapping("/favorites")
    public ResponseEntity<Resource> exportFavorites(@RequestAttribute Long userId) {
        File file = exportService.exportFavorites(userId);
        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    @GetMapping("/all")
    public ResponseEntity<Resource> exportAll(@RequestAttribute Long userId) {
        File file = exportService.exportAll(userId);
        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
}
