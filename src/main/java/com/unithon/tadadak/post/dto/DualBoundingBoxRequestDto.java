package com.unithon.tadadak.post.dto;

import lombok.Data;

@Data
public class DualBoundingBoxRequestDto {
    private BoundingBoxRequestDto departureBox;   // 출발지 화면 범위
    private BoundingBoxRequestDto destinationBox; // 도착지 화면 범위
}
