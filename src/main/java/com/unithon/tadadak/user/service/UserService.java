package com.unithon.tadadak.user.service;

import com.google.firebase.auth.FirebaseAuth;
import com.unithon.tadadak.auth.dto.LoginResponse;
import com.unithon.tadadak.auth.util.JwtUtil;
import com.unithon.tadadak.user.domain.User;
import com.unithon.tadadak.user.dto.UserRequest;
import com.unithon.tadadak.user.dto.UserResponse;
import com.unithon.tadadak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입 (JWT 토큰 포함)
    public LoginResponse createUser(UserRequest request) {
        try {
            // 사용자 이름 중복 확인
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return LoginResponse.builder()
                        .message("이미 존재하는 사용자명입니다.")
                        .build();
            }

            // 사용자 생성 (password 필수, 나머지는 기본값 사용)
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .trustScore(request.getTrustScore() != null ? request.getTrustScore() : 36.5f)
                    .penaltyCount(request.getPenaltyCount())
                    .praiseCount(request.getPraiseCount())
                    .moneyWeight(request.getMoneyWeight())
                    .distanceWeight(request.getDistanceWeight())
                    .trustWeight(request.getTrustWeight())
                    .build();

            User savedUser = userRepository.save(user);

            // JWT 토큰 생성
            String accessToken = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getUserId());

            // Firebase 커스텀 토큰 생성 (Firebase 활성화된 경우에만)
            String firebaseToken = null;
            try {
                if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
                    log.info("Firebase가 비활성화되어 있습니다. Firebase 토큰을 생성하지 않습니다.");
                } else {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("appUserId", savedUser.getUserId().toString());
                    firebaseToken = FirebaseAuth.getInstance()
                            .createCustomToken(savedUser.getUserId().toString(), claims);
                }
            } catch (Exception e) {
                log.warn("Firebase 토큰 생성 실패 (회원가입): {}", e.getMessage());
            }

            log.info("회원가입 성공: username={}, userId={}", savedUser.getUsername(), savedUser.getUserId());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .firebaseToken(firebaseToken)
                    .userId(savedUser.getUserId())
                    .username(savedUser.getUsername())
                    .message("회원가입 성공")
                    .build();

        } catch (Exception e) {
            log.error("회원가입 실패", e);
            return LoginResponse.builder()
                    .message("회원가입 중 오류가 발생했습니다.")
                    .build();
        }
    }

    // 기존 회원가입 (UserResponse 반환)
    public UserResponse createUserSimple(UserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .trustScore(request.getTrustScore())
                .penaltyCount(request.getPenaltyCount())
                .praiseCount(request.getPraiseCount())
                .moneyWeight(request.getMoneyWeight())
                .distanceWeight(request.getDistanceWeight())
                .trustWeight(request.getTrustWeight())
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    // 단일 회원 조회
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserResponse.from(user);
    }

    // 닉네임 중복 확인
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    // 회원 정보 수정
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.updateInfo(request.getUsername(), request.getTrustScore(),
                request.getPenaltyCount(), request.getPraiseCount());
        
        // 가중치 업데이트
        user.updateWeights(request.getMoneyWeight(), request.getDistanceWeight(), request.getTrustWeight());

        return UserResponse.from(userRepository.save(user));
    }

    // 📝 새로 추가: 가중치만 업데이트
    public UserResponse updateUserWeights(Long id, Double moneyWeight, Double distanceWeight, Double trustWeight) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.updateWeights(moneyWeight, distanceWeight, trustWeight);

        return UserResponse.from(userRepository.save(user));
    }

    // 회원 탈퇴
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
