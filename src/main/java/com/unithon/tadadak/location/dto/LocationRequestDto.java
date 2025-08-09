package com.unithon.tadadak.location.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationRequestDto {
    private Double latitude;
    private Double longitude;
    private Long userId;  // optional (실시간 위치일 때)
    private Long postId;  // optional (공지 위치일 때)
}

