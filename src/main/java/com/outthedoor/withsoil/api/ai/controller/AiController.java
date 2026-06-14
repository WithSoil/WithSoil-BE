package com.outthedoor.withsoil.api.ai.controller;

import com.outthedoor.withsoil.api.ai.dto.request.AiChatRequestDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatResponseDto;
import com.outthedoor.withsoil.api.ai.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}