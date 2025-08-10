package com.unithon.tadadak.global.exception;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final int status;
    private final String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
    }

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}