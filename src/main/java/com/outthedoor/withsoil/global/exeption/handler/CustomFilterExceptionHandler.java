package com.outthedoor.withsoil.global.exeption.handler;

import com.outthedoor.withsoil.global.response.ApiResponse;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomFilterExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /// 인증 실패 시 핸들링 (`401`, ex. 토큰이 유효하지 않거나 없는 경우 등)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        sendErrorResponse(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                ErrorStatus.UNAUTHORIZED_ACCESS.getMessage()
        );
    }

    /// 인가 실패 시 핸들링 (`403`, ex. 권한 없음)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        sendErrorResponse(
                response,
                HttpServletResponse.SC_FORBIDDEN,
                ErrorStatus.FORBIDDEN_ACCESS_DENIED.getMessage()
        );
    }

    private void sendErrorResponse(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = ApiResponse.fail(status, message);
        String json = objectMapper.writeValueAsString(apiResponse);

        response.getWriter().write(json);
    }
}