package com.unithon.tadadak.recommend.service;

import com.unithon.tadadak.post.domain.Post;
import com.unithon.tadadak.recommend.dto.Candidate;
import com.unithon.tadadak.recommend.dto.RecommendRequest;
import com.unithon.tadadak.recommend.infra.RecommendClient;
import com.unithon.tadadak.post.repository.NearbyPostRow;
import com.unithon.tadadak.post.repository.PostRepository;
import com.unithon.tadadak.user.domain.User;
import com.unithon.tadadak.user.repository.UserRepository;
import com.unithon.tadadak.groupmember.repository.GroupMemberRepository;
import com.unithon.tadadak.groups.repository.GroupsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final RecommendClient client;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupsRepository groupsRepository;

    /**
     * 📝 새로운 방식: 출발지와 도착지를 모두 고려한 추천
     */
    public List<Long> recommendByRoute(Long userId, double depLat, double depLng, 
                                      double destLat, double destLng, double radius, int topN) {
        try {
            log.info("Route-based recommendation for user {} from ({}, {}) to ({}, {}) within {}m", 
                    userId, depLat, depLng, destLat, destLng, radius);
            
            // 1) 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            
            // 2) 경계박스 생성 (출발지와 도착지 각각 반경 기반)
            double[] depBox = createBoundingBox(depLat, depLng, radius);
            double[] destBox = createBoundingBox(destLat, destLng, radius);
            
            // 3) 교집합 쿼리로 적절한 Post들 조회
            List<Post> posts = postRepository.findAllInIntersectionWithDetails(
                    depBox[0], depBox[1], depBox[2], depBox[3],    // 출발지 박스
                    destBox[0], destBox[1], destBox[2], destBox[3] // 도착지 박스
            );
            
            // 4) 사용자가 이미 참여한 그룹 필터링
            List<Post> filteredPosts = posts.stream()
                    .filter(post -> !hasUserJoined(userId, post))
                    .filter(post -> "OPEN".equals(post.getStatus()))
                    .filter(post -> post.getDepartureTime().isAfter(java.time.LocalDateTime.now()))
                    .toList();
            
            log.info("Found {} suitable posts for route-based recommendation", filteredPosts.size());
            
            if (filteredPosts.isEmpty()) {
                return List.of();
            }
            
            // 5) 거리와 신뢰도 계산하여 후보 생성
            List<Candidate> candidates = filteredPosts.stream()
                    .map(post -> createCandidate(post, depLat, depLng))
                    .toList();
            
            // 6) AI 호출
            RecommendRequest request = new RecommendRequest(
                    userId,
                    nullToZero(user.getMoneyWeight()),
                    nullToZero(user.getDistanceWeight()), 
                    nullToZero(user.getTrustWeight()),
                    candidates,
                    topN
            );
            
            return client.rank(request);
            
        } catch (Exception e) {
            log.error("Route-based recommendation failed for user {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 📝 기존 방식: 단일 좌표 반경 기반 추천 (하위 호환성)
     */
    public List<Long> recommend(Long userId, double lat, double lng, double radiusMeters, int topN) {
        try {
            // 1) 입력 검증
            validateRecommendRequest(userId, lat, lng, radiusMeters, topN);
            
            // 2) 유저의 가중치 (연관관계 활용)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            // 3) 반경 내 후보 N개 조회 (정원 미달 + 미참여 + 유효시간만)
            List<NearbyPostRow> rows = postRepository.findNearbyWithHostTrust(lat, lng, radiusMeters, userId, 50);
            
            log.info("Found {} nearby candidates for user {} within {}m radius", rows.size(), userId, radiusMeters);

            // 4) 후보가 없으면 빈 목록 반환
            if (rows.isEmpty()) {
                log.warn("No nearby candidates found for user {} within {}m radius", userId, radiusMeters);
                return List.of();
            }

            // 5) CandidateDto 변환
            List<Candidate> candidates = rows.stream()
                    .map(r -> new Candidate(
                            r.getPostId(),
                            nullToZero(r.getEstimatedPrice()),
                            nullToZero(r.getDistanceM()),
                            nullToZero(r.getTrustScore())
                    ))
                    .toList();

            // 6) AI 요청 본문 구성
            RecommendRequest req = new RecommendRequest(
                    userId,
                    nullToZero(user.getMoneyWeight()),
                    nullToZero(user.getDistanceWeight()),
                    nullToZero(user.getTrustWeight()),
                    candidates,
                    Math.min(topN, candidates.size()) // topN이 후보 수보다 클 수 없음
            );

            // 7) FastAPI 호출 → 정렬된 ID 목록 반환
            List<Long> recommendations = client.rank(req);
            
            // 8) 결과 검증 및 로깅
            List<Long> validRecommendations = validateRecommendations(recommendations, candidates);
            
            log.info("AI recommended {} valid posts for user {}: {}", validRecommendations.size(), userId, validRecommendations);
            return validRecommendations;
            
        } catch (Exception e) {
            log.error("Error occurred while generating recommendations for user {}: {}", userId, e.getMessage(), e);
            // 에러 발생 시 빈 목록 반환 (fallback)
            return List.of();
        }
    }

    /**
     * 추천 요청 파라미터 검증
     */
    private void validateRecommendRequest(Long userId, double lat, double lng, double radiusMeters, int topN) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Invalid latitude: " + lat);
        }
        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Invalid longitude: " + lng);
        }
        if (radiusMeters <= 0 || radiusMeters > 50000) { // 최대 50km
            throw new IllegalArgumentException("Invalid radius: " + radiusMeters);
        }
        if (topN <= 0 || topN > 100) { // 최대 100개
            throw new IllegalArgumentException("Invalid topN: " + topN);
        }
    }

    /**
     * AI 추천 결과 검증
     */
    private List<Long> validateRecommendations(List<Long> recommendations, List<Candidate> candidates) {
        if (recommendations == null) {
            return List.of();
        }
        
        // 원본 후보에 없는 ID가 추천되지 않도록 검증
        List<Long> candidateIds = candidates.stream().map(Candidate::postId).toList();
        
        return recommendations.stream()
                .filter(id -> id != null && candidateIds.contains(id))
                .distinct()
                .toList();
    }

    /**
     * 사용자의 참여 이력 기반 가중치 자동 조정 (향후 확장용)
     */
    public void updateUserWeightsBasedOnHistory(Long userId) {
        try {
            // 📝 연관관계를 활용한 사용자 이력 분석
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            
            // 사용자의 그룹 참여 이력 분석
            List<Long> userGroupIds = groupMemberRepository.findGroupIdsByUserId(userId);
            
            if (userGroupIds.isEmpty()) {
                log.info("No group history found for user {}, keeping default weights", userId);
                return;
            }
            
            // 📝 향후 확장: 참여했던 그룹들의 패턴 분석
            // - 평균 거리 선호도
            // - 평균 가격 선호도  
            // - 신뢰도 민감도
            
            log.info("Analyzed {} groups for user {}, weight auto-adjustment not implemented yet", 
                    userGroupIds.size(), userId);
                    
        } catch (Exception e) {
            log.error("Error analyzing user history for {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 📝 새로 추가: 사용자의 추천 통계 조회
     */
    public RecommendStatsDto getUserRecommendStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        List<Long> groupIds = groupMemberRepository.findGroupIdsByUserId(userId);
        int totalParticipations = groupIds.size();
        
        // 현재 참여 중인 그룹 수
        int activeParticipations = (int) groupIds.stream()
                .map(groupsRepository::findById)
                .filter(opt -> opt.isPresent() && 
                              opt.get().getStatus().equals("WAITING") || 
                              opt.get().getStatus().equals("IN_PROGRESS"))
                .count();
        
        return RecommendStatsDto.builder()
                .userId(userId)
                .username(user.getUsername())
                .trustScore(user.getTrustScore().doubleValue())
                .totalParticipations(totalParticipations)
                .activeParticipations(activeParticipations)
                .moneyWeight(user.getMoneyWeight())
                .distanceWeight(user.getDistanceWeight())
                .trustWeight(user.getTrustWeight())
                .build();
    }

    // 📝 새로 추가: 추천 통계 DTO
    @lombok.Builder
    @lombok.Getter
    public static class RecommendStatsDto {
        private Long userId;
        private String username;
        private Double trustScore;
        private Integer totalParticipations;
        private Integer activeParticipations;
        private Double moneyWeight;
        private Double distanceWeight;
        private Double trustWeight;
    }

        private double nullToZero(Double v) {
        return v == null ? 0.0 : v;
    }
    
    /**
     * 📝 좌표와 반경으로 경계박스 생성
     * @return [minLat, maxLat, minLng, maxLng]
     */
    private double[] createBoundingBox(double lat, double lng, double radiusMeters) {
        // 대략적인 계산: 1도 ≈ 111,000m
        double latDelta = radiusMeters / 111000.0;
        double lngDelta = radiusMeters / (111000.0 * Math.cos(Math.toRadians(lat)));
        
        return new double[] {
            lat - latDelta,  // minLat
            lat + latDelta,  // maxLat
            lng - lngDelta,  // minLng
            lng + lngDelta   // maxLng
        };
    }
    
    /**
     * 📝 사용자가 특정 Post의 그룹에 이미 참여했는지 확인
     */
    private boolean hasUserJoined(Long userId, Post post) {
        return post.getGroups().stream()
                .anyMatch(group -> groupMemberRepository.existsByGroupIdAndUserId(group.getGroupId(), userId));
    }
    
    /**
     * 📝 Post에서 Candidate 객체 생성 (거리 계산 포함)
     */
    private Candidate createCandidate(Post post, double userLat, double userLng) {
        // 사용자 위치에서 Post 시작점까지의 거리 계산 (Haversine)
        double distance = calculateDistance(
            userLat, userLng, 
            post.getStartLocation().getLatitude(), 
            post.getStartLocation().getLongitude()
        );
        
        return new Candidate(
            post.getPostId(),
            post.getEstimatedPrice().doubleValue(),
            distance,
            post.getHost().getTrustScore().doubleValue()
        );
    }
    
    /**
     * 📝 Haversine 공식으로 두 좌표 간 거리 계산 (미터)
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000; // 지구 반지름 (미터)
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2);
                
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    private double nullToZero(Integer v) { 
        return v == null ? 0.0 : v.doubleValue(); 
    }
} 