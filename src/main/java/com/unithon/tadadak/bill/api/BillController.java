package com.unithon.tadadak.bill.api;

import com.unithon.tadadak.bill.domain.BillStatus;
import com.unithon.tadadak.bill.dto.BillResponse;
import com.unithon.tadadak.bill.dto.CreateBillRequest;
import com.unithon.tadadak.bill.dto.UpdateBillRequest;
import com.unithon.tadadak.bill.dto.UpdateBillStatusRequest;
import com.unithon.tadadak.bill.service.BillService;
<<<<<<< HEAD
import jakarta.servlet.http.HttpServletRequest;
=======
import jakarta.validation.Valid;
>>>>>>> 3718bf4 (bill 도메인 추가 + post 도메인 수정)
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<BillResponseDto> create(@RequestBody BillRequestDto dto, HttpServletRequest request) {
        // JWT에서 사용자 정보 추출
        Long userId = getCurrentUserId(request);
        // dto에 userId 설정 (필요시)
        
        Bill bill = billService.create(dto);
        return ResponseEntity.ok(BillResponseDto.from(bill));
=======
    public BillResponse create(@RequestBody @Valid CreateBillRequest req) {
        return billService.create(req);
    }

    @GetMapping("/{billId}")
    public BillResponse get(@PathVariable Long billId) {
        return billService.get(billId);
    }

    @GetMapping
    public Page<BillResponse> list(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BillStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return billService.list(groupId, userId, status, page, size);
    }

    @PatchMapping("/{billId}")
    public BillResponse update(@PathVariable Long billId,
                               @RequestBody @Valid UpdateBillRequest req) {
        return billService.update(billId, req);
    }

    @PatchMapping("/{billId}/status")
    public BillResponse updateStatus(@PathVariable Long billId,
                                     @RequestBody @Valid UpdateBillStatusRequest req) {
        return billService.updateStatus(billId, req);
    }

    @DeleteMapping("/{billId}")
    public void delete(@PathVariable Long billId) {
        billService.delete(billId);
>>>>>>> 3718bf4 (bill 도메인 추가 + post 도메인 수정)
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
        }
        return userId;
    }
}

