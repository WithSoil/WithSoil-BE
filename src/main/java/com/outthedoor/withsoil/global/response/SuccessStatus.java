package com.outthedoor.withsoil.global.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    /// 200 Ok
    SUCCESS_MEMBER_LOGIN(HttpStatus.OK, "로그인 성공"),
    SUCCESS_ARTICLE_GET(HttpStatus.OK, "게시글 조회 성공"),
    SUCCESS_ARTICLE_UPDATE(HttpStatus.OK, "게시글 수정 성공"),

    /// 201 Created
    SUCCESS_MEMBER_REGISTRATION(HttpStatus.CREATED, "회원가입 성공"),
    SUCCESS_ARTICLE_CREATE(HttpStatus.CREATED, "게시글 생성 성공"),

    /// 204 No Content
    SUCCESS_MEMBER_WITHDRAW(HttpStatus.NO_CONTENT, "회원탈퇴 성공"),
    SUCCESS_ARTICLE_DELETE(HttpStatus.NO_CONTENT, "게시글 삭제 성공"),

    ;


    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}