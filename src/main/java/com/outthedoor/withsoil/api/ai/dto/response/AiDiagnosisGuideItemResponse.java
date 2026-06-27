package com.outthedoor.withsoil.api.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "병해 진단 가이드 항목")
public record AiDiagnosisGuideItemResponse(
        @Schema(description = "가이드 항목 제목", example = "예방/방제")
        String title,

        @Schema(description = "가이드 항목 내용", example = "병든 잎은 일찍 제거하고, 통풍과 배수를 관리해주세요.")
        String content
) {
}
