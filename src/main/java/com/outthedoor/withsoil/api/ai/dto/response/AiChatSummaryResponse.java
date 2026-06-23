package com.outthedoor.withsoil.api.ai.dto.response;

import com.outthedoor.withsoil.api.ai.entity.AiChat;
import com.outthedoor.withsoil.api.ai.entity.AiChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "AI 채팅방 요약 응답")
public record AiChatSummaryResponse(
        @Schema(description = "채팅방 ID", example = "1")
        Long chatId,

        @Schema(description = "채팅 제목", example = "감자 잎 갈색 반점 문의")
        String title,

        @Schema(description = "채팅방 생성 일시", example = "2026-06-22T10:30:00")
        LocalDateTime chatDateTime,

        @Schema(description = "마지막 메시지 내용", example = "통풍을 확보하고 병든 잎을 제거해주세요.")
        String lastMessage,

        @Schema(description = "마지막 메시지 일시", example = "2026-06-22T10:32:00")
        LocalDateTime lastMessageDateTime
) {
    public static AiChatSummaryResponse of(AiChat aiChat, AiChatMessage lastMessage) {
        return new AiChatSummaryResponse(
                aiChat.getId(),
                aiChat.getTitle(),
                aiChat.getChatDateTime(),
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getMessageDateTime() : null
        );
    }
}
