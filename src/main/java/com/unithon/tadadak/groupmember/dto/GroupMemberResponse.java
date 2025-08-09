package com.unithon.tadadak.groupmember.dto;

import com.unithon.tadadak.groupmember.domain.GroupMember;
import com.unithon.tadadak.groupmember.domain.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupMemberResponse {
    private Long groupId;
    private Long userId;
    private boolean isHost;
    private PaymentStatus paymentStatus;
    private LocalDateTime joinedAt;

    public static GroupMemberResponse from(GroupMember gm) {
        return GroupMemberResponse.builder()
                .groupId(gm.getGroup().getGroupId())               // 연관관계를 통한 접근
                .userId(gm.getUser().getUserId())                  // 연관관계를 통한 접근
                .isHost(gm.isHost())
                .paymentStatus(gm.getPaymentStatus())
                .joinedAt(gm.getJoinedAt())
                .build();
    }
}

