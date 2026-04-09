package br.com.projetoApi.Common.Exception;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.projetoApi.Config.RestErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<RestErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        return buildError(exception.getStatus(), exception.getMessage(), exception.getReason(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC));
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "VALIDATION_ERROR");
        body.put("message", "Falha de validacao na requisicao.");
        body.put("reason", details);
        body.put("path", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class, DisabledException.class })
    public ResponseEntity<RestErrorResponse> handleAuthenticationException(Exception exception, HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "Falha na autenticacao.", exception.getMessage(), request);
    }

    @ExceptionHandler({ AccessDeniedException.class, AuthorizationDeniedException.class })
    public ResponseEntity<RestErrorResponse> handleAccessDeniedException(Exception exception, HttpServletRequest request) {
        return buildError(
                HttpStatus.FORBIDDEN,
                "O usuario autenticado nao possui permissao para executar esta operacao.",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", exception.getMessage(), request);
    }

    private ResponseEntity<RestErrorResponse> buildError(
            HttpStatus status,
            String message,
            String reason,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(new RestErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                status.name(),
                message,
                reason,
                request.getRequestURI()));
    }
}
