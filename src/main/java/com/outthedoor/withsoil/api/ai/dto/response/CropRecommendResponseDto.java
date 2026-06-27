package com.outthedoor.withsoil.api.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "작물 추천 최종 응답 DTO")
public class CropRecommendResponseDto {

    @Schema(description = "요청된 지역", example = "진천군")
    private String region;

    @Schema(description = "요청된 재배 목적", example = "단독주택 마당에서 가족들과 가볍게...")
    private String purpose;

    @Schema(description = "추천된 작물 상세 목록")
    private List<CropRecommendDetailDto> recommendedCrops;
}