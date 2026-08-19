package com.portfolio.banhoetosa.strategy;

import com.portfolio.banhoetosa.model.TipoServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrecoServicoFactoryTest {
    @Test
    @DisplayName("devolve a estratégia correspondente ao tipo de serviço pedido")
    void devolveEstrategiaCorrespondente() {
        BanhoPrecoStrategy banho = new BanhoPrecoStrategy();
        TosaPrecoStrategy tosa = new TosaPrecoStrategy();
        PrecoServicoFactory factory = new PrecoServicoFactory(List.of(banho, tosa));

        assertThat(factory.obter(TipoServico.BANHO)).isSameAs(banho);
        assertThat(factory.obter(TipoServico.TOSA)).isSameAs(tosa);
    }

    @Test
    @DisplayName("recusa um tipo de serviço sem estratégia registrada")
    void recusaTipoSemEstrategia() {
        PrecoServicoFactory factory = new PrecoServicoFactory(List.of(new BanhoPrecoStrategy()));

        assertThatThrownBy(() -> factory.obter(TipoServico.BANHO_E_TOSA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BANHO_E_TOSA");
    }
}

