package com.unithon.tadadak.groups.dto;

import lombok.Getter;

@Getter
public class GroupsRequest {
    private Long postId;
    private int maxMemberCount;
    private int currentMemberCount;
    private String status; // nullable
}
