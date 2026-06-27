package com.outthedoor.withsoil.api.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "작물 추천 요청 DTO")
public class CropRecommendRequestDto {

    @NotBlank(message = "지역 정보는 필수입니다.")
    @Schema(description = "추천 대상 지역 (시군구 단위)", example = "진천군")
    private String region;

    @NotBlank(message = "재배 목적 및 상황은 필수입니다.")
    @Schema(description = "사용자의 재배 목적 및 환경 설명", example = "단독주택 마당에서 가족들과 가볍게 농사지어보고 싶어요. 초보자용으로 알려주세요.")
    private String purpose;
}