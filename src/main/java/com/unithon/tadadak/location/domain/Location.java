package com.unithon.tadadak.location.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    private Double latitude;
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private com.unithon.tadadak.user.domain.User user; // 실시간 위치 (nullable)

    // 📝 주의: 원래 스키마에서는 Post가 Location을 참조하는 구조
    // Location.post_id는 역참조용이 아니라 단순 FK 저장용
    @Column(name = "post_id") 
    private Long postId; // 공지 위치일 때의 Post ID (nullable)

    private LocalDateTime createdAt;
}
