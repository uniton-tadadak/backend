package com.unithon.tadadak.groups.service;

import com.unithon.tadadak.groups.domain.Groups;
import com.unithon.tadadak.groups.dto.GroupsRequest;
import com.unithon.tadadak.groups.dto.GroupsResponse;
import com.unithon.tadadak.groups.repository.GroupsRepository;
import com.unithon.tadadak.post.repository.PostRepository;
import com.unithon.tadadak.post.domain.Post;
import com.unithon.tadadak.global.exception.CustomException;
import com.unithon.tadadak.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupsService {

    private final GroupsRepository groupsRepository;
    private final PostRepository postRepository;

    public GroupsResponse getGroupByPostId(Long postId) {
        log.info("🔍 Post ID로 Group 조회 시도: {}", postId);

        try {
            List<Groups> groups = groupsRepository.findAllByPostId(postId);
            log.info("🔍 조회된 Group 개수: {}", groups.size());

            if (groups.isEmpty()) {
                throw new CustomException(ErrorCode.GROUP_NOT_FOUND);
            }

            // 첫 번째 Group 사용
            Groups firstGroup = groups.get(0);
            GroupsResponse result = GroupsResponse.from(firstGroup);

            log.info("✅ Group 조회 성공: {}", result);
            return result;
        } catch (Exception e) {
            log.error("❌ Group 조회 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    public GroupsResponse createGroup(GroupsRequest request) {
        // Post 엔티티 조회
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Groups group = Groups.builder()
                .post(post)  // 📝 변경: postId → post 엔티티
                .maxMemberCount(request.getMaxMemberCount())
                .currentMemberCount(request.getCurrentMemberCount())
                .status(request.getStatus())
                .build();

        Groups savedGroup = groupsRepository.save(group);
        log.info("Created group {} for post {}", savedGroup.getGroupId(), post.getPostId());
        
        return GroupsResponse.from(savedGroup);
    }

    public List<GroupsResponse> getAllGroups() {
        return groupsRepository.findAll().stream()
                .map(GroupsResponse::from)
                .collect(Collectors.toList());
    }

    public GroupsResponse getGroupById(Long id) {
        return groupsRepository.findById(id)
                .map(GroupsResponse::from)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
    }
}

