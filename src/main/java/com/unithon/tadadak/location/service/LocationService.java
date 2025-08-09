package com.unithon.tadadak.location.service;

import com.unithon.tadadak.location.domain.Location;
import com.unithon.tadadak.location.dto.LocationRequestDto;
import com.unithon.tadadak.location.repository.LocationRepository;
import com.unithon.tadadak.post.repository.PostRepository;
import com.unithon.tadadak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public Location create(LocationRequestDto dto) {
        return locationRepository.save(Location.builder()
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .user(dto.getUserId() != null ? userRepository.findById(dto.getUserId()).orElse(null) : null)
                .postId(dto.getPostId())  // DTO에서 제공된 경우에만 설정
                .createdAt(LocalDateTime.now())
                .build());
    }
}

