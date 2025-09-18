package dev.shiwa.jwtstarter.demo;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.shiwa.jwtstarter.core.error.JwtAuthException;

@RestControllerAdvice
public class JwtExceptionHandler {

    @ExceptionHandler(JwtAuthException.class)
    public ResponseEntity<Map<String, Object>> handleJwtAuthException(JwtAuthException ex) {

	HttpStatus status = switch (ex.getErrorCode()) {
	case EXPIRED_TOKEN, INVALID_TOKEN, INVALID_TOKEN_TYPE, REFRESH_REUSE_DETECTED -> HttpStatus.UNAUTHORIZED;
	case REFRESH_DISABLED -> HttpStatus.BAD_REQUEST;
	default -> HttpStatus.INTERNAL_SERVER_ERROR;
	};

	Map<String, Object> body = Map.of("error", ex.getErrorCode().name(), "message", ex.getMessage());

	return ResponseEntity.status(status).body(body);
    }
}