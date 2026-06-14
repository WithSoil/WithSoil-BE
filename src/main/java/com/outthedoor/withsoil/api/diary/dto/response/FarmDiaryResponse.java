package com.outthedoor.withsoil.api.diary.dto.response;

import com.outthedoor.withsoil.api.diary.entity.FarmDiary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "농부일지 상세 응답")
public record FarmDiaryResponse(
        @Schema(description = "일지 ID", example = "1")
        Long id,

        @Schema(description = "일지 작성 일시", example = "2026-05-05T09:30:00")
        LocalDateTime diaryDateTime,

        @Schema(description = "오늘 한 일 목록", example = "[\"물 주기\", \"비료 주기\"]")
        List<String> works,

        @Schema(description = "일지 메모", example = "토마토에 물을 듬뿍 줬다. 잎이 싱싱해 보인다.")
        String memo,

        @Schema(description = "작물 사진 목록")
        List<FarmDiaryPhotoResponse> photos
) {
    public static FarmDiaryResponse of(FarmDiary diary) {
        return new FarmDiaryResponse(
                diary.getId(),
                diary.getDiaryDateTime(),
                List.copyOf(diary.getWorks()),
                diary.getMemo(),
                diary.getPhotos().stream()
                        .map(FarmDiaryPhotoResponse::of)
                        .toList()
        );
    }
}
