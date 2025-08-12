package com.unithon.tadadak.groupmember.api;

import com.unithon.tadadak.global.exception.CustomException;
import com.unithon.tadadak.global.exception.ErrorCode;
import com.unithon.tadadak.groupmember.domain.GroupMember;
import com.unithon.tadadak.groupmember.dto.GroupChangeResponse;
import com.unithon.tadadak.groupmember.dto.GroupMemberRequest;
import com.unithon.tadadak.groupmember.dto.GroupMemberResponse;
import com.unithon.tadadak.groupmember.service.GroupMemberService;
import com.unithon.tadadak.groups.domain.Groups;
import com.unithon.tadadak.groups.repository.GroupsRepository;
import com.unithon.tadadak.post.domain.Post;
import com.unithon.tadadak.post.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-members")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberService service;
    private final GroupsRepository groupsRepository;   // 🆕 추가
    private final PostRepository postRepository;       // 🆕 추가

    @PostMapping
    public ResponseEntity<GroupChangeResponse> joinGroup(
            @RequestBody GroupMemberRequest request,
            HttpServletRequest httpRequest
    ) {
        // JWT에서 사용자 정보 추출하여 요청에 설정
        Long userId = getCurrentUserId(httpRequest);
        request.setUserId(userId);

        // 서비스 내부에서: 인원 +1, perMember 재계산
        GroupMemberResponse res = service.joinGroup(request);

        // 갱신된 그룹/포스트 재조회
        Groups group = groupsRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        Post post = postRepository.findById(group.getPost().getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        GroupChangeResponse body = GroupChangeResponse.builder()
                .postId(post.getPostId())
                .groupId(group.getGroupId())
                .currentMembers(group.getCurrentMemberCount())
                .estimatedPrice(post.getEstimatedPrice())
                .estimatePricePerMember(post.getEstimatePricePerMember())
                .build();

        return ResponseEntity.ok(body);
    }

    // 관리자/호스트용 멤버 제거 (필요시)
    @DeleteMapping("/{groupId}/{userId}")
    public ResponseEntity<GroupChangeResponse> removeGroupMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        // JWT에서 호스트 정보 추출하여 권한 체크(필요 시 추가)
        Long hostUserId = getCurrentUserId(request);
        // TODO: hostUserId가 해당 그룹의 호스트인지 검증 로직 추가

        // 서비스 내부에서: 인원 -1, perMember 재계산
        service.leaveGroup(groupId, userId);

        // 갱신된 그룹/포스트 재조회
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        Post post = postRepository.findById(group.getPost().getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        GroupChangeResponse body = GroupChangeResponse.builder()
                .postId(post.getPostId())
                .groupId(group.getGroupId())
                .currentMembers(group.getCurrentMemberCount())
                .estimatedPrice(post.getEstimatedPrice())
                .estimatePricePerMember(post.getEstimatePricePerMember())
                .build();

        return ResponseEntity.ok(body);
    }



    /**
     * 📝 내가 참여한 모든 그룹 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<GroupMemberResponse>> getMyGroupMemberships(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<GroupMemberResponse> memberships = service.getUserGroupMemberships(userId);
        return ResponseEntity.ok(memberships);
    }

    /**
     * 📝 관리자용: 특정 사용자의 모든 참여 그룹 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<GroupMemberResponse>> getUserGroupMemberships(@PathVariable Long userId, HttpServletRequest request) {
        // JWT에서 요청자 정보 추출 (권한 체크용)
        Long requesterId = getCurrentUserId(request);
        List<GroupMemberResponse> memberships = service.getUserGroupMemberships(userId);
        return ResponseEntity.ok(memberships);
    }

    /**
     * 📝 특정 그룹의 모든 멤버 조회
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable Long groupId, HttpServletRequest request) {
        // JWT에서 사용자 정보 추출 (그룹 멤버인지 확인 등)
        Long userId = getCurrentUserId(request);
        List<GroupMemberResponse> members = service.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    /**
     * 📝 내가 특정 그룹의 호스트인지 확인
     */
    @GetMapping("/{groupId}/is-host")
    public ResponseEntity<Boolean> isGroupHost(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean isHost = service.isGroupHost(groupId, userId);
        return ResponseEntity.ok(isHost);
    }

    /**
     * 📝 관리자용: 사용자가 특정 그룹의 호스트인지 확인
     */
    @GetMapping("/{groupId}/{userId}/is-host")
    public ResponseEntity<Boolean> checkUserIsGroupHost(@PathVariable Long groupId, @PathVariable Long userId, HttpServletRequest request) {
        // JWT에서 요청자 정보 추출 (권한 체크용)
        Long requesterId = getCurrentUserId(request);
        boolean isHost = service.isGroupHost(groupId, userId);
        return ResponseEntity.ok(isHost);
    }

    /**
     * 📝 내 그룹 멤버 정보 조회
     */
    @GetMapping("/{groupId}/me")
    public ResponseEntity<GroupMemberResponse> getMyGroupMember(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        GroupMemberResponse member = service.getGroupMember(groupId, userId);
        return ResponseEntity.ok(member);
    }

    /**
     * 📝 관리자용: 그룹 멤버 상세 정보 조회
     */
    @GetMapping("/{groupId}/{userId}")
    public ResponseEntity<GroupMemberResponse> getGroupMember(
            @PathVariable Long groupId, 
            @PathVariable Long userId,
            HttpServletRequest request) {
        // JWT에서 요청자 정보 추출 (권한 체크용)
        Long requesterId = getCurrentUserId(request);
        GroupMemberResponse member = service.getGroupMember(groupId, userId);
        return ResponseEntity.ok(member);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userId;
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<String> leaveGroup(@PathVariable Long groupId, HttpServletRequest request) {
        // JWT에서 사용자 정보 추출하여 자신만 나갈 수 있도록
        Long userId = getCurrentUserId(request);
        service.leaveGroup(groupId, userId);
        return ResponseEntity.ok("그룹에서 성공적으로 나갔습니다.");
    }

    /**
     * 📝 그룹 나가기 (별칭 - 더 명확한 URL)
     */
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<String> leaveGroupAlternative(@PathVariable Long groupId, HttpServletRequest request) {
        // JWT에서 사용자 정보 추출하여 자신만 나갈 수 있도록
        Long userId = getCurrentUserId(request);
        service.leaveGroup(groupId, userId);
        return ResponseEntity.ok("그룹에서 성공적으로 나갔습니다.");
    }

}


