package com.unithon.tadadak.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unithon.tadadak.post.domain.Post;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDto {
    private Long postId;
    private Long hostId;
    private String hostName;

    private Long startLocationId;
    private Long endLocationId;

    private Integer desiredMembers;
    private Integer estimatedPrice;
    private Integer estimatePricePerMember;

    private LocalDateTime departureTime;
    private String status;
    private LocalDateTime createdAt;

    // 좌표 + 주소(주소는 주입 없으면 null)
    private double startLat;
    private double startLng;
    private String startAddress;

    private double endLat;
    private double endLng;
    private String endAddress;

    private Long groupId;
    private Integer currentMembers;
    private Integer maxMembers;
    private Integer duration; // int 형식
    private String groupStatus;
    private boolean isAvailable;

    // 기본 fromEntity (주소 미주입)
    public static PostResponseDto fromEntity(Post post) {
        return fromEntity(post, null, null, null);
    }

    // 주소/기간 등 외부 주입 버전
    public static PostResponseDto fromEntity(Post post,
                                             String startAddress,
                                             String endAddress,
                                             Integer duration // 분/초 중 팀 규칙대로
    ) {
        var groups = post.getGroups();
        var firstGroup = (groups != null && !groups.isEmpty()) ? groups.get(0) : null;

        int estimated = post.getEstimatedPrice() != null ? post.getEstimatedPrice() : 0;
        Integer curMembers = (firstGroup != null) ? firstGroup.getCurrentMemberCount() : null;
        int currentMembers = (curMembers != null) ? curMembers : 0;
        int estimatePerMember = currentMembers > 0 ? (estimated / currentMembers) : 0;

        return PostResponseDto.builder()
                .postId(post.getPostId())
                .hostId(post.getHost().getUserId())
                .hostName(post.getHost().getUsername())
                .startLocationId(post.getStartLocation() != null ? post.getStartLocation().getLocationId() : null)
                .endLocationId(post.getEndLocation() != null ? post.getEndLocation().getLocationId() : null)
                .desiredMembers(post.getDesiredMembers())
                .estimatedPrice(estimated)
                .estimatePricePerMember(estimatePerMember)
                .departureTime(post.getDepartureTime())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .startLat(post.getStartLocation().getLatitude())
                .startLng(post.getStartLocation().getLongitude())
                .startAddress(post.getStartAddress()) // 🔴 DB 저장 안 함: 클라에서 받은 걸 그대로
                .endLat(post.getEndLocation().getLatitude())
                .endLng(post.getEndLocation().getLongitude())
                .endAddress(post.getEndAddress())     // 🔴 DB 저장 안 함: 클라에서 받은 걸 그대로
                .groupId(firstGroup != null ? firstGroup.getGroupId() : null)
                .currentMembers(currentMembers)
                .maxMembers(firstGroup != null ? firstGroup.getMaxMemberCount() : post.getDesiredMembers())
                .duration(post.getDuration()) // 없으면 null
                .groupStatus(firstGroup != null ? firstGroup.getStatus() : "NO_GROUP")
                .isAvailable(firstGroup == null || currentMembers < (firstGroup != null ? firstGroup.getMaxMemberCount() : Integer.MAX_VALUE))
                .build();
    }
}
