package com.photo.dto;

import lombok.Data;

@Data
public class BackupCreateDTO {
    private String name;
    private String type = "full";
}
