package com.unithon.tadadak.chatroom.api;

import com.unithon.tadadak.chatroom.service.ChatRoomService;
import com.unithon.tadadak.groupmember.service.GroupMemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomMembershipController {

    private final ChatRoomService chatRoomService;
    private final GroupMemberService groupMemberService;

    /**
     * 채팅방 입장
     * DB의 GroupMember 테이블에도 추가하고, Firestore 채팅방에도 멤버 추가
     */
    @PostMapping("/{postId}/join")
    public ResponseEntity<Map<String, String>> joinRoom(
            @PathVariable Long postId,
            HttpServletRequest request) {
        
        // JWT에서 사용자 정보 추출 (try 블록 밖에서 선언)
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "인증 정보를 찾을 수 없습니다."));
        }
        
        try {
            // 1) 먼저 DB에서 그룹 멤버 추가 (비즈니스 로직 검증 포함)
            // GroupMemberService를 통해 정원, 상태 등을 체크
            // 실제 구현은 GroupMemberService에서 해야 함
            
            // 2) Firestore 채팅방에 멤버 추가
            chatRoomService.addMemberToRoom(postId.toString(), userId.toString());
            
            log.info("사용자 {}가 방 {}에 입장했습니다.", userId, postId);
            
            return ResponseEntity.ok(Map.of(
                "message", "채팅방 입장 성공",
                "postId", postId.toString(),
                "userId", userId.toString()
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("채팅방 입장 실패 - 잘못된 요청: postId={}, userId={}, error={}", postId, userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
                    
        } catch (Exception e) {
            log.error("채팅방 입장 중 오류 발생: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "채팅방 입장 실패"));
        }
    }

    /**
     * 채팅방 나가기
     * DB의 GroupMember에서도 제거하고, Firestore 채팅방에서도 멤버 제거
     */
    @PostMapping("/{postId}/leave")
    public ResponseEntity<Map<String, String>> leaveRoom(
            @PathVariable Long postId,
            HttpServletRequest request) {
        
        // JWT에서 사용자 정보 추출 (try 블록 밖에서 선언)
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "인증 정보를 찾을 수 없습니다."));
        }
        
        try {
            // 1) DB에서 그룹 멤버 제거
            // GroupMemberService를 통해 제거 (호스트인 경우 처리 로직 포함)
            
            // 2) Firestore 채팅방에서 멤버 제거
            chatRoomService.removeMemberFromRoom(postId.toString(), userId.toString());
            
            log.info("사용자 {}가 방 {}에서 나갔습니다.", userId, postId);
            
            return ResponseEntity.ok(Map.of(
                "message", "채팅방 나가기 성공",
                "postId", postId.toString(),
                "userId", userId.toString()
            ));
            
        } catch (Exception e) {
            log.error("채팅방 나가기 중 오류 발생: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "채팅방 나가기 실패"));
        }
    }

    /**
     * 멤버 강퇴 (호스트만 가능)
     */
    @PostMapping("/{postId}/kick")
    public ResponseEntity<Map<String, String>> kickMember(
            @PathVariable Long postId,
            @RequestParam Long targetUserId,
            HttpServletRequest request) {
        
        // JWT에서 호스트 정보 추출 (try 블록 밖에서 선언)
        Long hostUserId = (Long) request.getAttribute("userId");
        if (hostUserId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "인증 정보를 찾을 수 없습니다."));
        }
        
        try {
            // 1) DB에서 권한 확인 및 멤버 제거
            // GroupMemberService를 통해 호스트 권한 확인 후 제거
            
            // 2) Firestore 채팅방에서 멤버 강퇴
            chatRoomService.kickMemberFromRoom(postId.toString(), hostUserId.toString(), targetUserId.toString());
            
            log.info("호스트 {}가 사용자 {}를 방 {}에서 강퇴했습니다.", hostUserId, targetUserId, postId);
            
            return ResponseEntity.ok(Map.of(
                "message", "멤버 강퇴 성공",
                "postId", postId.toString(),
                "hostUserId", hostUserId.toString(),
                "targetUserId", targetUserId.toString()
            ));
            
        } catch (IllegalStateException e) {
            log.warn("강퇴 권한 없음: hostUserId={}, targetUserId={}, postId={}", hostUserId, targetUserId, postId);
            return ResponseEntity.status(403)
                    .body(Map.of("error", e.getMessage()));
                    
        } catch (IllegalArgumentException e) {
            log.warn("강퇴 요청 오류: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
                    
        } catch (Exception e) {
            log.error("멤버 강퇴 중 오류 발생: postId={}, hostUserId={}, targetUserId={}", postId, hostUserId, targetUserId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "멤버 강퇴 실패"));
        }
    }

    /**
     * 채팅방 정보 조회
     */
    @GetMapping("/{postId}/info")
    public ResponseEntity<Map<String, Object>> getRoomInfo(@PathVariable Long postId) {
        try {
            // Firestore에서 채팅방 정보를 가져오는 로직은 ChatRoomService에 추가 필요
            // 현재는 기본 응답만 반환
            
            return ResponseEntity.ok(Map.of(
                "postId", postId.toString(),
                "status", "OPEN",
                "message", "채팅방 정보 조회 성공"
            ));
            
        } catch (Exception e) {
            log.error("채팅방 정보 조회 실패: postId={}", postId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "채팅방 정보 조회 실패"));
        }
    }
} 