package com.outthedoor.withsoil.global.security.jwt;

import com.outthedoor.withsoil.global.security.entity.CustomUserDetails;
import com.outthedoor.withsoil.global.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization 헤더에서 Bearer 토큰 추출
        String token = resolveToken(request);

        if (token != null && jwtProvider.validateToken(token)) {

            // 2. 토큰에서 이메일 추출
            String email = jwtProvider.getEmailFromToken(token)
                    .orElse(null);

            if (email != null) {

                // 3. DB 조회하여 인증
                CustomUserDetails userDatils = (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);

                // 4. 인증 완료 후 password 제거
                userDatils.eraseCredentials();

                // 5. Authentication 생성
                //      - principal: CustomUserDetails
                //      - credentials: null
                //      - authorities: DB에서 꺼낸 Role 기반 GrantedAuthority (ex. ROLE_USER)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDatils,
                        null,
                        userDatils.getAuthorities()
                );

                // 6. 요청 정보(IP 등) 추가
                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // 7. SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("[JWT] 인증 성공 - email: {}", email);
            }
        }

        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 "Bearer " 제거 후 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}