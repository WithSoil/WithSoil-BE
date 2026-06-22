package com.outthedoor.withsoil.api.ai.controller;

import com.outthedoor.withsoil.api.ai.dto.request.AiChatRequestDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatResponseDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiDiagnosisResponseDto;
import com.outthedoor.withsoil.api.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "AI", description = "AI 챗봇 및 작물 병해 진단 API 입니다.")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiService aiService;

    @Operation(
            summary = "AI 챗봇 질문",
            description = "AI 서버의 RAG/일반 챗봇에 질문을 전달하고 답변을 반환합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AiChatRequestDto.class),
                            examples = @ExampleObject(
                                    name = "챗봇 질문 예시",
                                    value = "{\"query\":\"감자 잎에 갈색 반점이 생겼는데 어떻게 해야 하나요?\"}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 챗봇 응답 성공"),
            @ApiResponse(responseCode = "400", description = "질문 입력값이 올바르지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "502", description = "AI 서버 연결 실패 또는 처리 오류")
    })
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> chat(
            @Valid @RequestBody AiChatRequestDto requestDto
    ) {
        AiChatResponseDto responseDto = aiService.chatRag(requestDto.query());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "작물 병해 진단",
            description = "작물명과 작물 사진을 AI 서버로 전달하여 병해 진단 결과를 반환합니다. multipart/form-data로 요청해주세요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작물 병해 진단 성공"),
            @ApiResponse(responseCode = "400", description = "작물명, 이미지, topk 입력값이 올바르지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "502", description = "AI 서버 연결 실패 또는 처리 오류")
    })
    @PostMapping(value = "/diagnose", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AiDiagnosisResponseDto> diagnoseCrop(
            @Parameter(description = "진단할 작물명", example = "감자", required = true)
            @RequestParam("crop_name") String cropName,
            @Parameter(
                    description = "진단할 작물 이미지 파일",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "상위 예측 결과 개수", example = "5")
            @RequestParam(value = "topk", defaultValue = "5") int topk
    ) {
        AiDiagnosisResponseDto responseDto = aiService.diagnoseCrop(cropName, file, topk);
        return ResponseEntity.ok(responseDto);
    }
}
