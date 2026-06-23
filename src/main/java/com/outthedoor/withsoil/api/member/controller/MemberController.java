package com.outthedoor.withsoil.api.member.controller;

import com.outthedoor.withsoil.api.member.dto.request.MemberLocationRequest;
import com.outthedoor.withsoil.api.member.dto.request.MemberPushTokenRequest;
import com.outthedoor.withsoil.api.member.dto.request.MemberLoginRequest;
import com.outthedoor.withsoil.api.member.dto.request.MemberSignupRequest;
import com.outthedoor.withsoil.api.member.dto.response.MemberLocationResponse;
import com.outthedoor.withsoil.api.member.dto.response.MemberLoginResponse;
import com.outthedoor.withsoil.api.member.dto.response.MemberMypageResponse;
import com.outthedoor.withsoil.api.member.dto.response.MemberSignupResponse;
import com.outthedoor.withsoil.api.member.entity.Member;
import com.outthedoor.withsoil.api.member.service.MemberService;
import com.outthedoor.withsoil.global.response.ApiResponse;
import com.outthedoor.withsoil.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원(Member)", description = "회원 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름을 받아 회원을 생성합니다. 농장 위치는 가입 후 별도 API로 설정합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberSignupResponse>> signup(
            @Valid @RequestBody MemberSignupRequest requestDTO
    ) {
        MemberSignupResponse responseDTO = memberService.signup(requestDTO);

        return ApiResponse.success(SuccessStatus.SUCCESS_MEMBER_REGISTRATION, responseDTO);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증하고 JWT Access Token을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberLoginResponse>> login(
            @Valid @RequestBody MemberLoginRequest requestDTO
    ) {
        MemberLoginResponse responseDTO = memberService.login(requestDTO);

        return ApiResponse.success(SuccessStatus.SUCCESS_MEMBER_LOGIN, responseDTO);
    }


    @Operation(summary = "회원 위치 수정", description = "로그인한 회원의 농장 위치 정보를 저장하거나 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/location")
    public ResponseEntity<ApiResponse<MemberLocationResponse>> updateLocation(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Valid @RequestBody MemberLocationRequest requestDTO
    ) {
        MemberLocationResponse responseDTO = memberService.updateLocation(member, requestDTO);

        return ApiResponse.success(SuccessStatus.SUCCESS_MEMBER_LOCATION_UPDATE, responseDTO);
    }

    @Operation(summary = "푸시 토큰 등록", description = "앱이 발급한 Expo 푸시 토큰을 회원에게 등록합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/push-token")
    public ResponseEntity<ApiResponse<Void>> updatePushToken(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Valid @RequestBody MemberPushTokenRequest requestDTO
    ) {
        memberService.updatePushToken(member, requestDTO);
        return ApiResponse.successOnly(SuccessStatus.SUCCESS_PUSH_TOKEN_UPDATE);
    }

    @Operation(summary = "마이페이지 조회", description = "로그인한 회원의 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<MemberMypageResponse>> getMypage(
            @AuthenticationPrincipal(expression = "member") Member member
    ) {
        MemberMypageResponse responseDTO = memberService.getMypage(member);

        return ApiResponse.success(SuccessStatus.SUCCESS_MEMBER_MYPAGE_GET, responseDTO);
    }
}
