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
    SUCCESS_MEMBER_MYPAGE_GET(HttpStatus.OK, "마이페이지 조회 성공"),
    SUCCESS_MEMBER_LOCATION_UPDATE(HttpStatus.OK, "회원 위치 수정 성공"),
    SUCCESS_DIARY_GET(HttpStatus.OK, "농부일지 조회 성공"),
    SUCCESS_DIARY_LIST_GET(HttpStatus.OK, "농부일지 목록 조회 성공"),
    SUCCESS_DIARY_UPDATE(HttpStatus.OK, "농부일지 수정 성공"),
    SUCCESS_WEATHER_FORECAST_GET(HttpStatus.OK, "6시간 후 기상예보 조회 성공"),
    SUCCESS_PUSH_TOKEN_UPDATE(HttpStatus.OK, "푸시 알림 토큰 등록 성공"),
    SUCCESS_WEATHER_NOTIFICATION_SEND(HttpStatus.OK, "기상예보 푸시 알림 발송 요청 성공"),

    /// 201 Created
    SUCCESS_MEMBER_REGISTRATION(HttpStatus.CREATED, "회원가입 성공"),
    SUCCESS_ARTICLE_CREATE(HttpStatus.CREATED, "게시글 생성 성공"),
    SUCCESS_DIARY_CREATE(HttpStatus.CREATED, "농부일지 생성 성공"),

    /// 204 No Content
    SUCCESS_MEMBER_WITHDRAW(HttpStatus.NO_CONTENT, "회원탈퇴 성공"),
    SUCCESS_ARTICLE_DELETE(HttpStatus.NO_CONTENT, "게시글 삭제 성공"),
    SUCCESS_DIARY_DELETE(HttpStatus.NO_CONTENT, "농부일지 삭제 성공"),

    ;


    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
