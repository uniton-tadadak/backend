package com.unithon.tadadak.user.api;

import com.unithon.tadadak.auth.dto.LoginResponse;
import com.unithon.tadadak.user.dto.UserRequest;
import com.unithon.tadadak.user.dto.UserResponse;
import com.unithon.tadadak.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 가입 (JWT 토큰 포함)
    @PostMapping
    public LoginResponse createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }
    
    // 회원 가입 (별칭)
    @PostMapping("/signup")
    public LoginResponse signup(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    // 단일 회원 조회 (자신의 정보만 조회 가능)
    @GetMapping("/me")
    public UserResponse getMyInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userService.getUser(userId);
    }

    // 관리자용 또는 공개 정보 조회 (필요 시)
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    // 닉네임 중복 확인
    @GetMapping("/check-username")
    public boolean checkUsername(@RequestParam String username) {
        return userService.isUsernameAvailable(username);
    }

    // 회원 정보 수정 (자신의 정보만 수정 가능)
    @PatchMapping("/me")
    public UserResponse updateMyInfo(@RequestBody UserRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userService.updateUser(userId, request);
    }

    // 관리자용 회원 정보 수정 (필요 시)
    @PatchMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    // 회원 탈퇴 (자신만 탈퇴 가능)
    @DeleteMapping("/me")
    public void deleteMyAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        userService.deleteUser(userId);
    }

    // 관리자용 회원 탈퇴 (필요 시)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // 📝 사용자 가중치 업데이트 (자신의 가중치만 수정 가능)
    @PutMapping("/me/weights")
    public UserResponse updateMyWeights(
            @RequestBody WeightUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userService.updateUserWeights(userId, 
                request.getMoneyWeight(), 
                request.getDistanceWeight(), 
                request.getTrustWeight());
    }

    // 관리자용 사용자 가중치 업데이트 (필요 시)
    @PutMapping("/{id}/weights")
    public UserResponse updateUserWeights(
            @PathVariable Long id,
            @RequestBody WeightUpdateRequest request) {
        return userService.updateUserWeights(id, 
                request.getMoneyWeight(), 
                request.getDistanceWeight(), 
                request.getTrustWeight());
    }

    // 📝 가중치 업데이트 전용 DTO
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WeightUpdateRequest {
        private Double moneyWeight;
        private Double distanceWeight;
        private Double trustWeight;
    }
}
