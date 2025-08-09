package com.unithon.tadadak.groupmember.dto;

import com.unithon.tadadak.groupmember.domain.PaymentStatus;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupMemberRequest {
    private Long groupId;
    private Long userId;
    private boolean isHost;
    private PaymentStatus paymentStatus;
}

