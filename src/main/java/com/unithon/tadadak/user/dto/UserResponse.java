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

    // 로그인 응답용 필드들
    private String accessToken;
    private String firebaseToken;
    private String message;

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

    // 로그인 성공 응답용 정적 메서드
    public static UserResponse loginSuccess(User user, String accessToken, String firebaseToken) {
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
                .accessToken(accessToken)
                .firebaseToken(firebaseToken)
                .message("로그인 성공")
                .build();
    }

    // 로그인 실패 응답용 정적 메서드
    public static UserResponse loginFailure(String message) {
        return UserResponse.builder()
                .message(message)
                .build();
    }
}

