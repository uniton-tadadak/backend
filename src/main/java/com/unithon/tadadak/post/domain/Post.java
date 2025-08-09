package com.unithon.tadadak.post.domain;

import com.unithon.tadadak.location.domain.Location;
import com.unithon.tadadak.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_location_id", nullable = false)
    private Location startLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_location_id", nullable = false)
    private Location endLocation;

    private Integer desiredMembers;
    private Integer estimatedPrice;
    private LocalDateTime departureTime;
    private String status; // OPEN, CLOSED, EXPIRED
    private LocalDateTime createdAt;

    // === 연관관계 매핑 ===

    /**
     * 이 Post 기반으로 생성된 그룹들
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.unithon.tadadak.groups.domain.Groups> groups = new ArrayList<>();
}