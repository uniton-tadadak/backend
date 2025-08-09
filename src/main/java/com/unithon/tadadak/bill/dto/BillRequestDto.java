package com.unithon.tadadak.bill.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BillRequestDto {
    private Long groupId;
    private Long userId;
    private Integer amount;
    private String status; // PENDING, PAID
}

