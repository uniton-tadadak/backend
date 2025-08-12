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

    // 🔴 클라에서 받은 주소 (DB에 저장 안 함, 응답에만 사용)
    private String startAddress;
    private String endAddress;

    private Integer duration; // 선택: 분 또는 초(팀 규약)
}

