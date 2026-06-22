package com.outthedoor.withsoil.api.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 챗봇 응답")
public record AiChatResponseDto(
        @Schema(description = "AI 서버 처리 상태", example = "success")
        String status,

        @Schema(description = "AI 답변", example = "감자 잎의 갈색 반점은 역병이나 점무늬병 가능성이 있어 병든 잎을 제거하고 통풍을 확보해주세요.")
        String answer
) {}
