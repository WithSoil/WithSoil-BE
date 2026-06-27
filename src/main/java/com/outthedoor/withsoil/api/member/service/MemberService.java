package com.outthedoor.withsoil.api.member.service;

import com.outthedoor.withsoil.api.ai.entity.RecommendationHistory;
import com.outthedoor.withsoil.api.ai.repository.RecommendationHistoryRepository;
import com.outthedoor.withsoil.api.member.dto.request.MemberLocationRequest;
import com.outthedoor.withsoil.api.member.dto.request.MemberPushTokenRequest;
import com.outthedoor.withsoil.api.member.dto.request.MemberLoginRequest;
import com.outthedoor.withsoil.api.member.dto.request.MemberSignupRequest;
import com.outthedoor.withsoil.api.member.dto.response.MemberLocationResponse;
import com.outthedoor.withsoil.api.member.dto.response.MemberLoginResponse;
import com.outthedoor.withsoil.api.member.dto.response.MemberMypageResponse;
import com.outthedoor.withsoil.api.member.dto.response.MemberSignupResponse;
import com.outthedoor.withsoil.api.member.entity.Member;
import com.outthedoor.withsoil.api.member.repository.MemberRepository;
import com.outthedoor.withsoil.global.exeption.BaseException;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import com.outthedoor.withsoil.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RecommendationHistoryRepository recommendationHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입
    public MemberSignupResponse signup(MemberSignupRequest requestDTO) {

        if (memberRepository.existsByEmailAndIsDeleted(requestDTO.email(), false)) {
            throw new BaseException(ErrorStatus.CONFLICT_DUPLICATE_EMAIL);
        }

        Member member = Member.createMember(
                requestDTO.email(),
                passwordEncoder.encode(requestDTO.password()),
                requestDTO.name()
        );

        Member saved = memberRepository.save(member);

        log.info("[Member] 회원가입 성공 - email: {}", saved.getEmail());

        return MemberSignupResponse.from(saved);
    }

    public MemberLocationResponse updateLocation(Member member, MemberLocationRequest requestDTO) {
        Member foundMember = memberRepository.findByIdAndIsDeleted(member.getId(), false)
                .orElseThrow(() -> new BaseException(ErrorStatus.NOT_FOUND_USER));
        foundMember.updateLocation(requestDTO.toEntity());

        log.info("[Member] 위치 정보 수정 성공 - email: {}", foundMember.getEmail());

        return MemberLocationResponse.from(foundMember.getLocation());
    }

    public void updatePushToken(Member member, MemberPushTokenRequest requestDTO) {
        Member foundMember = memberRepository.findByIdAndIsDeleted(member.getId(), false)
                .orElseThrow(() -> new BaseException(ErrorStatus.NOT_FOUND_USER));
        foundMember.updatePushToken(requestDTO.pushToken());
        log.info("[Member] 푸시 토큰 등록 성공 - email: {}", foundMember.getEmail());
    }

    // 로그인
    @Transactional(readOnly = true)
    public MemberLoginResponse login(MemberLoginRequest requestDTO) {

        Member member = memberRepository.findByEmailAndIsDeleted(requestDTO.email(), false)
                .orElseThrow(() -> new BaseException(ErrorStatus.UNAUTHORIZED_INVALID_LOGIN));

        if (!passwordEncoder.matches(requestDTO.password(), member.getPassword())) {
            throw new BaseException(ErrorStatus.UNAUTHORIZED_INVALID_LOGIN);
        }

        String accessToken = jwtProvider.generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole()
        );

        log.info("[Member] 로그인 성공 - email: {}", member.getEmail());

        return MemberLoginResponse.of(accessToken);
    }

    // 내 정보 조회(마이페이지)
    @Transactional(readOnly = true)
    public MemberMypageResponse getMypage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        RecommendationHistory latestHistory = recommendationHistoryRepository
                .findFirstByMemberOrderByCreatedAtDesc(member)
                .orElse(null);

        String recommendationJson = (latestHistory != null) ? latestHistory.getResponseJson() : null;

        return MemberMypageResponse.from(member, recommendationJson);
    }
}
