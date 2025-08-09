package com.unithon.tadadak.location.api;

import com.unithon.tadadak.location.domain.Location;
import com.unithon.tadadak.location.dto.LocationRequestDto;
import com.unithon.tadadak.location.dto.LocationResponseDto;
import com.unithon.tadadak.location.service.LocationService;
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
    public ResponseEntity<LocationResponseDto> create(@RequestBody LocationRequestDto dto) {
        Location location = locationService.create(dto);
        return ResponseEntity.ok(LocationResponseDto.from(location));
    }
}

