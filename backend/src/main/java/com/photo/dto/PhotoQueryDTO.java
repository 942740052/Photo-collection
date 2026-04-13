package com.photo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PhotoQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private Long albumId;
    private String keyword;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private LocalDateTime shootStartTime;
    private LocalDateTime shootEndTime;
    
    private Long tagId;
    private String tagIds;
    
    private Integer isFavorite;
    private Integer isDeleted = 0;
    
    private String cameraModel;
    private String location;
    private String locationFilter = "contains";
    
    private Double gpsLatitude;
    private Double gpsLongitude;
    private Double gpsRadius;
    
    private String sortBy = "created_at";
    private String sortOrder = "DESC";
    
    private Long minFileSize;
    private Long maxFileSize;
    
    private Integer widthMin;
    private Integer widthMax;
    private Integer heightMin;
    private Integer heightMax;
    
    public List<Long> getTagIdList() {
        if (tagIds == null || tagIds.isEmpty()) {
            return null;
        }
        String[] ids = tagIds.split(",");
        List<Long> result = new java.util.ArrayList<>();
        for (String id : ids) {
            try {
                result.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
            }
        }
        return result.isEmpty() ? null : result;
    }
}
