package com.outthedoor.withsoil.global.security.jwt;

import com.outthedoor.withsoil.api.member.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secretKey}") String secretKey,
            @Value("${jwt.access.expiration}") long accessTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    // Access Token 생성
    public String generateAccessToken(Long memberId, String email, Role role) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("email", email)
                .claim("role", role)
                .claim("type", "access_token")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 만료된 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("[JWT] 지원하지 않는 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("[JWT] 잘못된 형식의 토큰: {}", e.getMessage());
        } catch (SignatureException e) {             // ← 별도로 분리
            log.warn("[JWT] 서명이 위변조된 토큰: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] 유효하지 않은 토큰: {}", e.getMessage());
        }
        return false;
    }

    // 토큰 클레임 추출
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 토큰 서브젝트(MemberId) 추출
    public Optional<Long> getMemberIdFromToken(String token) {
        try {
            return Optional.of(Long.parseLong(getClaimsFromToken(token).getSubject()));
        } catch (Exception e) {
            log.error("[JWT] 토큰에서 MemberId 추출 실패 : {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    // 토큰 클레임에서 이메일 추출
    public Optional<String> getEmailFromToken(String token) {
        try {
            return Optional.ofNullable(getClaimsFromToken(token).get("email", String.class));
        } catch (Exception e) {
            log.error("[JWT] 토큰에서 이메일 추출 실패 : {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    // 토큰 클레임에서 권한 추출
    public Optional<String> getRoleFromToken(String token) {
        try {
            return Optional.ofNullable(getClaimsFromToken(token).get("role", String.class));
        } catch (Exception e) {
            log.error("[JWT] 토큰에서 권한 추출 실패 : {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}