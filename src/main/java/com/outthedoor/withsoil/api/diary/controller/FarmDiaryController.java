package com.outthedoor.withsoil.api.diary.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.outthedoor.withsoil.api.diary.dto.request.FarmDiaryRequest;
import com.outthedoor.withsoil.api.diary.dto.response.FarmDiaryCalendarResponse;
import com.outthedoor.withsoil.api.diary.dto.response.FarmDiaryResponse;
import com.outthedoor.withsoil.api.diary.dto.response.FarmDiarySummaryResponse;
import com.outthedoor.withsoil.api.diary.service.FarmDiaryService;
import com.outthedoor.withsoil.api.member.entity.Member;
import com.outthedoor.withsoil.global.exeption.BaseException;
import com.outthedoor.withsoil.global.response.ApiResponse;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import com.outthedoor.withsoil.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

@Tag(name = "농부일지(FarmDiary)", description = "농부일지 기록, 조회, 수정, 삭제 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/farm-diaries")
@SecurityRequirement(name = "bearerAuth")
public class FarmDiaryController {

    private final FarmDiaryService farmDiaryService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(
            summary = "농부일지 생성",
            description = "오늘 한 일, 작물 사진, 메모를 등록합니다. multipart/form-data에서 request 파트에는 JSON 문자열을 입력해주세요.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            examples = @ExampleObject(
                                    name = "농부일지 생성 예시",
                                    value = "{\"request\":\"{\\\"diaryDateTime\\\":\\\"2026-05-05T09:30:00\\\",\\\"works\\\":[\\\"물 주기\\\",\\\"비료 주기\\\"],\\\"memo\\\":\\\"토마토에 물을 듬뿍 줬다.\\\"}\",\"photos\":[\"tomato.jpg\"]}"
                            )
                    )
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FarmDiaryResponse>> createDiary(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(
                    description = "농부일지 JSON 문자열",
                    required = true,
                    schema = @Schema(
                            type = "string",
                            example = "{\"diaryDateTime\":\"2026-05-05T09:30:00\",\"works\":[\"물 주기\",\"비료 주기\"],\"memo\":\"토마토에 물을 듬뿍 줬다.\"}"
                    )
            )
            @RequestPart("request") String requestJson,
            @Parameter(
                    description = "작물 사진 파일 목록",
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        FarmDiaryRequest request = parseRequest(requestJson);
        FarmDiaryResponse response = farmDiaryService.createDiary(member, request, photos);
        return ApiResponse.success(SuccessStatus.SUCCESS_DIARY_CREATE, response);
    }

    @Operation(summary = "월별 농부일지 목록 조회", description = "선택한 월에 작성한 농부일지를 최신 날짜순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FarmDiarySummaryResponse>>> getMonthlyDiaries(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(name = "month", description = "조회할 월", example = "2026-05", in = ParameterIn.QUERY)
            @RequestParam String month
    ) {
        YearMonth parsedMonth = parseYearMonth(month);
        List<FarmDiarySummaryResponse> response = farmDiaryService.getMonthlyDiaries(member, parsedMonth);
        return ApiResponse.success(SuccessStatus.SUCCESS_DIARY_LIST_GET, response);
    }

    @Operation(summary = "월별 농부일지 작성 날짜 조회", description = "선택한 월에 일지가 작성된 날짜와 날짜별 일지 개수를 조회합니다. 캘린더 표시용 API입니다.")
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<FarmDiaryCalendarResponse>>> getMonthlyCalendar(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(name = "month", description = "조회할 월", example = "2026-05", in = ParameterIn.QUERY)
            @RequestParam String month
    ) {
        YearMonth parsedMonth = parseYearMonth(month);
        List<FarmDiaryCalendarResponse> response = farmDiaryService.getMonthlyCalendar(member, parsedMonth);
        return ApiResponse.success(SuccessStatus.SUCCESS_DIARY_LIST_GET, response);
    }

    @Operation(summary = "날짜별 농부일지 조회", description = "특정 날짜에 작성한 농부일지 목록을 조회합니다.")
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<FarmDiaryResponse>>> getDiaryByDate(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "조회할 날짜", example = "2026-05-05")
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<FarmDiaryResponse> response = farmDiaryService.getDiariesByDate(member, date);
        return ApiResponse.success(SuccessStatus.SUCCESS_DIARY_GET, response);
    }

    @Operation(summary = "농부일지 상세 조회", description = "농부일지 ID로 상세 내용을 조회합니다.")
    @GetMapping("/{diaryId}")
    public ResponseEntity<ApiResponse<FarmDiaryResponse>> getDiary(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "일지 ID", example = "1") @PathVariable Long diaryId
    ) {
        FarmDiaryResponse response = farmDiaryService.getDiary(member, diaryId);
        return ApiResponse.success(SuccessStatus.SUCCESS_DIARY_GET, response);
    }

    @Operation(
            summary = "농부일지 수정",
            description = "일지 내용과 사진을 수정합니다. photos를 함께 보내면 기존 사진은 전체 교체됩니다. request 파트에는 JSON 문자열을 입력해주세요.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            examples = @ExampleObject(
                                    name = "농부일지 수정 예시",
                                    value = "{\"request\":\"{\\\"diaryDateTime\\\":\\\"2026-05-05T09:30:00\\\",\\\"works\\\":[\\\"물 주기\\\",\\\"가지치기\\\"],\\\"memo\\\":\\\"가지치기까지 마쳤다.\\\"}\",\"photos\":[\"updated.jpg\"]}"
                            )
                    )
            )
    )
    @PutMapping(value = "/{diaryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FarmDiaryResponse>> updateDiary(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "일지 ID", example = "1") @PathVariable Long diaryId,
            @Parameter(
                    description = "농부일지 JSON 문자열",
                    required = true,
                    schema = @Schema(
                            type = "string",
                            example = "{\"diaryDateTime\":\"2026-05-05T09:30:00\",\"works\":[\"물 주기\",\"가지치기\"],\"memo\":\"가지치기까지 마쳤다.\"}"
                    )
            )
            @RequestPart("request") String requestJson,
            @Parameter(
                    description = "교체할 작물 사진 파일 목록",
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        FarmDiaryRequest request = parseRequest(requestJson);
        FarmDiaryResponse response = farmDiaryService.updateDiary(member, diaryId, request, photos);
        return ApiResponse.success(SuccessStatus.SUCCESS_DIARY_UPDATE, response);
    }

    @Operation(summary = "농부일지 삭제", description = "농부일지를 삭제합니다.")
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<ApiResponse<Void>> deleteDiary(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "일지 ID", example = "1") @PathVariable Long diaryId
    ) {
        farmDiaryService.deleteDiary(member, diaryId);
        return ApiResponse.successOnly(SuccessStatus.SUCCESS_DIARY_DELETE);
    }

    @Operation(summary = "농부일지 사진 조회", description = "농부일지에 등록된 작물 사진 파일을 조회합니다.")
    @GetMapping("/photos/{photoId}")
    public ResponseEntity<Resource> getPhoto(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "사진 ID", example = "1") @PathVariable Long photoId
    ) {
        FarmDiaryService.PhotoFile photoFile = farmDiaryService.getPhoto(member, photoId);
        return ResponseEntity.ok()
                .contentType(photoFile.mediaType())
                .body(photoFile.resource());
    }

    private FarmDiaryRequest parseRequest(String requestJson) {
        try {
            FarmDiaryRequest request = objectMapper.readValue(requestJson, FarmDiaryRequest.class);
            validateRequest(request);
            return request;
        } catch (JacksonException e) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_INPUT, e);
        }
    }

    private void validateRequest(FarmDiaryRequest request) {
        Set<ConstraintViolation<FarmDiaryRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_INPUT);
        }
    }

    private YearMonth parseYearMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_INPUT, e);
        }
    }
}
