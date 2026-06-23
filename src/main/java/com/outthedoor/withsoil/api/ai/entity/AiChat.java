package com.outthedoor.withsoil.api.ai.entity;

import com.outthedoor.withsoil.api.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(
        name = "ai_chat",
        indexes = {
                @Index(name = "idx_ai_chat_member_datetime", columnList = "member_id, chat_date_time")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "chat_date_time", nullable = false)
    private LocalDateTime chatDateTime;

    @Column(nullable = false, length = 100)
    private String title;

    @OneToMany(mappedBy = "aiChat")
    private List<AiChatMessage> messages = new ArrayList<>();

    @Column(nullable = false)
    private boolean isDeleted;

    private LocalDateTime deletedAt;

    public static AiChat create(Member member, String title) {
        AiChat aiChat = new AiChat();
        aiChat.member = member;
        aiChat.chatDateTime = LocalDateTime.now();
        aiChat.title = title;
        aiChat.isDeleted = false;
        return aiChat;
    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
