package com.portfolio.banhoetosa.strategy;

import com.portfolio.banhoetosa.model.Pet;
import com.portfolio.banhoetosa.model.TipoServico;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TosaPrecoStrategy implements PrecoServicoStrategy {

    @Override
    public TipoServico getTipoServico() {
        return TipoServico.TOSA;
    }

    @Override
    public BigDecimal calcular(Pet pet) {
        return BigDecimal.valueOf(pet.getPesoKg() <= 10 ? 55.00 : 75.00);
    }
}
