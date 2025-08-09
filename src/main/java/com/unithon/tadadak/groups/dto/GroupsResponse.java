package com.unithon.tadadak.groups.dto;

import com.unithon.tadadak.groups.domain.Groups;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class GroupsResponse {
    private Long groupId;
    private Long postId;
    private int maxMemberCount;
    private int currentMemberCount;
    private String status;
    private LocalDateTime createdAt;

    public static GroupsResponse from(Groups groups) {
        return GroupsResponse.builder()
                .groupId(groups.getGroupId())
                .postId(groups.getPost().getPostId())  // 📝 변경: groups.getPostId() → groups.getPost().getPostId()
                .maxMemberCount(groups.getMaxMemberCount())
                .currentMemberCount(groups.getCurrentMemberCount())
                .status(groups.getStatus())
                .createdAt(groups.getCreatedAt())
                .build();
    }
}
