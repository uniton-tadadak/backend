package com.unithon.tadadak.groupmember.api;

import com.unithon.tadadak.groupmember.domain.GroupMember;
import com.unithon.tadadak.groupmember.dto.GroupMemberRequest;
import com.unithon.tadadak.groupmember.dto.GroupMemberResponse;
import com.unithon.tadadak.groupmember.service.GroupMemberService;
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

    @PostMapping
    public GroupMemberResponse joinGroup(@RequestBody GroupMemberRequest request, HttpServletRequest httpRequest) {
        // JWT에서 사용자 정보 추출하여 요청에 설정
        Long userId = getCurrentUserId(httpRequest);
        request.setUserId(userId);  // 🆕 JWT에서 추출한 userId로 강제 설정
        
        return service.joinGroup(request);
    }

    @DeleteMapping("/{groupId}")
    public void leaveGroup(@PathVariable Long groupId, HttpServletRequest request) {
        // JWT에서 사용자 정보 추출하여 자신만 나갈 수 있도록
        Long userId = getCurrentUserId(request);
        service.leaveGroup(groupId, userId);
    }

    // 관리자용 멤버 제거 (필요시)
    @DeleteMapping("/{groupId}/{userId}")
    public void removeGroupMember(@PathVariable Long groupId, @PathVariable Long userId, HttpServletRequest request) {
        // JWT에서 호스트 정보 추출하여 권한 체크
        Long hostUserId = getCurrentUserId(request);
        // 호스트 권한 체크 로직 추가 필요
        service.leaveGroup(groupId, userId);
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
}


