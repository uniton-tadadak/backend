package com.unithon.tadadak.location.dto;

import com.unithon.tadadak.location.domain.Location;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationResponseDto {
    private Long locationId;
    private Double latitude;
    private Double longitude;
    private Long userId;     // nullable
    private Long postId;     // nullable  
    private LocalDateTime createdAt;

    public static LocationResponseDto from(Location location) {
        return LocationResponseDto.builder()
                .locationId(location.getLocationId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .userId(location.getUser() != null ? location.getUser().getUserId() : null)
                .postId(location.getPostId())
                .createdAt(location.getCreatedAt())
                .build();
    }
}

