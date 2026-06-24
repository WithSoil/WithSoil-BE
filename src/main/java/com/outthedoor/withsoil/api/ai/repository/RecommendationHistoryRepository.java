// RecommendationHistoryRepository.java
package com.outthedoor.withsoil.api.ai.repository;

import com.outthedoor.withsoil.api.ai.entity.RecommendationHistory;
import com.outthedoor.withsoil.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    // 나중에 마이페이지에서 이력 조회할 때 사용
    List<RecommendationHistory> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
    Optional<RecommendationHistory> findFirstByMemberOrderByCreatedAtDesc(Member member);
}