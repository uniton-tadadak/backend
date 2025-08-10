package com.unithon.tadadak.bill.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBillRequest {
    @Min(0) private Integer amount; // null이면 변경 안함
}
