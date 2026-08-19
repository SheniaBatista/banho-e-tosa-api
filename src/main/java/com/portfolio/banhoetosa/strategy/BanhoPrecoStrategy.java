package com.portfolio.banhoetosa.strategy;

import com.portfolio.banhoetosa.model.Pet;
import com.portfolio.banhoetosa.model.TipoServico;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BanhoPrecoStrategy implements PrecoServicoStrategy {

    @Override
    public TipoServico getTipoServico() {
        return TipoServico.BANHO;
    }

    @Override
    public BigDecimal calcular(Pet pet) {
        return BigDecimal.valueOf(pet.getPesoKg() <= 10 ? 45.00 : 60.00);
    }
}
