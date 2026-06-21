package com.outthedoor.withsoil.api.ai.controller;

import com.outthedoor.withsoil.api.ai.dto.request.AiChatRequestDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatResponseDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiDiagnosisResponseDto;
import com.outthedoor.withsoil.api.ai.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> testChat(@RequestBody AiChatRequestDto requestDto) {

        AiChatResponseDto responseDto = aiService.chatRag(requestDto.query());
        return ResponseEntity.ok(responseDto);
    }
    @PostMapping(value = "/diagnose", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AiDiagnosisResponseDto> diagnoseCrop(
            @RequestParam("crop_name") String cropName,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "topk", defaultValue = "5") int topk) {

        AiDiagnosisResponseDto responseDto = aiService.diagnoseCrop(cropName, file, topk);
        return ResponseEntity.ok(responseDto);
    }
}