package com.portfolio.banhoetosa;

import com.portfolio.banhoetosa.dto.AgendamentoRequest;
import com.portfolio.banhoetosa.dto.PetRequest;
import com.portfolio.banhoetosa.exception.RegraNegocioException;
import com.portfolio.banhoetosa.facade.AgendamentoFacade;
import com.portfolio.banhoetosa.model.Agendamento;
import com.portfolio.banhoetosa.model.Cliente;
import com.portfolio.banhoetosa.model.Pet;
import com.portfolio.banhoetosa.model.StatusAgendamento;
import com.portfolio.banhoetosa.model.TipoServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class BanhoETosaApiApplicationTests {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private AgendamentoFacade facade;

    @Test
    @DisplayName("o contexto da aplicação sobe com o PostgreSQL configurado")
    void contextLoads() {
        assertThat(facade).isNotNull();
        assertThat(POSTGRES.isRunning()).isTrue();
    }

    @Test
    @DisplayName("percorre o fluxo cliente -> pet -> agendamento -> conclusão")
    void fluxoCompletoPersisteNoBanco() {
        Cliente ana = facade.cadastrarCliente(
                new Cliente("Ana Souza", "11999999999", "ana@email.com"));
        assertThat(ana.getId()).isNotNull();

        Pet thor = facade.cadastrarPet(
                new PetRequest("Thor", "Cachorro", "Shih-tzu", 7.5, ana.getId()));
        assertThat(thor.getId()).isNotNull();
        assertThat(thor.getTutor().getId()).isEqualTo(ana.getId());

        Agendamento agendamento = facade.criarAgendamento(new AgendamentoRequest(
                ana.getId(), thor.getId(), TipoServico.BANHO_E_TOSA,
                LocalDateTime.now().plusDays(3)));

        assertThat(agendamento.getId()).isNotNull();
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(agendamento.getValor()).isEqualByComparingTo(new BigDecimal("85.00"));

        Agendamento concluido = facade.concluirAgendamento(agendamento.getId());
        assertThat(concluido.getStatus()).isEqualTo(StatusAgendamento.CONCLUIDO);

        assertThat(facade.listarAgendamentos())
                .extracting(Agendamento::getId)
                .contains(agendamento.getId());
    }

    @Test
    @DisplayName("a regra de tutor é aplicada com os dados vindos do banco")
    void impedeAgendarPetDeOutroTutor() {
        Cliente ana = facade.cadastrarCliente(
                new Cliente("Ana Souza", "11999999999", "ana@email.com"));
        Cliente bruno = facade.cadastrarCliente(
                new Cliente("Bruno Lima", "11888888888", "bruno@email.com"));

        Pet petDaAna = facade.cadastrarPet(
                new PetRequest("Thor", "Cachorro", null, 7.5, ana.getId()));

        assertThatThrownBy(() -> facade.criarAgendamento(new AgendamentoRequest(
                bruno.getId(), petDaAna.getId(), TipoServico.BANHO,
                LocalDateTime.now().plusDays(1))))
                .isInstanceOf(RegraNegocioException.class);
    }
}

