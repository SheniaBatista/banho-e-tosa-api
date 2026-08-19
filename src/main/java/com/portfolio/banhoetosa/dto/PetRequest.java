package com.portfolio.banhoetosa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PetRequest(
        @NotBlank String nome,
        @NotBlank String especie,
        String raca,
        @NotNull @Positive Double pesoKg,
        @NotNull Long clienteId
) {
}
