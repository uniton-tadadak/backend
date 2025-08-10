package com.unithon.tadadak.location.api;

import com.unithon.tadadak.location.domain.Location;
import com.unithon.tadadak.location.dto.LocationRequestDto;
import com.unithon.tadadak.location.dto.LocationResponseDto;
import com.unithon.tadadak.location.service.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationResponseDto> create(@RequestBody LocationRequestDto dto, HttpServletRequest request) {
        // JWT에서 사용자 정보 추출
        Long userId = getCurrentUserId(request);
        // dto에 userId 설정 (필요시)
        
        Location location = locationService.create(dto);
        return ResponseEntity.ok(LocationResponseDto.from(location));
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userId;
    }
}

