package com.portfolio.banhoetosa.dto;

import com.portfolio.banhoetosa.model.TipoServico;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoRequest(
        @NotNull Long clienteId,
        @NotNull Long petId,
        @NotNull TipoServico tipoServico,
        @NotNull @Future LocalDateTime dataHora
) {
}
