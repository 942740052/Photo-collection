package com.photo.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlbumDTO {
    private Long id;

    @NotBlank(message = "相册名称不能为空")
    private String name;

    private String description;

    private Long coverPhotoId;

    private Integer isPublic;
}
