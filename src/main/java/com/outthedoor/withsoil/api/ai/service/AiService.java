package com.outthedoor.withsoil.api.ai.service;

import com.outthedoor.withsoil.api.ai.dto.response.AiChatResponseDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiDiagnosisResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
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

    public AiDiagnosisResponseDto diagnoseCrop(String cropName, MultipartFile file, int topk) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("crop_name", cropName);
        builder.part("topk", topk);

        builder.part("file", file.getResource())
                .filename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");

        return webClient.post()
                .uri("/api/v1/ai/diagnose")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(AiDiagnosisResponseDto.class)
                .block();
    }
}