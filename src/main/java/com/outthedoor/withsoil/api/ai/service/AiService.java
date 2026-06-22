package com.outthedoor.withsoil.api.ai.service;

import com.outthedoor.withsoil.api.ai.dto.request.AiChatRequestDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatDetailResponse;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatMessageResponse;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatResponseDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatSummaryResponse;
import com.outthedoor.withsoil.api.ai.dto.response.AiDiagnosisResponseDto;
import com.outthedoor.withsoil.api.ai.entity.AiChat;
import com.outthedoor.withsoil.api.ai.entity.AiChatMessage;
import com.outthedoor.withsoil.api.ai.entity.AiChatMessageRole;
import com.outthedoor.withsoil.api.ai.repository.AiChatMessageRepository;
import com.outthedoor.withsoil.api.ai.repository.AiChatRepository;
import com.outthedoor.withsoil.api.member.entity.Member;
import com.outthedoor.withsoil.global.exeption.BaseException;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final int CHAT_TITLE_MAX_LENGTH = 30;

    private final WebClient webClient;
    private final AiChatRepository aiChatRepository;
    private final AiChatMessageRepository aiChatMessageRepository;

    public AiService(
            @Value("${ai.server.base-url:http://localhost:8000}") String aiServerBaseUrl,
            AiChatRepository aiChatRepository,
            AiChatMessageRepository aiChatMessageRepository
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServerBaseUrl)
                .build();
        this.aiChatRepository = aiChatRepository;
        this.aiChatMessageRepository = aiChatMessageRepository;
    }

    @Transactional
    public AiChatResponseDto chatRag(Member member, AiChatRequestDto request) {
        AiChat aiChat = getOrCreateChat(member, request.chatId(), request.query());

        aiChatMessageRepository.save(AiChatMessage.create(aiChat, AiChatMessageRole.USER, request.query().trim()));

        AiChatResponseDto aiResponse = requestAiChat(request.query());
        validateAiChatResponse(aiResponse);

        AiChatMessage savedAiMessage = aiChatMessageRepository.save(
                AiChatMessage.create(aiChat, AiChatMessageRole.ASSISTANT, aiResponse.answer())
        );

        return AiChatResponseDto.of(aiChat, aiResponse.status(), aiResponse.answer(), savedAiMessage.getMessageDateTime());
    }

    @Transactional(readOnly = true)
    public List<AiChatSummaryResponse> getChatHistories(Member member) {
        return aiChatRepository.findAllByMemberAndIsDeletedFalseOrderByChatDateTimeDesc(member)
                .stream()
                .map(aiChat -> AiChatSummaryResponse.of(
                        aiChat,
                        aiChatMessageRepository.findFirstByAiChatOrderByMessageDateTimeDesc(aiChat).orElse(null)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public AiChatDetailResponse getChatHistory(Member member, Long chatId) {
        AiChat aiChat = getChat(member, chatId);
        List<AiChatMessageResponse> messages = aiChatMessageRepository.findAllByAiChatOrderByMessageDateTimeAsc(aiChat)
                .stream()
                .map(AiChatMessageResponse::from)
                .toList();
        return AiChatDetailResponse.of(aiChat, messages);
    }

    @Transactional
    public void deleteChat(Member member, Long chatId) {
        AiChat aiChat = getChat(member, chatId);
        aiChat.delete();
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

    private AiChatResponseDto requestAiChat(String query) {
        Map<String, String> body = Map.of("query", query.trim());

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

    private AiChat getOrCreateChat(Member member, Long chatId, String query) {
        if (chatId != null) {
            return getChat(member, chatId);
        }
        return aiChatRepository.save(AiChat.create(member, createTitle(query)));
    }

    private AiChat getChat(Member member, Long chatId) {
        return aiChatRepository.findByIdAndMemberAndIsDeletedFalse(chatId, member)
                .orElseThrow(() -> new BaseException(ErrorStatus.NOT_FOUND_AI_CHAT));
    }

    private String createTitle(String query) {
        String normalized = query.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= CHAT_TITLE_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, CHAT_TITLE_MAX_LENGTH) + "...";
    }

    private void validateAiChatResponse(AiChatResponseDto response) {
        if (response == null || !"success".equals(response.status()) || !StringUtils.hasText(response.answer())) {
            throw new BaseException(ErrorStatus.BAD_GATEWAY_AI_SERVER_ERROR);
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
