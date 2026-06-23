package com.outthedoor.withsoil.api.ai.repository;

import com.outthedoor.withsoil.api.ai.entity.AiChat;
import com.outthedoor.withsoil.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiChatRepository extends JpaRepository<AiChat, Long> {

    Optional<AiChat> findByIdAndMemberAndIsDeletedFalse(Long id, Member member);

    List<AiChat> findAllByMemberAndIsDeletedFalseOrderByChatDateTimeDesc(Member member);
}
