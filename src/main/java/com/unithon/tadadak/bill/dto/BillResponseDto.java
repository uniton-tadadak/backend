package com.unithon.tadadak.bill.dto;

import com.unithon.tadadak.bill.domain.Bill;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BillResponseDto {
    private Long billId;
    private Long groupId;
    private Long userId;
    private Integer amount;
    private String status;
    private LocalDateTime createdAt;

    public static BillResponseDto from(Bill bill) {
        return BillResponseDto.builder()
                .billId(bill.getBillId())
                .groupId(bill.getGroup().getGroupId())
                .userId(bill.getUser().getUserId())
                .amount(bill.getAmount())
                .status(bill.getStatus())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}

