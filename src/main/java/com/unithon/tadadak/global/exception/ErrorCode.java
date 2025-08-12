package com.unithon.tadadak.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "공고를 찾을 수 없습니다"),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹을 찾을 수 없습니다"),
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "위치를 찾을 수 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다"),
    
    DUPLICATE_JOIN(HttpStatus.CONFLICT, "이미 참여한 파티입니다"),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다"),
    GROUP_FULL(HttpStatus.CONFLICT, "그룹이 가득 찼습니다"),
    
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),
    
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다"),
    HOST_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "호스트는 그룹을 나갈 수 없습니다. 그룹을 삭제해주세요."),

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 데이터입니다."),
    // Bill 관련
    BILL_NOT_FOUND(HttpStatus.NOT_FOUND, "청구서를 찾을 수 없습니다."),
    NOT_GROUP_MEMBER(HttpStatus.BAD_REQUEST, "해당 유저는 그룹원이 아닙니다."),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "금액은 0 이상이어야 합니다."),
    GROUP_MEMBER_COUNT_INVALID(HttpStatus.NOT_FOUND, "그룹원의 수는 0보다 커야합니다.");

    private final HttpStatus status;
    private final String message;

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}