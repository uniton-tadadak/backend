package com.unithon.tadadak.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = new ErrorResponse(errorCode);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        ErrorResponse response = new ErrorResponse(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ex.getBindingResult().getFieldErrors().stream()
                        .findFirst()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                        .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage())
        );
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse response = new ErrorResponse(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );
        return new ResponseEntity<>(response, ErrorCode.INVALID_INPUT_VALUE.getStatus());
    }
}

