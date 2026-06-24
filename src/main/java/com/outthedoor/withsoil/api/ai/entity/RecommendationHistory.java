package com.outthedoor.withsoil.api.ai.entity;

import com.outthedoor.withsoil.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "recommendation_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member와의 다대일(N:1) 단방향 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false, length = 500)
    private String purpose;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String responseJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public RecommendationHistory(Member member, String region, String purpose, String responseJson) {
        this.member = member;
        this.region = region;
        this.purpose = purpose;
        this.responseJson = responseJson;
    }
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}