package com.portfolio.banhoetosa.controller;

import com.portfolio.banhoetosa.facade.AgendamentoFacade;
import com.portfolio.banhoetosa.model.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoFacade facade;

    private static Cliente clienteSalvo() {
        Cliente cliente = new Cliente("Ana Souza", "11999999999", "ana@email.com");
        cliente.setId(1L);
        return cliente;
    }

    @Test
    @DisplayName("POST /clientes devolve 201 com o cliente criado")
    void cadastraCliente() throws Exception {
        when(facade.cadastrarCliente(any(Cliente.class))).thenReturn(clienteSalvo());

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Souza",
                                  "telefone": "11999999999",
                                  "email": "ana@email.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Souza"));
    }

    @Test
    @DisplayName("POST /clientes rejeita nome em branco com 400")
    void rejeitaNomeEmBranco() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "  ",
                                  "telefone": "11999999999",
                                  "email": "ana@email.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("nome")));

        verify(facade, never()).cadastrarCliente(any());
    }

    @Test
    @DisplayName("POST /clientes rejeita e-mail inválido com 400")
    void rejeitaEmailInvalido() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Souza",
                                  "telefone": "11999999999",
                                  "email": "nao-e-um-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("email")));

        verify(facade, never()).cadastrarCliente(any());
    }

    @Test
    @DisplayName("GET /clientes lista os clientes cadastrados")
    void listaClientes() throws Exception {
        when(facade.listarClientes()).thenReturn(List.of(clienteSalvo()));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nome").value("Ana Souza"));
    }
}

