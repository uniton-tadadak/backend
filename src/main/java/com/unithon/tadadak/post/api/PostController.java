package com.unithon.tadadak.post.api;

import com.unithon.tadadak.post.domain.Post;
import com.unithon.tadadak.post.dto.DualBoundingBoxRequestDto;
import com.unithon.tadadak.post.dto.PostRequestDto;
import com.unithon.tadadak.post.dto.PostResponseDto;
import com.unithon.tadadak.post.service.PostService;
import com.unithon.tadadak.recommend.service.RecommendService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final RecommendService recommendService;

    @PostMapping
    public ResponseEntity<PostResponseDto> create(@RequestBody PostRequestDto dto,
                                                  HttpServletRequest request) {
        // 1) JWT에서 사용자 ID 추출 → 호스트 설정
        Long userId = getCurrentUserId(request);
        dto.setHostId(userId);

        // 2) 서비스로 저장 (lat/lng 등 엔티티 필드만 영속)
        Post post = postService.createPost(dto);

        // 3) 주소/기간은 DB에 저장하지 않고, 클라이언트에서 들어온 값을 그대로 응답에 실어준다
        PostResponseDto res = PostResponseDto.fromEntity(
                post,
                dto.getStartAddress(),  // 🔴 클라 → 응답 에코
                dto.getEndAddress(),    // 🔴 클라 → 응답 에코
                dto.getDuration()       // 🔴 int(분/초 팀 규약대로) 그대로 에코
        );

        return ResponseEntity.ok(res);
    }


    /**
     * 📝 새로 추가: Post와 Location을 함께 생성 (편의 API)
     */
    @PostMapping("/with-locations")
    public ResponseEntity<PostResponseDto> createWithLocations(
            @RequestBody PostService.CreatePostWithLocationsRequest request,
            HttpServletRequest httpRequest) {
        // JWT에서 사용자 ID 추출하여 hostId로 설정
        Long userId = getCurrentUserId(httpRequest);
        // CreatePostWithLocationsRequest에 setHostId 메서드가 있다면 설정
        // request.setHostId(userId);  // 필요 시 추가
        
        PostResponseDto result = postService.createPostWithLocations(request);
        return ResponseEntity.ok(result);
    }

//    @PostMapping("/intersection")
//    public ResponseEntity<List<PostResponseDto>> getIntersectionPosts(
//            @RequestBody DualBoundingBoxRequestDto boxes) {
//        List<PostResponseDto> result = postService.getPostsInIntersection(boxes);
//        return ResponseEntity.ok(result);
//    }

    /**
     * 📝 새로운 방식: 출발지+도착지 기반 추천 (권장)
     */
//    @GetMapping("/recommend/route")
//    public ResponseEntity<List<PostResponseDto>> getRecommendedPostsByRoute(
//            @RequestParam double depLat,   // 출발지 위도
//            @RequestParam double depLng,   // 출발지 경도
//            @RequestParam double destLat,  // 도착지 위도
//            @RequestParam double destLng,  // 도착지 경도
//            @RequestParam(defaultValue = "1000") double radius, // 허용 반경(미터)
//            @RequestParam(defaultValue = "10") int topN,
//            HttpServletRequest request
//    ) {
//        Long userId = getCurrentUserId(request);
//
//        // 1) AI 추천으로 Post ID 목록 가져오기 (route-based)
//        List<Long> recommendedIds = recommendService.recommendByRoute(userId, depLat, depLng, destLat, destLng, radius, topN);
//
//        // 2) Post ID를 실제 Post 상세정보로 변환
//        List<PostResponseDto> result = postService.getPostsByIds(recommendedIds);
//
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("/recommend/route/box")
    public ResponseEntity<RoomsResponse> getRecommendedByBoxes(
            @RequestBody DualBoundingBoxRequestDto boxes,
            @RequestParam(defaultValue = "20") int topN,
            @RequestParam(name = "includeJoined", defaultValue = "false") boolean includeJoined,
            @RequestParam(name = "includePast", defaultValue = "false") boolean includePast,
            HttpServletRequest request
    ) {
        // JWT에서 사용자 ID 추출
        Long userId = getCurrentUserId(request);

        // 1) 박스 교집합 기반 후보 조회 + AI 랭킹
        List<Long> ranked = recommendService.recommendByBoxes(userId, boxes, topN, includeJoined, includePast);

        // 2) 정렬된 ID → 상세 DTO (추천 순서 유지)
        var dtos = postService.getPostsByIds(ranked);

        // 3) rooms 래핑해서 반환
        return ResponseEntity.ok(new RoomsResponse(dtos));
    }

    /** 응답 래퍼: { "rooms": [ ... ] } */
    @lombok.Getter
    @lombok.AllArgsConstructor
    static class RoomsResponse {
        private List<PostResponseDto> rooms;
    }
    
    /**
     * 📝 기존 방식: 단일 좌표 기반 추천 (하위 호환성)
     */
//    @GetMapping("/recommend")
//    public ResponseEntity<List<PostResponseDto>> getRecommendedPosts(
//            @RequestParam double lat,
//            @RequestParam double lng,
//            @RequestParam double radiusMeters,
//            @RequestParam(defaultValue = "10") int topN,
//            HttpServletRequest request
//    ) {
//        Long userId = getCurrentUserId(request);
//
//        // 1) AI 추천으로 Post ID 목록 가져오기
//        List<Long> recommendedIds = recommendService.recommend(userId, lat, lng, radiusMeters, topN);
//
//        // 2) Post ID를 실제 Post 상세정보로 변환
//        List<PostResponseDto> result = postService.getPostsByIds(recommendedIds);
//
//        return ResponseEntity.ok(result);
//    }

    /**
     * 📝 새로 추가: 참여 가능한 활성 Post들 조회
     */
    @GetMapping("/available")
    public ResponseEntity<List<PostResponseDto>> getAvailablePosts() {
        List<PostResponseDto> result = postService.getAvailablePosts();
        return ResponseEntity.ok(result);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userId;
    }

}
