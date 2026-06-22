package com.outthedoor.withsoil.global.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    /// 400 Bad Request
    BAD_REQUEST_INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    BAD_REQUEST_INVALID_EMAIL(HttpStatus.BAD_REQUEST, "잘못된 이메일 형식입니다."),
    BAD_REQUEST_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호 형식입니다."),
    BAD_REQUEST_INVALID_DIARY_PHOTO(HttpStatus.BAD_REQUEST, "작물 사진은 이미지 파일만 업로드할 수 있습니다."),
    BAD_REQUEST_INVALID_AI_REQUEST(HttpStatus.BAD_REQUEST, "AI 요청값이 올바르지 않습니다."),
    BAD_REQUEST_INVALID_AI_IMAGE(HttpStatus.BAD_REQUEST, "진단 이미지는 이미지 파일만 업로드할 수 있습니다."),

    /// 413 Payload Too Large
    PAYLOAD_TOO_LARGE_UPLOAD_FILE(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기를 초과했습니다."),

    /// 401 Unauthorized
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다."),
    UNAUTHORIZED_INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNAUTHORIZED_TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다."),
    UNAUTHORIZED_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    /// 403 Forbidden
    FORBIDDEN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    /// 404 Not Found
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."),
    NOT_FOUND_ARTICLE(HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
    NOT_FOUND_DIARY(HttpStatus.NOT_FOUND, "해당 농부일지를 찾을 수 없습니다."),
    NOT_FOUND_DIARY_PHOTO(HttpStatus.NOT_FOUND, "해당 농부일지 사진을 찾을 수 없습니다."),

    /// 409 Conflict
    CONFLICT_DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    /// 502 Bad Gateway
    BAD_GATEWAY_AI_SERVER_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "AI 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요."),
    BAD_GATEWAY_AI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "AI 서버 처리 중 오류가 발생했습니다."),

    /// 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    ;


    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
