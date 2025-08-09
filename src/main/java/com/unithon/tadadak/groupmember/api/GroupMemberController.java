package com.unithon.tadadak.groupmember.api;

import com.unithon.tadadak.groupmember.domain.GroupMember;
import com.unithon.tadadak.groupmember.dto.GroupMemberRequest;
import com.unithon.tadadak.groupmember.dto.GroupMemberResponse;
import com.unithon.tadadak.groupmember.service.GroupMemberService;
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
    public GroupMemberResponse joinGroup(@RequestBody GroupMemberRequest request) {
        return service.joinGroup(request);
    }

    @DeleteMapping("/{groupId}/{userId}")
    public void leaveGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        service.leaveGroup(groupId, userId);
    }

    /**
     * 📝 새로 추가: 특정 그룹의 모든 멤버 조회
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMemberResponse> members = service.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    /**
     * 📝 새로 추가: 특정 사용자의 모든 참여 그룹 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<GroupMemberResponse>> getUserGroupMemberships(@PathVariable Long userId) {
        List<GroupMemberResponse> memberships = service.getUserGroupMemberships(userId);
        return ResponseEntity.ok(memberships);
    }

    /**
     * 📝 새로 추가: 사용자가 특정 그룹의 호스트인지 확인
     */
    @GetMapping("/{groupId}/{userId}/is-host")
    public ResponseEntity<Boolean> isGroupHost(@PathVariable Long groupId, @PathVariable Long userId) {
        boolean isHost = service.isGroupHost(groupId, userId);
        return ResponseEntity.ok(isHost);
    }

    /**
     * 📝 새로 추가: 그룹 멤버 상세 정보 조회
     */
    @GetMapping("/{groupId}/{userId}")
    public ResponseEntity<GroupMemberResponse> getGroupMember(
            @PathVariable Long groupId, 
            @PathVariable Long userId) {
        GroupMemberResponse member = service.getGroupMember(groupId, userId);
        return ResponseEntity.ok(member);
    }
}


