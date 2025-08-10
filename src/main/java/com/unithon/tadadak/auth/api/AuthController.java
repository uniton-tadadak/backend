package com.unithon.tadadak.auth.api;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.unithon.tadadak.auth.dto.LoginRequest;
import com.unithon.tadadak.auth.dto.LoginResponse;
import com.unithon.tadadak.auth.dto.RegisterRequest;
import com.unithon.tadadak.auth.util.JwtUtil;
import com.unithon.tadadak.user.domain.User;
import com.unithon.tadadak.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입은 UserController에서 처리 (/api/users 또는 /api/users/signup)
     * 여기서는 로그인 전용
     */

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // 사용자 조회
            User user = userRepository.findByUsername(request.getUsername())
                    .orElse(null);

            // 사용자 존재 여부 및 패스워드 확인
            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(LoginResponse.builder()
                                .message("사용자명 또는 비밀번호가 올바르지 않습니다.")
                                .build());
            }

            // JWT 토큰 생성
            String accessToken = jwtUtil.generateToken(user.getUsername(), user.getUserId());

            // Firebase 커스텀 토큰 생성 (Firebase 활성화된 경우에만)
            String firebaseToken = null;
            try {
                if (FirebaseApp.getApps().isEmpty()) {
                    log.info("Firebase가 비활성화되어 있습니다. Firebase 토큰을 생성하지 않습니다.");
                } else {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("appUserId", user.getUserId().toString());
                    firebaseToken = FirebaseAuth.getInstance()
                            .createCustomToken(user.getUserId().toString(), claims);
                }
            } catch (Exception e) {
                log.warn("Firebase 토큰 생성 실패 (로그인): {}", e.getMessage());
            }

            log.info("로그인 성공: username={}, userId={}", user.getUsername(), user.getUserId());

            return ResponseEntity.ok(LoginResponse.builder()
                    .accessToken(accessToken)
                    .firebaseToken(firebaseToken)
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .message("로그인 성공")
                    .build());

        } catch (Exception e) {
            log.error("로그인 실패", e);
            return ResponseEntity.internalServerError()
                    .body(LoginResponse.builder()
                            .message("로그인 중 오류가 발생했습니다.")
                            .build());
        }
    }

    /**
     * Firebase 커스텀 토큰 발급 (JWT 토큰 인증 필요)
     * 프론트엔드에서 signInWithCustomToken(token)에 사용
     */
    @PostMapping("/firebase-token")
    public ResponseEntity<Map<String, String>> generateFirebaseToken(HttpServletRequest request) {
        try {
            // JWT에서 사용자 정보 추출
            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "인증 정보를 찾을 수 없습니다."));
            }
            
            // Firebase 활성화 확인
            if (FirebaseApp.getApps().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Firebase가 비활성화되어 있습니다."));
            }
            
            // 커스텀 클레임 추가 (옵션)
            Map<String, Object> claims = new HashMap<>();
            claims.put("appUserId", userId.toString());
            
            // Firebase 커스텀 토큰 생성
            String customToken = FirebaseAuth.getInstance()
                    .createCustomToken(userId.toString(), claims);
            
            log.info("Firebase 커스텀 토큰 생성 완료: userId={}, username={}", userId, username);
            
            return ResponseEntity.ok(Map.of(
                "token", customToken,
                "userId", userId.toString(),
                "username", username
            ));
            
        } catch (FirebaseAuthException e) {
            log.error("Firebase 커스텀 토큰 생성 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Firebase 토큰 생성 실패: " + e.getMessage()));
                    
        } catch (Exception e) {
            log.error("예상치 못한 오류로 Firebase 토큰 생성 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "토큰 생성 중 오류 발생"));
        }
    }

    /**
     * 토큰 유효성 검증 (옵션)
     */
    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyFirebaseToken(@RequestParam String token) {
        try {
            var decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decodedToken.getUid();
            String appUserId = decodedToken.getClaims().get("appUserId").toString();
            
            log.info("Firebase 토큰 검증 성공: uid={}, appUserId={}", uid, appUserId);
            
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "uid", uid,
                "appUserId", appUserId
            ));
            
        } catch (FirebaseAuthException e) {
            log.warn("Firebase 토큰 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "error", e.getMessage()));
                    
        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("valid", false, "error", "토큰 검증 실패"));
        }
    }
} 