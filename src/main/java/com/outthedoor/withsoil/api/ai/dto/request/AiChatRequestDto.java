package com.outthedoor.withsoil.api.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 챗봇 요청")
public record AiChatRequestDto(
        @Schema(description = "AI에게 전달할 질문", example = "감자 잎에 갈색 반점이 생겼는데 어떻게 해야 하나요?")
        @NotBlank(message = "질문은 필수입니다.")
        @Size(max = 500, message = "질문은 500자 이내로 입력해주세요.")
        String query
) {}
