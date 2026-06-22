package com.outthedoor.withsoil.api.ai.repository;

import com.outthedoor.withsoil.api.ai.entity.AiChat;
import com.outthedoor.withsoil.api.ai.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    List<AiChatMessage> findAllByAiChatOrderByMessageDateTimeAsc(AiChat aiChat);

    Optional<AiChatMessage> findFirstByAiChatOrderByMessageDateTimeDesc(AiChat aiChat);
}
