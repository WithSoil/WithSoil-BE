package com.outthedoor.withsoil.api.diary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "농부일지 날짜별 요약 응답")
public record FarmDiaryCalendarResponse(
        @Schema(description = "일지가 작성된 날짜", example = "2026-05-05")
        LocalDate date,

        @Schema(description = "해당 날짜에 작성된 일지 개수", example = "3")
        long diaryCount
) {
}
