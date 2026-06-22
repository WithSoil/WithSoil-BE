package com.outthedoor.withsoil.api.ai.controller;

import com.outthedoor.withsoil.api.ai.dto.request.AiChatRequestDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatDetailResponse;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatResponseDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatSummaryResponse;
import com.outthedoor.withsoil.api.ai.dto.response.AiDiagnosisResponseDto;
import com.outthedoor.withsoil.api.ai.service.AiService;
import com.outthedoor.withsoil.api.member.entity.Member;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            description = "chatId가 없거나 null이면 새 채팅방을 생성하고, chatId가 있으면 로그인한 사용자의 기존 채팅방에 메시지를 추가합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AiChatRequestDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "새 채팅 시작",
                                            summary = "새 채팅방 생성",
                                            value = "{\"query\":\"감자 잎에 갈색 반점이 생겼는데 어떻게 해야 하나요?\"}"
                                    ),
                                    @ExampleObject(
                                            name = "기존 채팅 이어쓰기",
                                            summary = "기존 채팅방에서 이어서",
                                            value = "{\"chatId\":1,\"query\":\"그럼 물 주는 양은 어떻게 조절해야 해?\"}"
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 챗봇 응답 및 메시지 저장 성공"),
            @ApiResponse(responseCode = "400", description = "질문 입력값이 올바르지 않음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "502", description = "AI 서버 연결 실패 또는 처리 오류", content = @Content)
    })
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> chat(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Valid @RequestBody AiChatRequestDto requestDto
    ) {
        AiChatResponseDto responseDto = aiService.chatRag(member, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "AI 채팅방 목록 조회", description = "로그인한 사용자의 삭제되지 않은 AI 채팅방 목록을 최신순으로 조회합니다. 목록 화면용으로 채팅방 ID, 제목, 생성 일시, 마지막 메시지만 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 채팅방 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = @Content)
    })
    @GetMapping("/chats")
    public ResponseEntity<List<AiChatSummaryResponse>> getChatHistories(
            @AuthenticationPrincipal(expression = "member") Member member
    ) {
        return ResponseEntity.ok(aiService.getChatHistories(member));
    }

    @Operation(summary = "AI 채팅방 상세 조회", description = "채팅방 ID로 로그인한 사용자의 특정 채팅방 메시지 전체를 시간순으로 조회합니다. 실제 대화 화면 진입 시 사용하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 채팅방 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/chats/{chatId}")
    public ResponseEntity<AiChatDetailResponse> getChatHistory(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "채팅방 ID", example = "1") @PathVariable Long chatId
    ) {
        return ResponseEntity.ok(aiService.getChatHistory(member, chatId));
    }

    @Operation(summary = "AI 채팅방 삭제", description = "로그인한 사용자의 AI 채팅방을 삭제합니다. 삭제된 채팅방은 목록/상세 조회와 이어쓰기 대상에서 제외됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "AI 채팅방 삭제 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<Void> deleteChatHistory(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "채팅방 ID", example = "1") @PathVariable Long chatId
    ) {
        aiService.deleteChat(member, chatId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "작물 병해 진단",
            description = "작물명과 작물 사진을 AI 서버로 전달하여 병해 진단 결과를 반환합니다. multipart/form-data로 요청해주세요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작물 병해 진단 성공"),
            @ApiResponse(responseCode = "400", description = "작물명, 이미지, topk 입력값이 올바르지 않음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = @Content),
            @ApiResponse(responseCode = "502", description = "AI 서버 연결 실패 또는 처리 오류", content = @Content)
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
