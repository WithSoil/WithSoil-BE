package com.outthedoor.withsoil.api.diary.dto.response;

import com.outthedoor.withsoil.api.diary.entity.FarmDiaryPhoto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "농부일지 사진 응답")
public record FarmDiaryPhotoResponse(
        @Schema(description = "사진 ID", example = "1")
        Long id,

        @Schema(description = "원본 파일명", example = "tomato.jpg")
        String originalFilename,

        @Schema(description = "사진 조회 URL", example = "/api/v1/farm-diaries/photos/1")
        String imageUrl
) {
    public static FarmDiaryPhotoResponse of(FarmDiaryPhoto photo) {
        return new FarmDiaryPhotoResponse(
                photo.getId(),
                photo.getOriginalFilename(),
                "/api/v1/farm-diaries/photos/" + photo.getId()
        );
    }
}
