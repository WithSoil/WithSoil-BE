package com.outthedoor.withsoil.api.ai.service;

import com.outthedoor.withsoil.api.ai.dto.response.AiChatResponseDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class AiService {

    private final WebClient webClient;

    public AiService() {
        this.webClient = WebClient.create("http://localhost:8000");
    }

    public AiChatResponseDto chatRag(String query) {
        Map<String, String> body = Map.of("query", query);

        return webClient.post()
                .uri("/api/v1/rag/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AiChatResponseDto.class)
                .block();
    }

}