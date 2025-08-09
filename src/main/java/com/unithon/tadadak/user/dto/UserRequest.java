package com.unithon.tadadak.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String username;
    private Float trustScore;
    private int penaltyCount;
    private int praiseCount;
    
    // 추천 시스템 가중치 (0.0 ~ 1.0 범위)
    private Double moneyWeight;
    private Double distanceWeight;
    private Double trustWeight;
}
