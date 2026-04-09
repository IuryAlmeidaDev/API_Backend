package br.com.projetoApi.Config;

import java.time.OffsetDateTime;

public record RestErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String reason,
        String path) {
}
