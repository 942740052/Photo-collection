package com.photo.dto;

import lombok.Data;

import java.util.List;

@Data
public class PhotoUpdateDTO {
    private Long id;

    private String title;

    private String description;

    private Long albumId;

    private List<Long> tagIds;
}
