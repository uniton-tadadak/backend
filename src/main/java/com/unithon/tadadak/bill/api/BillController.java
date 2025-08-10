package com.unithon.tadadak.bill.api;

import com.unithon.tadadak.bill.domain.Bill;
import com.unithon.tadadak.bill.dto.BillRequestDto;
import com.unithon.tadadak.bill.dto.BillResponseDto;
import com.unithon.tadadak.bill.service.BillService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {
    private final BillService billService;

    @PostMapping
    public ResponseEntity<BillResponseDto> create(@RequestBody BillRequestDto dto, HttpServletRequest request) {
        // JWT에서 사용자 정보 추출
        Long userId = getCurrentUserId(request);
        // dto에 userId 설정 (필요시)
        
        Bill bill = billService.create(dto);
        return ResponseEntity.ok(BillResponseDto.from(bill));
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userId;
    }
}

