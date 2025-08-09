package com.unithon.tadadak.post.api;

import com.unithon.tadadak.post.domain.Post;
import com.unithon.tadadak.post.dto.DualBoundingBoxRequestDto;
import com.unithon.tadadak.post.dto.PostRequestDto;
import com.unithon.tadadak.post.dto.PostResponseDto;
import com.unithon.tadadak.post.service.PostService;
import com.unithon.tadadak.recommend.service.RecommendService;
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
    public ResponseEntity<PostResponseDto> create(@RequestBody PostRequestDto dto) {
        Post post = postService.createPost(dto);
        return ResponseEntity.ok(PostResponseDto.fromEntity(post));
    }

    /**
     * 📝 새로 추가: Post와 Location을 함께 생성 (편의 API)
     */
    @PostMapping("/with-locations")
    public ResponseEntity<PostResponseDto> createWithLocations(
            @RequestBody PostService.CreatePostWithLocationsRequest request) {
        PostResponseDto result = postService.createPostWithLocations(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/intersection")
    public ResponseEntity<List<PostResponseDto>> getIntersectionPosts(
            @RequestBody DualBoundingBoxRequestDto boxes) {
        List<PostResponseDto> result = postService.getPostsInIntersection(boxes);
        return ResponseEntity.ok(result);
    }

    /**
     * 📝 새로운 방식: 출발지+도착지 기반 추천 (권장)
     */
    @GetMapping("/recommend/route")
    public ResponseEntity<List<PostResponseDto>> getRecommendedPostsByRoute(
            @RequestParam double depLat,   // 출발지 위도
            @RequestParam double depLng,   // 출발지 경도
            @RequestParam double destLat,  // 도착지 위도
            @RequestParam double destLng,  // 도착지 경도
            @RequestParam(defaultValue = "1000") double radius, // 허용 반경(미터)
            @RequestParam(defaultValue = "10") int topN
    ) {
        Long userId = currentUserId();
        
        // 1) AI 추천으로 Post ID 목록 가져오기 (route-based)
        List<Long> recommendedIds = recommendService.recommendByRoute(userId, depLat, depLng, destLat, destLng, radius, topN);
        
        // 2) Post ID를 실제 Post 상세정보로 변환
        List<PostResponseDto> result = postService.getPostsByIds(recommendedIds);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 📝 기존 방식: 단일 좌표 기반 추천 (하위 호환성)
     */
    @GetMapping("/recommend")
    public ResponseEntity<List<PostResponseDto>> getRecommendedPosts(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radiusMeters,
            @RequestParam(defaultValue = "10") int topN
    ) {
        Long userId = currentUserId();
        
        // 1) AI 추천으로 Post ID 목록 가져오기
        List<Long> recommendedIds = recommendService.recommend(userId, lat, lng, radiusMeters, topN);
        
        // 2) Post ID를 실제 Post 상세정보로 변환
        List<PostResponseDto> result = postService.getPostsByIds(recommendedIds);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 📝 새로 추가: 참여 가능한 활성 Post들 조회
     */
    @GetMapping("/available")
    public ResponseEntity<List<PostResponseDto>> getAvailablePosts() {
        List<PostResponseDto> result = postService.getAvailablePosts();
        return ResponseEntity.ok(result);
    }

    private Long currentUserId() {
        // JWT/세션 등에서 실제 유저 ID 가져오도록 바꾸기
        return 1L;
    }

}
