package com.unithon.tadadak.user.api;

import com.unithon.tadadak.user.dto.UserRequest;
import com.unithon.tadadak.user.dto.UserResponse;
import com.unithon.tadadak.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 가입
    @PostMapping
    public UserResponse createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }
    
    // 회원 가입 (별칭)
    @PostMapping("/signup")
    public UserResponse signup(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    // 단일 회원 조회
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    // 닉네임 중복 확인
    @GetMapping("/check-username")
    public boolean checkUsername(@RequestParam String username) {
        return userService.isUsernameAvailable(username);
    }

    // 회원 정보 수정
    @PatchMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    // 회원 탈퇴
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // 📝 새로 추가: 사용자 가중치 업데이트
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
