package com.portfolio.banhoetosa.strategy;

import com.portfolio.banhoetosa.model.Pet;
import com.portfolio.banhoetosa.model.TipoServico;

import java.math.BigDecimal;

public interface PrecoServicoStrategy {
    TipoServico getTipoServico();
    BigDecimal calcular(Pet pet);
}
