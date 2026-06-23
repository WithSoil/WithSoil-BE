package com.outthedoor.withsoil.global.exeption.advice;

import com.outthedoor.withsoil.global.exeption.BaseException;
import com.outthedoor.withsoil.global.response.ApiResponse;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {

        return ResponseEntity
                .status(e.getStatusCode())
                .body(ApiResponse.failOnly(e.getErrorStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(ErrorStatus.BAD_REQUEST_INVALID_INPUT.getMessage());

        return ResponseEntity
                .status(ErrorStatus.BAD_REQUEST_INVALID_INPUT.getStatusCode())
                .body(ApiResponse.fail(ErrorStatus.BAD_REQUEST_INVALID_INPUT.getStatusCode(), message));
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return ResponseEntity
                .status(ErrorStatus.PAYLOAD_TOO_LARGE_UPLOAD_FILE.getStatusCode())
                .body(ApiResponse.failOnly(ErrorStatus.PAYLOAD_TOO_LARGE_UPLOAD_FILE));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        String message = "file".equals(e.getRequestPartName())
                ? ErrorStatus.BAD_REQUEST_INVALID_AI_IMAGE.getMessage()
                : ErrorStatus.BAD_REQUEST_INVALID_INPUT.getMessage();

        return ResponseEntity
                .status(ErrorStatus.BAD_REQUEST_INVALID_INPUT.getStatusCode())
                .body(ApiResponse.fail(ErrorStatus.BAD_REQUEST_INVALID_INPUT.getStatusCode(), message));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.fail(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "지원하지 않는 요청 형식입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {

        log.error("[Exception] 처리되지 않은 예외가 발생했습니다.", e);

        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail(500, ErrorStatus.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
