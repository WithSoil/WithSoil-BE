package com.outthedoor.withsoil.api.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record MemberSignupRequest(
        @Schema(description = "이메일", example = "farmer@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "잘못된 이메일 형식입니다.")
        String email,

        @Schema(description = "비밀번호", example = "password123")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하로 입력해 주세요.")
        String password,

        @Schema(description = "이름", example = "김농부")
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 20, message = "이름은 20자 이하로 입력해 주세요.")
        String name
) {
}
