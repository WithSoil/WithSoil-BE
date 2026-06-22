package com.outthedoor.withsoil.api.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 챗봇 요청. chatId가 없으면 새 채팅방을 생성하고, chatId가 있으면 기존 채팅방에 이어서 저장합니다.")
public record AiChatRequestDto(
        @Schema(description = "이어 대화할 채팅방 ID. 비워두면 새 채팅방을 생성합니다.", example = "1", nullable = true)
        Long chatId,

        @Schema(description = "AI에게 전달할 질문", example = "감자 잎에 갈색 반점이 생겼는데 어떻게 해야 하나요?")
        @NotBlank(message = "질문은 필수입니다.")
        @Size(max = 500, message = "질문은 500자 이내로 입력해주세요.")
        String query
) {}
