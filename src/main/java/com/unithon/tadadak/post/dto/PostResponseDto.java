package com.unithon.tadadak.post.dto;

import com.unithon.tadadak.post.domain.Post;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostResponseDto {
    private Long postId;
    private Long hostId;
    private String hostName;  // 📝 추가: 호스트 이름
    private Long startLocationId;
    private Long endLocationId;
    private Integer desiredMembers;
    private Integer estimatedPrice;
    private Integer estimatePricePerMember;  // 그룹원 수로 나눈 값

    private LocalDateTime departureTime;
    private String status;
    private LocalDateTime createdAt;

    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;

    // === 그룹 정보 추가 ===
    private Long groupId;              // 📝 추가: 연결된 그룹 ID
    private Integer currentMembers;    // 📝 추가: 현재 참여 인원
    private Integer maxMembers;        // 📝 추가: 최대 인원
    private String groupStatus;        // 📝 추가: 그룹 상태
    private boolean isAvailable;       // 📝 추가: 참여 가능 여부

    public static PostResponseDto fromEntity(Post post) {
        // 첫 번째 그룹 정보 가져오기 (보통 Post당 하나의 그룹)
        var groups = post.getGroups();
        var firstGroup = (groups != null && !groups.isEmpty()) ? groups.get(0) : null;
        // 2) 예상 총액(Null-safe)
        Integer estimatedPrice = post.getEstimatedPrice();
        int estimated = (estimatedPrice != null) ? estimatedPrice : 0;

        // 3) 현재 멤버 수(Null-safe)
        Integer cur = (firstGroup != null) ? firstGroup.getCurrentMemberCount() : null;
        int currentMembers = (cur != null) ? cur : 0;

        // 4) 1/N (0명일 때 0으로 반환; 정책에 따라 예외 처리 가능)
        int estimatePerMember = (currentMembers > 0) ? (estimated / currentMembers) : 0;

        return PostResponseDto.builder()
                .postId(post.getPostId())
                .hostId(post.getHost().getUserId())
                .hostName(post.getHost().getUsername())
                .startLocationId(post.getStartLocation().getLocationId())
                .endLocationId(post.getEndLocation().getLocationId())
                .desiredMembers(post.getDesiredMembers())
                .estimatedPrice(estimated)
                .estimatePricePerMember(estimatePerMember)
                .departureTime(post.getDepartureTime())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())

                .startLat(post.getStartLocation().getLatitude())
                .startLng(post.getStartLocation().getLongitude())
                .endLat(post.getEndLocation().getLatitude())
                .endLng(post.getEndLocation().getLongitude())

                .groupId(firstGroup != null ? firstGroup.getGroupId() : null)
                .currentMembers(currentMembers)
                .maxMembers(firstGroup != null ? firstGroup.getMaxMemberCount() : post.getDesiredMembers())
                .groupStatus(firstGroup != null ? firstGroup.getStatus() : "NO_GROUP")
                .isAvailable(firstGroup == null || firstGroup.getCurrentMemberCount() < firstGroup.getMaxMemberCount())
                .build();
    }
}
