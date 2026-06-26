package com.outthedoor.withsoil.api.ai.service;

import com.outthedoor.withsoil.api.ai.dto.request.AiChatRequestDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatDetailResponse;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatMessageResponse;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatResponseDto;
import com.outthedoor.withsoil.api.ai.dto.response.AiChatSummaryResponse;
import com.outthedoor.withsoil.api.ai.dto.response.AiDiagnosisGuideResponse;
import com.outthedoor.withsoil.api.ai.dto.response.AiDiagnosisResponseDto;
import com.outthedoor.withsoil.api.ai.entity.AiChat;
import com.outthedoor.withsoil.api.ai.entity.AiChatMessage;
import com.outthedoor.withsoil.api.ai.entity.AiChatMessageRole;
import com.outthedoor.withsoil.api.ai.entity.DiseaseGuide;
import com.outthedoor.withsoil.api.ai.repository.AiChatMessageRepository;
import com.outthedoor.withsoil.api.ai.repository.AiChatRepository;
import com.outthedoor.withsoil.api.ai.repository.DiseaseGuideRepository;
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
    private static final int CHAT_QUERY_MAX_LENGTH = 500;

    private final WebClient webClient;
    private final AiChatRepository aiChatRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final DiseaseGuideRepository diseaseGuideRepository;

    public AiService(
            @Value("${ai.server.base-url:http://localhost:8000}") String aiServerBaseUrl,
            AiChatRepository aiChatRepository,
            AiChatMessageRepository aiChatMessageRepository,
            DiseaseGuideRepository diseaseGuideRepository
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServerBaseUrl)
                .build();
        this.aiChatRepository = aiChatRepository;
        this.aiChatMessageRepository = aiChatMessageRepository;
        this.diseaseGuideRepository = diseaseGuideRepository;
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

    @Transactional
    public AiChatResponseDto chatRagWithImage(Member member, Long chatId, String query, MultipartFile file) {
        validateChatQuery(query);
        validateImageFile(file);

        AiChat aiChat = getOrCreateChat(member, chatId, query);
        String userMessage = query.trim() + "\n\n[이미지 첨부: " + getOriginalFilename(file) + "]";

        aiChatMessageRepository.save(AiChatMessage.create(aiChat, AiChatMessageRole.USER, userMessage));

        AiChatResponseDto aiResponse = requestAiChatWithImage(query, file);
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
            AiDiagnosisResponseDto response = webClient.post()
                    .uri("/api/v1/ai/diagnose")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(AiDiagnosisResponseDto.class)
                    .block();
            return appendDiagnosisGuide(cropName, response);
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

    private AiChatResponseDto requestAiChatWithImage(String query, MultipartFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("query", query.trim());
        builder.part("mode", "general");
        builder.part("file", file.getResource())
                .filename(getOriginalFilename(file))
                .contentType(MediaType.parseMediaType(file.getContentType()));

        try {
            return webClient.post()
                    .uri("/api/v1/rag/chat/image")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
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

    private void validateChatQuery(String query) {
        if (!StringUtils.hasText(query) || query.trim().length() > CHAT_QUERY_MAX_LENGTH) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_AI_REQUEST);
        }
    }

    private void validateDiagnosisRequest(String cropName, MultipartFile file, int topk) {
        if (!StringUtils.hasText(cropName) || topk < 1 || topk > 10) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_AI_REQUEST);
        }

        validateImageFile(file);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_AI_IMAGE);
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_AI_IMAGE);
        }
    }

    private String getOriginalFilename(MultipartFile file) {
        return StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "image.jpg";
    }

    private AiDiagnosisResponseDto appendDiagnosisGuide(String requestedCropName, AiDiagnosisResponseDto response) {
        if (response == null) {
            throw new BaseException(ErrorStatus.BAD_GATEWAY_AI_SERVER_ERROR);
        }

        String cropName = StringUtils.hasText(response.crop()) ? response.crop().trim() : requestedCropName.trim();
        String diseaseName = resolveDiseaseName(cropName, response);

        return diseaseGuideRepository.findByCropNameNormalizedAndDiseaseNameNormalized(
                        DiseaseGuide.normalize(cropName),
                        DiseaseGuide.normalize(diseaseName)
                )
                .map(AiDiagnosisGuideResponse::from)
                .map(response::withGuide)
                .orElse(response.withGuide(null));
    }

    private String resolveDiseaseName(String cropName, AiDiagnosisResponseDto response) {
        if (isNormalDiagnosis(response)) {
            return "정상";
        }

        if (!StringUtils.hasText(response.diagnosis())) {
            return "";
        }

        String diseaseName = response.diagnosis().trim();
        for (String prefix : List.of(cropName + "_", cropName + " ", cropName + "-", cropName)) {
            if (diseaseName.startsWith(prefix)) {
                diseaseName = diseaseName.substring(prefix.length()).trim();
                break;
            }
        }
        return diseaseName;
    }

    private boolean isNormalDiagnosis(AiDiagnosisResponseDto response) {
        String resultType = response.resultType();
        String diagnosis = response.diagnosis();
        return (StringUtils.hasText(resultType) && List.of("normal", "healthy").contains(resultType.trim().toLowerCase()))
                || (StringUtils.hasText(diagnosis) && diagnosis.contains("정상"));
    }

    private BaseException mapAiServerResponseException(WebClientResponseException e) {
        if (e.getStatusCode().is4xxClientError()) {
            return new BaseException(ErrorStatus.BAD_REQUEST_INVALID_AI_REQUEST, e);
        }
        return new BaseException(ErrorStatus.BAD_GATEWAY_AI_SERVER_ERROR, e);
    }
}
