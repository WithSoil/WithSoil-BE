package com.outthedoor.withsoil.api.ai.dto.response;

import com.outthedoor.withsoil.api.ai.entity.AiChatMessage;
import com.outthedoor.withsoil.api.ai.entity.AiChatMessageRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "AI 채팅 메시지 응답")
public record AiChatMessageResponse(
        @Schema(description = "메시지 ID", example = "1")
        Long id,

        @Schema(description = "메시지 작성 주체", example = "USER")
        AiChatMessageRole role,

        @Schema(description = "메시지 내용", example = "감자 잎이 갈색으로 변했는데 어떻게 해야 하나요?")
        String content,

        @Schema(description = "메시지 작성 일시", example = "2026-06-22T10:30:00")
        LocalDateTime messageDateTime
) {
    public static AiChatMessageResponse from(AiChatMessage message) {
        return new AiChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getMessageDateTime()
        );
    }
}
