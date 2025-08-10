package com.unithon.tadadak.bill.dto;

import com.unithon.tadadak.bill.domain.BillStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BillResponse {
    private Long billId;
    private Long groupId;
    private Long userId;
    private Integer amount;
    private BillStatus status;
    private LocalDateTime createdAt;
}

