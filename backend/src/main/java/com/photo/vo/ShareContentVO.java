package com.photo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ShareContentVO {
    private String type;
    private Object content;
    private List<PhotoVO> photos;
}
