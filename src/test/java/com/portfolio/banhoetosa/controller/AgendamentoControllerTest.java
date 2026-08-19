package com.portfolio.banhoetosa.controller;

import com.portfolio.banhoetosa.dto.AgendamentoRequest;
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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
class AgendamentoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoFacade facade;

    private static Agendamento agendamento(StatusAgendamento status) {
        Cliente tutor = new Cliente("Ana Souza", "11999999999", "ana@email.com");
        tutor.setId(1L);

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Thor");
        pet.setPesoKg(7.5);
        pet.setTutor(tutor);

        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setCliente(tutor);
        agendamento.setPet(pet);
        agendamento.setTipoServico(TipoServico.BANHO_E_TOSA);
        agendamento.setDataHora(LocalDateTime.of(2099, 1, 20, 14, 0));
        agendamento.setValor(new BigDecimal("85.00"));
        agendamento.setStatus(status);
        return agendamento;
    }

    @Test
    @DisplayName("POST /agendamentos devolve 201 com o valor calculado pelo servidor")
    void criaAgendamento() throws Exception {
        when(facade.criarAgendamento(any(AgendamentoRequest.class)))
                .thenReturn(agendamento(StatusAgendamento.AGENDADO));

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": 1,
                                  "petId": 1,
                                  "tipoServico": "BANHO_E_TOSA",
                                  "dataHora": "2099-01-20T14:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valor").value(85.00))
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.pet.nome").value("Thor"));
    }

    @Test
    @DisplayName("POST /agendamentos rejeita data no passado com 400")
    void rejeitaDataNoPassado() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": 1,
                                  "petId": 1,
                                  "tipoServico": "BANHO",
                                  "dataHora": "2020-01-20T14:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("dataHora")));

        verify(facade, never()).criarAgendamento(any());
    }

    @Test
    @DisplayName("POST /agendamentos rejeita requisição sem o tipo de serviço com 400")
    void rejeitaRequisicaoIncompleta() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": 1,
                                  "petId": 1,
                                  "dataHora": "2099-01-20T14:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(facade, never()).criarAgendamento(any());
    }

    @Test
    @DisplayName("GET /agendamentos lista os agendamentos")
    void listaAgendamentos() throws Exception {
        when(facade.listarAgendamentos())
                .thenReturn(List.of(agendamento(StatusAgendamento.AGENDADO)));

        mockMvc.perform(get("/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].cliente.nome").value("Ana Souza"));
    }

    @Test
    @DisplayName("PATCH /agendamentos/{id}/concluir devolve o agendamento concluído")
    void concluiAgendamento() throws Exception {
        when(facade.concluirAgendamento(1L))
                .thenReturn(agendamento(StatusAgendamento.CONCLUIDO));

        mockMvc.perform(patch("/agendamentos/1/concluir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    @DisplayName("PATCH /agendamentos/{id}/cancelar devolve 400 quando a regra de status impede")
    void informaRegraDeNegocioAoCancelar() throws Exception {
        when(facade.cancelarAgendamento(1L)).thenThrow(new RegraNegocioException(
                "Somente agendamentos com status AGENDADO podem ser cancelados."));

        mockMvc.perform(patch("/agendamentos/1/cancelar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(
                        "Somente agendamentos com status AGENDADO podem ser cancelados."));
    }
}

