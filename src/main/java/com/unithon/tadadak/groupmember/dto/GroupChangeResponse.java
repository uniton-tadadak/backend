package com.unithon.tadadak.groupmember.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupChangeResponse {
    private Long postId;
    private Long groupId;
    private Integer currentMembers;          // 현재 인원
    private Integer estimatedPrice;          // 총 예상 금액
    private Integer estimatePricePerMember;  // 1인당 예상 금액
}
