package com.unithon.tadadak.groups.repository;

import com.unithon.tadadak.groups.domain.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupsRepository extends JpaRepository<Groups, Long> {
    
    /**
     * Post ID로 해당하는 그룹 조회
     */
    @Query("SELECT g FROM Groups g WHERE g.post.postId = :postId")
    Optional<Groups> findByPostId(@Param("postId") Long postId);
    
    /**
     * 정원이 차지 않은 활성 그룹들 조회
     */
    @Query("SELECT g FROM Groups g WHERE g.status IN ('WAITING', 'IN_PROGRESS') AND g.currentMemberCount < g.maxMemberCount")
    List<Groups> findAvailableGroups();
    
    /**
     * 특정 Post들에 대한 그룹 정보 일괄 조회
     */
    @Query("SELECT g FROM Groups g WHERE g.post.postId IN :postIds")
    List<Groups> findByPostIdIn(@Param("postIds") List<Long> postIds);
    
    /**
     * Post 엔티티로 그룹 조회
     */
    Optional<Groups> findByPost(com.unithon.tadadak.post.domain.Post post);
    
    /**
     * 특정 사용자가 호스트인 그룹들 조회
     */
    @Query("SELECT g FROM Groups g WHERE g.post.host.userId = :hostId")
    List<Groups> findByHostId(@Param("hostId") Long hostId);
}
