package com.outthedoor.withsoil.api.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "ai_chat_message",
        indexes = {
                @Index(name = "idx_ai_chat_message_chat_datetime", columnList = "ai_chat_id, message_date_time")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_chat_id", nullable = false)
    private AiChat aiChat;

    @Column(name = "message_date_time", nullable = false)
    private LocalDateTime messageDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiChatMessageRole role;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public static AiChatMessage create(AiChat aiChat, AiChatMessageRole role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.aiChat = aiChat;
        message.messageDateTime = LocalDateTime.now();
        message.role = role;
        message.content = content;
        return message;
    }
}
