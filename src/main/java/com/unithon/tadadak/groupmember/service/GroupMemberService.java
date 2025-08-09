package com.unithon.tadadak.groupmember.service;

import com.unithon.tadadak.global.exception.CustomException;
import com.unithon.tadadak.global.exception.ErrorCode;
import com.unithon.tadadak.groupmember.domain.GroupMember;
import com.unithon.tadadak.groupmember.domain.GroupMemberId;
import com.unithon.tadadak.groupmember.dto.GroupMemberRequest;
import com.unithon.tadadak.groupmember.dto.GroupMemberResponse;
import com.unithon.tadadak.groupmember.repository.GroupMemberRepository;
import com.unithon.tadadak.groups.repository.GroupsRepository;
import com.unithon.tadadak.groups.domain.Groups;
import com.unithon.tadadak.user.repository.UserRepository;
import com.unithon.tadadak.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMemberService {

    private final GroupMemberRepository repository;
    private final GroupsRepository groupsRepository;
    private final UserRepository userRepository;

    public GroupMemberResponse joinGroup(GroupMemberRequest request) {
        // 그룹과 사용자 엔티티 조회
        Groups group = groupsRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 이미 참여했는지 확인
        if (repository.existsByGroupIdAndUserId(group.getGroupId(), user.getUserId())) {
            throw new CustomException(ErrorCode.DUPLICATE_JOIN);
        }
        
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .isHost(request.isHost())
                .paymentStatus(request.getPaymentStatus())
                .build();
        
        return GroupMemberResponse.from(repository.save(member));
    }

    public void leaveGroup(Long groupId, Long userId) {
        // 복합키로 직접 삭제
        GroupMemberId id = new GroupMemberId(groupId, userId);
        if (!repository.existsById(id)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        repository.deleteById(id);
        
        log.info("User {} left group {}", userId, groupId);
    }

    /**
     * 📝 새로 추가: 특정 그룹의 모든 멤버 조회
     */
    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        List<GroupMember> members = repository.findByGroupId(groupId);
        log.info("Found {} members in group {}", members.size(), groupId);
        
        return members.stream()
                .map(GroupMemberResponse::from)
                .toList();
    }

    /**
     * 📝 새로 추가: 특정 사용자의 모든 참여 그룹 조회
     */
    public List<GroupMemberResponse> getUserGroupMemberships(Long userId) {
        List<GroupMember> memberships = repository.findByUserId(userId);
        log.info("User {} is member of {} groups", userId, memberships.size());
        
        return memberships.stream()
                .map(GroupMemberResponse::from)
                .toList();
    }

    /**
     * 📝 새로 추가: 사용자가 특정 그룹의 호스트인지 확인
     */
    public boolean isGroupHost(Long groupId, Long userId) {
        return repository.findByGroupIdAndUserId(groupId, userId)
                .map(GroupMember::isHost)
                .orElse(false);
    }

    /**
     * 📝 새로 추가: 그룹 멤버 상세 정보 조회
     */
    public GroupMemberResponse getGroupMember(Long groupId, Long userId) {
        GroupMemberId id = new GroupMemberId(groupId, userId);
        GroupMember member = repository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        
        return GroupMemberResponse.from(member);
    }
}


