package com.outthedoor.withsoil.api.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class ExpoPushService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://exp.host")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(256 * 1024))
            .build();

    public void send(String pushToken, String title, String body, String notificationType) {
        try {
            webClient.post()
                    .uri("/--/api/v2/push/send")
                    .bodyValue(Map.of(
                            "to", pushToken,
                            "title", title,
                            "body", body,
                            "sound", "default",
                            "priority", "high",
                            "channelId", "weather-alerts",
                            "data", Map.of("type", notificationType)
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
        } catch (Exception e) {
            log.warn("[Push] Expo 푸시 발송 실패 - tokenSuffix={}", tokenSuffix(pushToken), e);
        }
    }

    private String tokenSuffix(String pushToken) {
        return pushToken.length() <= 8 ? pushToken : pushToken.substring(pushToken.length() - 8);
    }
}
