package com.outthedoor.withsoil.api.ai.service;

import com.outthedoor.withsoil.api.ai.dto.response.AiChatResponseDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiDiagnosisResponseDto;
import com.outthedoor.withsoil.global.exeption.BaseException;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
public class AiService {

    private final WebClient webClient;

    public AiService(@Value("${ai.server.base-url:http://localhost:8000}") String aiServerBaseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServerBaseUrl)
                .build();
    }

    public AiChatResponseDto chatRag(String query) {
        Map<String, String> body = Map.of("query", query);

        try {
            return webClient.post()
                    .uri("/api/v1/rag/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(AiChatResponseDto.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new BaseException(ErrorStatus.BAD_GATEWAY_AI_SERVER_UNAVAILABLE, e);
        } catch (WebClientResponseException e) {
            throw mapAiServerResponseException(e);
        }
    }

    public AiDiagnosisResponseDto diagnoseCrop(String cropName, MultipartFile file, int topk) {
        validateDiagnosisRequest(cropName, file, topk);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("crop_name", cropName.trim());
        builder.part("topk", topk);
        builder.part("file", file.getResource())
                .filename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg")
                .contentType(MediaType.parseMediaType(file.getContentType()));

        try {
            return webClient.post()
                    .uri("/api/v1/ai/diagnose")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(AiDiagnosisResponseDto.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new BaseException(ErrorStatus.BAD_GATEWAY_AI_SERVER_UNAVAILABLE, e);
        } catch (WebClientResponseException e) {
            throw mapAiServerResponseException(e);
        }
    }

    private void validateDiagnosisRequest(String cropName, MultipartFile file, int topk) {
        if (!StringUtils.hasText(cropName) || topk < 1 || topk > 10) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_AI_REQUEST);
        }

        if (file == null || file.isEmpty()) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_AI_IMAGE);
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_AI_IMAGE);
        }
    }

    private BaseException mapAiServerResponseException(WebClientResponseException e) {
        if (e.getStatusCode().is4xxClientError()) {
            return new BaseException(ErrorStatus.BAD_REQUEST_INVALID_AI_REQUEST, e);
        }
        return new BaseException(ErrorStatus.BAD_GATEWAY_AI_SERVER_ERROR, e);
    }
}
