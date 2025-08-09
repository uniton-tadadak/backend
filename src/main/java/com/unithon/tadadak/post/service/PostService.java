package com.unithon.tadadak.post.service;

import com.unithon.tadadak.location.domain.Location;
import com.unithon.tadadak.location.repository.LocationRepository;
import com.unithon.tadadak.location.service.LocationService;
import com.unithon.tadadak.post.domain.Post;
import com.unithon.tadadak.post.dto.DualBoundingBoxRequestDto;
import com.unithon.tadadak.post.dto.PostRequestDto;
import com.unithon.tadadak.post.dto.PostResponseDto;
import com.unithon.tadadak.post.repository.PostRepository;
import com.unithon.tadadak.user.repository.UserRepository;
import com.unithon.tadadak.user.domain.User;
import com.unithon.tadadak.groups.domain.Groups;
import com.unithon.tadadak.groups.repository.GroupsRepository;
import com.unithon.tadadak.groupmember.domain.GroupMember;
import com.unithon.tadadak.groupmember.domain.PaymentStatus;
import com.unithon.tadadak.groupmember.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final GroupsRepository groupsRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public Post createPost(PostRequestDto dto) {
        // 1) Post 생성
        User host = userRepository.findById(dto.getHostId())
                .orElseThrow(() -> new IllegalArgumentException("Host user not found"));
        
        Post post = postRepository.save(
                Post.builder()
                        .host(host)
                        .startLocation(locationRepository.findById(dto.getStartLocationId()).orElseThrow())
                        .endLocation(locationRepository.findById(dto.getEndLocationId()).orElseThrow())
                        .desiredMembers(dto.getDesiredMembers())
                        .estimatedPrice(dto.getEstimatedPrice())
                        .departureTime(dto.getDepartureTime())
                        .status("OPEN")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        
        // 2) Groups 자동 생성
        Groups group = groupsRepository.save(
                Groups.builder()
                        .post(post)
                        .maxMemberCount(dto.getDesiredMembers())
                        .currentMemberCount(1)  // 호스트가 첫 번째 멤버
                        .status("WAITING")
                        .build()
        );
        
        // 3) GroupMember 자동 생성 (호스트를 첫 번째 멤버로)
        GroupMember hostMember = groupMemberRepository.save(
                GroupMember.builder()
                        .group(group)
                        .user(host)
                        .isHost(true)
                        .paymentStatus(PaymentStatus.WAIT)
                        .build()
        );
        
        // 4) Post 엔티티에 생성된 group 추가 (JPA 연관관계 동기화)
        post.getGroups().add(group);
        
        log.info("Post {} 생성 완료 → Groups {} 생성 → Host {} 자동 참여", 
                post.getPostId(), group.getGroupId(), host.getUserId());
        
        return post;
    }

    public List<PostResponseDto> getPostsInIntersection(DualBoundingBoxRequestDto boxes) {
        var b1 = boxes.getDepartureBox();
        var b2 = boxes.getDestinationBox();
        
        // 📝 변경: fetch join 쿼리 사용으로 N+1 문제 방지
        List<Post> list = postRepository.findAllInIntersectionWithDetails(
                b1.getMinLat(), b1.getMaxLat(), b1.getMinLng(), b1.getMaxLng(),
                b2.getMinLat(), b2.getMaxLat(), b2.getMinLng(), b2.getMaxLng()
        );
        
        log.info("Found {} posts in intersection", list.size());
        return list.stream().map(PostResponseDto::fromEntity).toList();
    }

    /**
     * 추천된 Post ID 목록을 받아서 실제 Post 상세정보로 변환
     * (추천 순서 유지)
     */
    public List<PostResponseDto> getPostsByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        
        // 📝 변경: fetch join 쿼리 사용으로 N+1 문제 방지
        List<Post> posts = postRepository.findAllByIdWithDetails(postIds);
        
        // ID 순서 유지 (추천 순서대로)
        List<PostResponseDto> result = postIds.stream()
                .map(id -> posts.stream()
                        .filter(post -> post.getPostId().equals(id))
                        .findFirst()
                        .map(PostResponseDto::fromEntity)
                        .orElse(null))
                .filter(dto -> dto != null)
                .toList();
        
        log.info("Converted {} post IDs to DTOs with details", result.size());
        return result;
    }

    /**
     * Post ID로 단일 조회
     */
    public PostResponseDto getPostById(Long postId) {
        // 📝 변경: fetch join 쿼리 사용으로 N+1 문제 방지
        return postRepository.findByIdWithDetails(postId)
                .map(dto -> {
                    log.info("Found post {} with details", postId);
                    return PostResponseDto.fromEntity(dto);
                })
                .orElse(null);
    }

    /**
     * 📝 새로 추가: 참여 가능한 활성 Post들 조회
     */
    public List<PostResponseDto> getAvailablePosts() {
        List<Post> posts = postRepository.findAvailablePostsWithDetails();
        log.info("Found {} available posts", posts.size());
        return posts.stream()
                .map(PostResponseDto::fromEntity)
                .toList();
    }

    /**
     * 📝 새로 추가: Post와 Location을 함께 생성하는 편의 메서드
     */
    @Transactional
    public PostResponseDto createPostWithLocations(CreatePostWithLocationsRequest request) {
        // 1) Host 사용자 조회
        User host = userRepository.findById(request.getHostId())
                .orElseThrow(() -> new IllegalArgumentException("Host user not found"));
        
        // 2) 먼저 Location들을 생성 (postId 없이)
        Location startLocation = locationRepository.save(Location.builder()
                .latitude(request.getStartLatitude())
                .longitude(request.getStartLongitude())
                .user(null)
                .postId(null)  // 나중에 업데이트
                .createdAt(LocalDateTime.now())
                .build());
        
        Location endLocation = locationRepository.save(Location.builder()
                .latitude(request.getEndLatitude())
                .longitude(request.getEndLongitude())
                .user(null)
                .postId(null)  // 나중에 업데이트
                .createdAt(LocalDateTime.now())
                .build());
        
        // 3) Post 생성 (Location ID들 포함)
        Post post = postRepository.save(
                Post.builder()
                        .host(host)
                        .startLocation(startLocation)  // Location 엔티티 설정
                        .endLocation(endLocation)      // Location 엔티티 설정
                        .desiredMembers(request.getDesiredMembers())
                        .estimatedPrice(request.getEstimatedPrice())
                        .departureTime(request.getDepartureTime())
                        .status("OPEN")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        
        // 4) Location들의 postId는 업데이트하지 않음 
        // (Post가 이미 start_location_id, end_location_id로 참조하고 있음)
        
        // 6) Groups 자동 생성
        Groups group = groupsRepository.save(
                Groups.builder()
                        .post(post)
                        .maxMemberCount(request.getDesiredMembers())
                        .currentMemberCount(1)  // 호스트가 첫 번째 멤버
                        .status("WAITING")
                        .build()
        );
        
        // 7) GroupMember 자동 생성 (호스트를 첫 번째 멤버로)
        GroupMember hostMember = groupMemberRepository.save(
                GroupMember.builder()
                        .group(group)
                        .user(host)
                        .isHost(true)
                        .paymentStatus(PaymentStatus.WAIT)
                        .build()
        );
        
        // 8) Post 엔티티에 생성된 group 추가 (JPA 연관관계 동기화)
        post.getGroups().add(group);
        
        log.info("Created post {} with new locations (start: {}, end: {}) → Groups {} → Host {} auto-joined", 
                post.getPostId(), startLocation.getLocationId(), endLocation.getLocationId(),
                group.getGroupId(), host.getUserId());
        
        return PostResponseDto.fromEntity(post);
    }

    // 📝 새로운 요청 DTO 추가
    @lombok.Builder
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreatePostWithLocationsRequest {
        private Long hostId;
        private Double startLatitude;
        private Double startLongitude;
        private Double endLatitude;
        private Double endLongitude;
        private Integer desiredMembers;
        private Integer estimatedPrice;
        private LocalDateTime departureTime;
    }

}

