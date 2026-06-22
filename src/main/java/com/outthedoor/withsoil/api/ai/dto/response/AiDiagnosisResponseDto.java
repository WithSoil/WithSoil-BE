package com.outthedoor.withsoil.api.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "작물 병해 진단 응답")
public record AiDiagnosisResponseDto(
        @Schema(description = "AI 서버 처리 상태", example = "success")
        String status,

        @Schema(description = "진단 대상 작물", example = "감자")
        String crop,

        @JsonAlias("result_type")
        @Schema(description = "진단 결과 유형", example = "disease")
        String resultType,

        @Schema(description = "진단명", example = "감자_역병")
        String diagnosis,

        @Schema(description = "사용자에게 보여줄 진단 메시지", example = "감자 역병 가능성이 높습니다. 병든 잎을 제거하고 방제를 검토해주세요.")
        String message,

        @Schema(description = "진단 신뢰도", example = "0.92")
        Double confidence
) {}
