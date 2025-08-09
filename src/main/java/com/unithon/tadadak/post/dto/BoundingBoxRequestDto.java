package com.unithon.tadadak.post.dto;

import lombok.Data;

@Data
public class BoundingBoxRequestDto {
    private double minLat;
    private double maxLat;
    private double minLng;
    private double maxLng;
}
