package com.unithon.tadadak.bill.api;

import com.unithon.tadadak.bill.domain.Bill;
import com.unithon.tadadak.bill.dto.BillRequestDto;
import com.unithon.tadadak.bill.dto.BillResponseDto;
import com.unithon.tadadak.bill.service.BillService;
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
    public ResponseEntity<BillResponseDto> create(@RequestBody BillRequestDto dto) {
        Bill bill = billService.create(dto);
        return ResponseEntity.ok(BillResponseDto.from(bill));
    }
}

