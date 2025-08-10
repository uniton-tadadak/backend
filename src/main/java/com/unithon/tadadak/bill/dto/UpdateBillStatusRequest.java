package com.unithon.tadadak.bill.dto;

import com.unithon.tadadak.bill.domain.BillStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBillStatusRequest {
    @NotNull
    private BillStatus status;
}
