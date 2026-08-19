package com.portfolio.banhoetosa.strategy;

import com.portfolio.banhoetosa.model.Pet;
import com.portfolio.banhoetosa.model.TipoServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrecoServicoStrategyTest {
    private final PrecoServicoFactory factory = new PrecoServicoFactory(List.of(
            new BanhoPrecoStrategy(),
            new TosaPrecoStrategy(),
            new BanhoETosaPrecoStrategy()));

    private static Pet petComPeso(double pesoKg) {
        Pet pet = new Pet();
        pet.setNome("Thor");
        pet.setPesoKg(pesoKg);
        return pet;
    }

    @ParameterizedTest(name = "{0} para pet de {1} kg custa R$ {2}")
    @CsvSource({
            "BANHO,          1.0,  45.00",
            "BANHO,          10.0, 45.00",
            "TOSA,           1.0,  55.00",
            "TOSA,           10.0, 55.00",
            "BANHO_E_TOSA,   1.0,  85.00",
            "BANHO_E_TOSA,   10.0, 85.00",
            "BANHO,          10.1, 60.00",
            "BANHO,          32.0, 60.00",
            "TOSA,           10.1, 75.00",
            "TOSA,           32.0, 75.00",
            "BANHO_E_TOSA,   10.1, 115.00",
            "BANHO_E_TOSA,   32.0, 115.00"
    })
    void calculaPrecoConformeServicoEFaixaDePeso(TipoServico tipoServico,
                                                 double pesoKg,
                                                 BigDecimal esperado) {
        BigDecimal valor = factory.obter(tipoServico).calcular(petComPeso(pesoKg));

        assertThat(valor).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("cada estratégia se identifica com o próprio tipo de serviço")
    void cadaEstrategiaConheceSeuTipoDeServico() {
        assertThat(new BanhoPrecoStrategy().getTipoServico()).isEqualTo(TipoServico.BANHO);
        assertThat(new TosaPrecoStrategy().getTipoServico()).isEqualTo(TipoServico.TOSA);
        assertThat(new BanhoETosaPrecoStrategy().getTipoServico()).isEqualTo(TipoServico.BANHO_E_TOSA);
    }

    @Test
    @DisplayName("existe uma estratégia registrada para todo tipo de serviço do enum")
    void cobreTodosOsTiposDeServico() {
        for (TipoServico tipoServico : TipoServico.values()) {
            assertThat(factory.obter(tipoServico))
                    .as("estratégia para %s", tipoServico)
                    .isNotNull();
        }
    }
}

