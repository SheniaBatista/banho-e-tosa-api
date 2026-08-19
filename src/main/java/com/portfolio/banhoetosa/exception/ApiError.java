package com.portfolio.banhoetosa.exception;

import java.time.LocalDateTime;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem
) {
}
