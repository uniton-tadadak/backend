package com.unithon.tadadak.groups.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ride_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Groups {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "max_member_count", nullable = false)
    private int maxMemberCount;

    @Column(name = "current_member_count", nullable = false)
    private int currentMemberCount;

    @Column(name = "status", nullable = false)
    private String status; // WAITING, IN_PROGRESS, COMPLETED

    // === 연관관계 매핑 ===

    /**
     * 그룹이 기반이 되는 Post
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private com.unithon.tadadak.post.domain.Post post;

    /**
     * 그룹에 참여한 멤버들
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.unithon.tadadak.groupmember.domain.GroupMember> members = new ArrayList<>();

    /**
     * 그룹의 결제 내역들
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.unithon.tadadak.bill.domain.Bill> bills = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "WAITING";
    }
}
