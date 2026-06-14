package com.outthedoor.withsoil.api.diary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Schema(description = "농부일지 생성/수정 요청")
public record FarmDiaryRequest(
        @Schema(description = "일지 작성 일시", example = "2026-05-05T09:30:00")
        @NotNull(message = "일지 작성 일시는 필수입니다.")
        LocalDateTime diaryDateTime,

        @Schema(description = "오늘 한 일 목록", example = "[\"물 주기\", \"비료 주기\", \"잡초 제거\"]")
        @Size(max = 20, message = "오늘 한 일은 최대 20개까지 입력할 수 있습니다.")
        List<@Size(max = 50, message = "오늘 한 일은 50자 이내로 입력해주세요.") String> works,

        @Schema(description = "일지 메모", example = "토마토에 물을 듬뿍 줬다. 잎이 싱싱해 보인다.")
        @Size(max = 1000, message = "메모는 1000자 이내로 입력해주세요.")
        String memo
) {
    public List<String> normalizedWorks() {
        if (works == null) {
            return new ArrayList<>();
        }

        return works.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(work -> !work.isBlank())
                .toList();
    }
}
