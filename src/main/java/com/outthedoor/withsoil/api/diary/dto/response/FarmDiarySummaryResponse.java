package com.outthedoor.withsoil.api.diary.dto.response;

import com.outthedoor.withsoil.api.diary.entity.FarmDiary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "농부일지 목록 응답")
public record FarmDiarySummaryResponse(
        @Schema(description = "일지 ID", example = "1")
        Long id,

        @Schema(description = "일지 작성 일시", example = "2026-05-28T09:30:00")
        LocalDateTime diaryDateTime,

        @Schema(description = "대표 사진 URL", example = "/api/v1/farm-diaries/photos/1")
        String thumbnailUrl,

        @Schema(description = "목록 미리보기 문구", example = "오늘은 토마토에 물을 듬뿍 주었다...")
        String preview
) {
    private static final int PREVIEW_LENGTH = 35;

    public static FarmDiarySummaryResponse of(FarmDiary diary) {
        String thumbnailUrl = diary.getPhotos().stream()
                .findFirst()
                .map(photo -> "/api/v1/farm-diaries/photos/" + photo.getId())
                .orElse(null);

        return new FarmDiarySummaryResponse(
                diary.getId(),
                diary.getDiaryDateTime(),
                thumbnailUrl,
                createPreview(diary)
        );
    }

    private static String createPreview(FarmDiary diary) {
        String source = diary.getMemo();
        if (source == null || source.isBlank()) {
            source = String.join(", ", diary.getWorks());
        }
        if (source == null || source.isBlank()) {
            return "기록된 내용이 없습니다.";
        }
        if (source.length() <= PREVIEW_LENGTH) {
            return source;
        }
        return source.substring(0, PREVIEW_LENGTH) + "...";
    }
}
