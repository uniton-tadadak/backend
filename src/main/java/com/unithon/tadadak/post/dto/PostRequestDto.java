package com.unithon.tadadak.post.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostRequestDto {
    private Long hostId;
    private Long startLocationId;
    private Long endLocationId;
    private Integer desiredMembers;
    private Integer estimatedPrice;
    private LocalDateTime departureTime;

    private String startAddress;
    private String endAddress;

    private Integer duration; // 선택: 분 또는 초(팀 규약)
}

