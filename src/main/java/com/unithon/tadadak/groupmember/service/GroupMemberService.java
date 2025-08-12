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
import com.unithon.tadadak.post.domain.Post;
import com.unithon.tadadak.post.repository.PostRepository;
import com.unithon.tadadak.user.repository.UserRepository;
import com.unithon.tadadak.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMemberService {

    private final GroupMemberRepository repository;
    private final GroupsRepository groupsRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public GroupMemberResponse joinGroup(GroupMemberRequest request) {
        // 그룹과 사용자 엔티티 조회
        Groups group = groupsRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(group.getPost().getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 이미 참여했는지 확인
        if (repository.existsByGroupIdAndUserId(group.getGroupId(), user.getUserId())) {
            throw new CustomException(ErrorCode.DUPLICATE_JOIN);
        }

        // 🆕 그룹 참여 가능 여부 확인
        if (!group.canJoin()) {
            if (group.isFull()) {
                log.warn("그룹 {} 정원 초과: 현재 {}/{} 명",
                        group.getGroupId(), group.getCurrentMemberCount(), group.getMaxMemberCount());
                throw new CustomException(ErrorCode.GROUP_FULL);
            } else {
                log.warn("그룹 {} 참여 불가능한 상태: {}", group.getGroupId(), group.getStatus());
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }

        // 그룹 멤버 생성
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .isHost(request.isHost())
                .paymentStatus(request.getPaymentStatus())
                .build();

        GroupMember savedMember = repository.save(member);

        // 🆕 그룹 현재 인원수 증가
        group.incrementMemberCount();
        groupsRepository.save(group);

        // 🆕 인원수에 따라 1인당 예상 금액 재계산
        if (group.getCurrentMemberCount() > 0 && post.getEstimatedPrice() != null) {
            int updatedPerMemberPrice = post.getEstimatedPrice() / group.getCurrentMemberCount();
            post.setEstimatePricePerMember(updatedPerMemberPrice);
            postRepository.save(post);
            log.info("Post {} 1인당 예상 금액 갱신: {}", post.getPostId(), updatedPerMemberPrice);
        }

        log.info("사용자 {}가 그룹 {}에 참여 완료. 현재 인원: {}/{}",
                user.getUserId(), group.getGroupId(),
                group.getCurrentMemberCount(), group.getMaxMemberCount());

        return GroupMemberResponse.from(savedMember);
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        // 그룹 멤버 존재 여부 확인
        GroupMemberId id = new GroupMemberId(groupId, userId);
        GroupMember member = repository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 그룹/포스트 조회
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        Post post = postRepository.findById(group.getPost().getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 🆕 호스트는 그룹을 나갈 수 없음
        if (member.isHost()) {
            log.warn("호스트 {}가 그룹 {}에서 나가려고 시도", userId, groupId);
            throw new CustomException(ErrorCode.HOST_CANNOT_LEAVE);
        }

        // 그룹 멤버 삭제
        repository.deleteById(id);

        // 🆕 그룹 현재 인원수 감소 (0 이하 방지)
        if (group.getCurrentMemberCount() > 0) {
            group.decrementMemberCount();
        }
        groupsRepository.save(group);

        // 🆕 인원 감소에 따른 1인당 예상 금액 재계산
        recalcEstimatePerMember(group, post);

        log.info("사용자 {}가 그룹 {}에서 나감. 현재 인원: {}/{}",
                userId, groupId, group.getCurrentMemberCount(), group.getMaxMemberCount());
    }

    private void recalcEstimatePerMember(Groups group, Post post) {
        Integer total = post.getEstimatedPrice();
        int count = group.getCurrentMemberCount();

        if (total != null && count > 0) {
            int perMember = total / count;
            post.setEstimatePricePerMember(perMember);
        } else {
            post.setEstimatePricePerMember(null);
        }
        postRepository.save(post);
    }

    /**
     * 그룹 인원수에 따라 Post.estimatePricePerMember 재계산
     * - 총 예상 금액이 null이거나 인원 0명이면 null
     * - 그 외에는 (총액 / 현재인원)
     */

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


