package com.outthedoor.withsoil.api.ai.dto.response;

import com.outthedoor.withsoil.api.ai.entity.AiChat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "AI 채팅방 상세 응답")
public record AiChatDetailResponse(
        @Schema(description = "채팅방 ID", example = "1")
        Long chatId,

        @Schema(description = "채팅 제목", example = "감자 잎 갈색 반점 문의")
        String title,

        @Schema(description = "채팅방 생성 일시", example = "2026-06-22T10:30:00")
        LocalDateTime chatDateTime,

        @Schema(description = "채팅 메시지 목록")
        List<AiChatMessageResponse> messages
) {
    public static AiChatDetailResponse of(AiChat aiChat, List<AiChatMessageResponse> messages) {
        return new AiChatDetailResponse(
                aiChat.getId(),
                aiChat.getTitle(),
                aiChat.getChatDateTime(),
                messages
        );
    }
}
