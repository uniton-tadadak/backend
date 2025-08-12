package com.unithon.tadadak.groups.api;

import com.unithon.tadadak.groups.domain.Groups;
import com.unithon.tadadak.groups.dto.GroupsRequest;
import com.unithon.tadadak.groups.dto.GroupsResponse;
import com.unithon.tadadak.groups.service.GroupsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupsController {

    private final GroupsService groupsService;

    @PostMapping
    public GroupsResponse createGroup(@RequestBody GroupsRequest request, HttpServletRequest httpRequest) {
        // JWT에서 사용자 정보 추출
        Long userId = getCurrentUserId(httpRequest);
        // 그룹 생성 권한 체크 등 필요 시 추가
        return groupsService.createGroup(request);
    }

    @GetMapping
    public List<GroupsResponse> getAllGroups(HttpServletRequest request) {
        // JWT에서 사용자 정보 추출
        Long userId = getCurrentUserId(request);
        // 모든 그룹 조회 (필요 시 사용자별 필터링 추가)
        return groupsService.getAllGroups();
    }

    @GetMapping("/{groupId}")
    public GroupsResponse getGroup(@PathVariable Long groupId, HttpServletRequest request) {
        // JWT에서 사용자 정보 추출
        Long userId = getCurrentUserId(request);
        // 그룹 접근 권한 체크 등 필요 시 추가
        return groupsService.getGroupById(groupId);
    }

    /**
     * 내가 참여한 그룹들 조회
     */
    @GetMapping("/my")
    public List<GroupsResponse> getMyGroups(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        // GroupsService에 사용자별 그룹 조회 메서드 추가 필요
        return groupsService.getAllGroups(); // 임시
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userId;
    }

}