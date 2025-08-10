package com.unithon.tadadak.chatroom.service;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class ChatRoomService {

    private final Optional<Firestore> firestore;

    @Autowired
    public ChatRoomService(Optional<Firestore> firestore) {
        this.firestore = firestore;
    }

    /**
     * Post 생성 시 Firestore에 채팅방 생성 + 호스트 멤버 추가
     */
    public void createRoomForPost(Long postId, Long hostUserId) {
        if (firestore.isEmpty()) {
            log.warn("Firebase가 비활성화되어 있습니다. 채팅방 생성을 건너뜁니다.");
            return;
        }

        try {
            var now = FieldValue.serverTimestamp();
            var roomRef = firestore.get().collection("rooms").document(postId.toString());
            
            // 채팅방 메타데이터 생성
            roomRef.set(Map.of(
                "createdBy", hostUserId.toString(),
                "createdAt", now,
                "status", "OPEN",
                "memberCount", 1
            )).get();

            // 호스트를 첫 번째 멤버로 추가
            var memberRef = roomRef.collection("members").document(hostUserId.toString());
            memberRef.set(Map.of(
                "role", "host",
                "joinedAt", now
            )).get();

            log.info("Post {}에 대한 Firestore 채팅방 생성 완료. 호스트: {}", postId, hostUserId);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Post {}에 대한 채팅방 생성 실패", postId, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Post {}에 대한 채팅방 생성 중 예상치 못한 오류", postId, e);
        }
    }

    /**
     * 채팅방 멤버 추가
     */
    public void addMemberToRoom(String postId, String userId) {
        if (firestore.isEmpty()) {
            log.warn("Firebase가 비활성화되어 있습니다.");
            return;
        }

        try {
            var roomRef = firestore.get().collection("rooms").document(postId);
            var room = roomRef.get().get();
            
            if (!room.exists()) {
                throw new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + postId);
            }

            var memberRef = roomRef.collection("members").document(userId);
            var memberDoc = memberRef.get().get();
            
            if (!memberDoc.exists()) {
                var now = FieldValue.serverTimestamp();
                memberRef.set(Map.of(
                    "role", "member",
                    "joinedAt", now
                )).get();
                
                // 멤버 수 증가
                roomRef.update("memberCount", FieldValue.increment(1)).get();
                
                log.info("사용자 {}가 채팅방 {}에 입장했습니다.", userId, postId);
            } else {
                log.info("사용자 {}는 이미 채팅방 {}에 참여 중입니다.", userId, postId);
            }
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("채팅방 {} 멤버 추가 실패: {}", postId, userId, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("채팅방 {} 멤버 추가 중 예상치 못한 오류", postId, e);
        }
    }

    /**
     * 채팅방 멤버 제거
     */
    public void removeMemberFromRoom(String postId, String userId) {
        if (firestore.isEmpty()) {
            log.warn("Firebase가 비활성화되어 있습니다.");
            return;
        }

        try {
            var roomRef = firestore.get().collection("rooms").document(postId);
            var memberRef = roomRef.collection("members").document(userId);
            var memberDoc = memberRef.get().get();
            
            if (memberDoc.exists()) {
                memberRef.delete().get();
                roomRef.update("memberCount", FieldValue.increment(-1)).get();
                
                log.info("사용자 {}가 채팅방 {}에서 나갔습니다.", userId, postId);
            }
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("채팅방 {} 멤버 제거 실패: {}", postId, userId, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("채팅방 {} 멤버 제거 중 예상치 못한 오류", postId, e);
        }
    }

    /**
     * 멤버 강퇴 (호스트만 가능)
     */
    public void kickMemberFromRoom(String postId, String hostUserId, String targetUserId) {
        if (firestore.isEmpty()) {
            log.warn("Firebase가 비활성화되어 있습니다.");
            return;
        }

        try {
            var roomRef = firestore.get().collection("rooms").document(postId);
            var roomDoc = roomRef.get().get();
            
            if (!roomDoc.exists()) {
                throw new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + postId);
            }
            
            // 호스트 권한 확인
            var createdBy = roomDoc.getString("createdBy");
            if (!hostUserId.equals(createdBy)) {
                throw new IllegalStateException("호스트만 멤버를 강퇴할 수 있습니다.");
            }
            
            // 자기 자신 강퇴 방지
            if (hostUserId.equals(targetUserId)) {
                throw new IllegalArgumentException("호스트는 자기 자신을 강퇴할 수 없습니다.");
            }
            
            var memberRef = roomRef.collection("members").document(targetUserId);
            var memberDoc = memberRef.get().get();
            
            if (memberDoc.exists()) {
                memberRef.delete().get();
                roomRef.update("memberCount", FieldValue.increment(-1)).get();
                
                log.info("호스트 {}가 사용자 {}를 채팅방 {}에서 강퇴했습니다.", hostUserId, targetUserId, postId);
            }
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("채팅방 {} 멤버 강퇴 실패", postId, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("채팅방 {} 멤버 강퇴 중 예상치 못한 오류", postId, e);
        }
    }

    /**
     * 채팅방 상태 변경 (Post 종료 시)
     */
    public void closeRoom(String postId) {
        if (firestore.isEmpty()) {
            log.warn("Firebase가 비활성화되어 있습니다.");
            return;
        }

        try {
            var roomRef = firestore.get().collection("rooms").document(postId);
            roomRef.update("status", "CLOSED").get();
            
            log.info("채팅방 {}가 닫혔습니다.", postId);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("채팅방 {} 닫기 실패", postId, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("채팅방 {} 닫기 중 예상치 못한 오류", postId, e);
        }
    }
} 