package com.unithon.tadadak.user.service;

import com.unithon.tadadak.user.domain.User;
import com.unithon.tadadak.user.dto.UserRequest;
import com.unithon.tadadak.user.dto.UserResponse;
import com.unithon.tadadak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 회원 가입
    public UserResponse createUser(UserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
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
