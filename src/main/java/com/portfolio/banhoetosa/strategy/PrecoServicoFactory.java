package com.portfolio.banhoetosa.strategy;

import com.portfolio.banhoetosa.model.TipoServico;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PrecoServicoFactory {

    private final Map<TipoServico, PrecoServicoStrategy> strategies;

    public PrecoServicoFactory(List<PrecoServicoStrategy> strategies) {
        this.strategies = new EnumMap<>(TipoServico.class);
        strategies.forEach(strategy -> this.strategies.put(strategy.getTipoServico(), strategy));
    }

    public PrecoServicoStrategy obter(TipoServico tipoServico) {
        PrecoServicoStrategy strategy = strategies.get(tipoServico);
        if (strategy == null) {
            throw new IllegalArgumentException("Tipo de serviço não suportado: " + tipoServico);
        }
        return strategy;
    }
}
