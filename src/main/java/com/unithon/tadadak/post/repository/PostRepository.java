package com.unithon.tadadak.post.repository;

import com.unithon.tadadak.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = """
        SELECT 
            p.post_id       AS postId,
            p.estimated_price AS estimatedPrice,
            (6371000 * acos(
                cos(radians(:lat)) * cos(radians(sl.latitude)) *
                cos(radians(sl.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(sl.latitude))
            )) AS distanceM,
            u.trust_score   AS trustScore
        FROM post p
        JOIN location sl ON sl.location_id = p.start_location_id
        JOIN users u ON u.user_id = p.host_id
        LEFT JOIN ride_groups rg ON rg.post_id = p.post_id
        WHERE p.status = 'OPEN'
          AND p.departure_time > NOW()
          AND (rg.group_id IS NULL OR rg.current_member_count < rg.max_member_count)
          AND (6371000 * acos(
                cos(radians(:lat)) * cos(radians(sl.latitude)) *
                cos(radians(sl.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(sl.latitude))
            )) <= :radius
          AND NOT EXISTS (
            SELECT 1 FROM group_members gm 
            WHERE gm.group_id = rg.group_id AND gm.user_id = :userId
          )
        ORDER BY distanceM ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<NearbyPostRow> findNearbyWithHostTrust(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radiusMeters,
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    /**
     * 📝 성능 최적화: Post와 연관된 모든 엔티티를 한 번에 조회 (fetch join)
     */
    @Query("""
  select distinct p
  from Post p
  left join fetch p.host
  left join fetch p.startLocation
  left join fetch p.endLocation
  left join fetch p.groups g
  where p.postId in :ids
""")
    List<Post> findAllByIdWithDetails(@Param("ids") List<Long> ids);


    /**
     * 📝 성능 최적화: 단일 Post 상세 조회 (fetch join)
     */
    @Query("""
        SELECT p 
        FROM Post p
        LEFT JOIN FETCH p.host h
        LEFT JOIN FETCH p.startLocation sl
        LEFT JOIN FETCH p.endLocation el
        LEFT JOIN FETCH p.groups g
        WHERE p.postId = :postId
        """)
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);

    /**
     * 📝 성능 최적화: 교집합 조회 (fetch join)
     */
    @Query("""
        SELECT DISTINCT p
        FROM Post p
        LEFT JOIN FETCH p.host h
        LEFT JOIN FETCH p.startLocation sl
        LEFT JOIN FETCH p.endLocation el
        LEFT JOIN FETCH p.groups g
        WHERE sl.latitude  BETWEEN :depMinLat AND :depMaxLat
          AND sl.longitude BETWEEN :depMinLng AND :depMaxLng
          AND el.latitude  BETWEEN :destMinLat AND :destMaxLat
          AND el.longitude BETWEEN :destMinLng AND :destMaxLng
        """)
    List<Post> findAllInIntersectionWithDetails(
            @Param("depMinLat")  double depMinLat,
            @Param("depMaxLat")  double depMaxLat,
            @Param("depMinLng")  double depMinLng,
            @Param("depMaxLng")  double depMaxLng,
            @Param("destMinLat") double destMinLat,
            @Param("destMaxLat") double destMaxLat,
            @Param("destMinLng") double destMinLng,
            @Param("destMaxLng") double destMaxLng
    );

    /**
     * 📝 활성화된 Post들 조회 (정원 미달만)
     */
    @Query("""
        SELECT DISTINCT p 
        FROM Post p
        LEFT JOIN FETCH p.host h
        LEFT JOIN FETCH p.startLocation sl
        LEFT JOIN FETCH p.endLocation el
        LEFT JOIN FETCH p.groups g
        WHERE p.status = 'OPEN'
          AND p.departureTime > CURRENT_TIMESTAMP
          AND (g.groupId IS NULL OR g.currentMemberCount < g.maxMemberCount)
        ORDER BY p.departureTime ASC
        """)
    List<Post> findAvailablePostsWithDetails();
}

