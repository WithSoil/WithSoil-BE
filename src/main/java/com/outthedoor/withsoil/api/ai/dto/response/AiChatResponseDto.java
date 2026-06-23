package com.outthedoor.withsoil.api.ai.dto.response;

import com.outthedoor.withsoil.api.ai.entity.AiChat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "AI 챗봇 응답")
public record AiChatResponseDto(
        @Schema(description = "채팅방 ID", example = "1")
        Long chatId,

        @Schema(description = "채팅 제목", example = "감자 잎 갈색 반점 문의")
        String title,

        @Schema(description = "AI 서버 처리 상태", example = "success")
        String status,

        @Schema(description = "AI 답변", example = "감자 잎의 갈색 반점은 역병이나 점무늬병 가능성이 있어 병든 잎을 제거하고 통풍을 확보해주세요.")
        String answer,

        @Schema(description = "AI 응답 저장 일시", example = "2026-06-22T10:32:00")
        LocalDateTime messageDateTime
) {
    public static AiChatResponseDto of(AiChat aiChat, String status, String answer, LocalDateTime messageDateTime) {
        return new AiChatResponseDto(
                aiChat.getId(),
                aiChat.getTitle(),
                status,
                answer,
                messageDateTime
        );
    }
}
