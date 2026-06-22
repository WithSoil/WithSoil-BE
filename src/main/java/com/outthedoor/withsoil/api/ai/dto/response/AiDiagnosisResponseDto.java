package com.outthedoor.withsoil.api.ai.dto.response;

public record AiDiagnosisResponseDto(
        String status,
        String crop,
        String resultType,
        String diagnosis,
        String message,
        Double confidence
) {}