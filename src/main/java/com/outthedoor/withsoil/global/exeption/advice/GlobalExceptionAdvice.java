package com.outthedoor.withsoil.global.exeption.advice;

import com.outthedoor.withsoil.global.exeption.BaseException;
import com.outthedoor.withsoil.global.response.ApiResponse;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {

        return ResponseEntity
                .status(e.getStatusCode())
                .body(ApiResponse.failOnly(e.getErrorStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {

        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail(500, ErrorStatus.INTERNAL_SERVER_ERROR.getMessage()));
    }
}