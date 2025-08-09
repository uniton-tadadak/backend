package com.unithon.tadadak.user.dto;

import com.unithon.tadadak.user.domain.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private Float trustScore;
    private int penaltyCount;
    private int praiseCount;
    private LocalDateTime createdAt;
    
    // 추천 시스템 가중치
    private Double moneyWeight;
    private Double distanceWeight;
    private Double trustWeight;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .trustScore(user.getTrustScore())
                .penaltyCount(user.getPenaltyCount())
                .praiseCount(user.getPraiseCount())
                .createdAt(user.getCreatedAt())
                .moneyWeight(user.getMoneyWeight())
                .distanceWeight(user.getDistanceWeight())
                .trustWeight(user.getTrustWeight())
                .build();
    }
}

