package com.unithon.tadadak.recommend.api;

import com.unithon.tadadak.recommend.service.RecommendService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 📝 새로운 방식: 출발지+도착지 기반 추천 (권장)
     */
//    @GetMapping("/route")
//    public List<Long> recommendByRoute(
//            @RequestParam double depLat,   // 출발지 위도
//            @RequestParam double depLng,   // 출발지 경도
//            @RequestParam double destLat,  // 도착지 위도
//            @RequestParam double destLng,  // 도착지 경도
//            @RequestParam(defaultValue = "1000") double radius, // 허용 반경(미터)
//            @RequestParam(defaultValue = "10") int topN,
//            HttpServletRequest request
//    ) {
//        Long userId = getCurrentUserId(request);
//        return recommendService.recommendByRoute(userId, depLat, depLng, destLat, destLng, radius, topN);
//    }
    
    /**
     * 📝 기존 방식: 단일 좌표 기반 추천 (하위 호환성)
     */
    @GetMapping
    public List<Long> recommend(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radiusMeters,
            @RequestParam(defaultValue = "10") int topN,
            HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        return recommendService.recommend(userId, lat, lng, radiusMeters, topN);
    }

    /**
     * 📝 사용자 추천 통계 조회
     */
    @GetMapping("/stats")
    public RecommendService.RecommendStatsDto getRecommendStats(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return recommendService.getUserRecommendStats(userId);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userId;
    }
} 