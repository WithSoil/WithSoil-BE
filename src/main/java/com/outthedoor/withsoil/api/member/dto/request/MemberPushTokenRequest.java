package com.outthedoor.withsoil.api.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Expo 푸시 알림 토큰")
public record MemberPushTokenRequest(
        @NotBlank(message = "푸시 토큰은 필수입니다.")
        @Pattern(
                regexp = "^(Expo|Exponent)PushToken\\[[^]]+]$",
                message = "올바른 Expo 푸시 토큰 형식이 아닙니다."
        )
        String pushToken
) {
}
