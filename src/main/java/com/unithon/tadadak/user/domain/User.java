package com.unithon.tadadak.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "trust_score", nullable = false)
    private Float trustScore;

    @Column(name = "penalty_count", nullable = false)
    private int penaltyCount;

    @Column(name = "praise_count", nullable = false)
    private int praiseCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 추천 시스템 가중치 필드들 (0.0 ~ 1.0 범위)
    @Column(name = "money_weight")
    private Double moneyWeight;

    @Column(name = "distance_weight")
    private Double distanceWeight;

    @Column(name = "trust_weight")
    private Double trustWeight;

    // === 연관관계 매핑 ===
    
    /**
     * 사용자가 작성한 Post들 (방장으로서)
     */
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.unithon.tadadak.post.domain.Post> hostPosts = new ArrayList<>();

    /**
     * 사용자가 참여한 GroupMember들 
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.unithon.tadadak.groupmember.domain.GroupMember> groupMemberships = new ArrayList<>();

    /**
     * 사용자의 결제 내역들
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.unithon.tadadak.bill.domain.Bill> bills = new ArrayList<>();

    /**
     * 사용자가 작성한 신고들 (신고자로서)
     */
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.unithon.tadadak.report.domain.Report> reportsMade = new ArrayList<>();

    /**
     * 사용자가 신고당한 내역들 (피신고자로서)
     */
    @OneToMany(mappedBy = "reported", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.unithon.tadadak.report.domain.Report> reportsReceived = new ArrayList<>();

    /**
     * 사용자의 위치 정보들
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.unithon.tadadak.location.domain.Location> locations = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.trustScore == null) {
            this.trustScore = 36.5f;
        }
        // 가중치 기본값 설정
        if (this.moneyWeight == null) {
            this.moneyWeight = 0.33;
        }
        if (this.distanceWeight == null) {
            this.distanceWeight = 0.33;
        }
        if (this.trustWeight == null) {
            this.trustWeight = 0.34;
        }
    }

    public void updateInfo(String username, Float trustScore, int penaltyCount, int praiseCount) {
        if (username != null) this.username = username;
        if (trustScore != null) this.trustScore = trustScore;
        this.penaltyCount = penaltyCount;
        this.praiseCount = praiseCount;
    }

    public void updateWeights(Double moneyWeight, Double distanceWeight, Double trustWeight) {
        if (moneyWeight != null) this.moneyWeight = moneyWeight;
        if (distanceWeight != null) this.distanceWeight = distanceWeight;
        if (trustWeight != null) this.trustWeight = trustWeight;
    }
}

