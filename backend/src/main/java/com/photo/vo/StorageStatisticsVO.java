package com.photo.vo;

import lombok.Data;

@Data
public class StorageStatisticsVO {
    private Long totalStorage;
    private Long usedStorage;
    private Long availableStorage;
    private Integer photoCount;
    private Integer albumCount;
    private Integer userCount;
}
