package com.outthedoor.withsoil.api.member.dto.request;

import com.outthedoor.withsoil.api.member.entity.MemberLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "사용자 위치 정보")
public record MemberLocationRequest(
        @Schema(description = "시/도", example = "전라북도")
        @NotBlank(message = "시/도는 필수입니다.")
        @Size(max = 20, message = "시/도는 20자 이하로 입력해 주세요.")
        String sido,

        @Schema(description = "시/군/구", example = "김제시")
        @NotBlank(message = "시/군/구는 필수입니다.")
        @Size(max = 30, message = "시/군/구는 30자 이하로 입력해 주세요.")
        String sigungu,

        @Schema(description = "읍/면/동", example = "백산면")
        @Size(max = 30, message = "읍/면/동은 30자 이하로 입력해 주세요.")
        String eupMyeonDong,

        @Schema(description = "리", example = "상정리")
        @Size(max = 30, message = "리는 30자 이하로 입력해 주세요.")
        String ri,

        @Schema(description = "위도", example = "35.8032000")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
        BigDecimal latitude,

        @Schema(description = "경도", example = "126.8801000")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
        BigDecimal longitude
) {
    public MemberLocation toEntity() {
        return MemberLocation.of(sido, sigungu, eupMyeonDong, ri, latitude, longitude);
    }
}
