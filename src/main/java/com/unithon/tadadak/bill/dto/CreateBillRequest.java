package com.unithon.tadadak.bill.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBillRequest {
    @NotNull
    private Long groupId;
    @NotNull private Long userId;
    @NotNull @Min(0) private Integer amount;
}
