package com.unithon.tadadak.groupmember.repository;

import com.unithon.tadadak.groupmember.domain.GroupMember;
import com.unithon.tadadak.groupmember.domain.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {
    
    /**
     * 특정 사용자가 참여한 모든 그룹 ID 목록 조회
     */
    @Query("SELECT gm.group.groupId FROM GroupMember gm WHERE gm.user.userId = :userId")
    List<Long> findGroupIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 그룹의 현재 멤버 수 조회
     */
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.groupId = :groupId")
    int countByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 사용자가 특정 그룹에 참여했는지 확인
     */
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.groupId = :groupId AND gm.user.userId = :userId")
    boolean existsByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * 그룹과 사용자로 GroupMember 조회
     */
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.groupId = :groupId AND gm.user.userId = :userId")
    Optional<GroupMember> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * 특정 그룹의 모든 멤버 조회
     */
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.groupId = :groupId")
    List<GroupMember> findByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 특정 사용자의 모든 참여 내역 조회
     */
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.userId = :userId")
    List<GroupMember> findByUserId(@Param("userId") Long userId);
}
