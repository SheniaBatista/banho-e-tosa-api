package com.portfolio.banhoetosa.controller;

import com.portfolio.banhoetosa.dto.PetRequest;
import com.portfolio.banhoetosa.exception.RecursoNaoEncontradoException;
import com.portfolio.banhoetosa.facade.AgendamentoFacade;
import com.portfolio.banhoetosa.model.Cliente;
import com.portfolio.banhoetosa.model.Pet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
class PetControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoFacade facade;

    private static Pet petSalvo() {
        Cliente tutor = new Cliente("Ana Souza", "11999999999", "ana@email.com");
        tutor.setId(1L);

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Thor");
        pet.setEspecie("Cachorro");
        pet.setRaca("Shih-tzu");
        pet.setPesoKg(7.5);
        pet.setTutor(tutor);
        return pet;
    }

    @Test
    @DisplayName("POST /pets devolve 201 com o pet e seu tutor")
    void cadastraPet() throws Exception {
        when(facade.cadastrarPet(any(PetRequest.class))).thenReturn(petSalvo());

        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Thor",
                                  "especie": "Cachorro",
                                  "raca": "Shih-tzu",
                                  "pesoKg": 7.5,
                                  "clienteId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Thor"))
                .andExpect(jsonPath("$.tutor.nome").value("Ana Souza"));
    }

    @Test
    @DisplayName("POST /pets rejeita peso zero ou negativo com 400")
    void rejeitaPesoInvalido() throws Exception {
        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Thor",
                                  "especie": "Cachorro",
                                  "pesoKg": -1,
                                  "clienteId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(facade, never()).cadastrarPet(any());
    }

    @Test
    @DisplayName("POST /pets rejeita requisição sem tutor com 400")
    void rejeitaPetSemTutor() throws Exception {
        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Thor",
                                  "especie": "Cachorro",
                                  "pesoKg": 7.5
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(facade, never()).cadastrarPet(any());
    }

    @Test
    @DisplayName("DELETE /pets/{id} devolve 204 quando o pet existe")
    void excluiPet() throws Exception {
        doNothing().when(facade).excluirPet(1L);

        mockMvc.perform(delete("/pets/1"))
                .andExpect(status().isNoContent());

        verify(facade).excluirPet(1L);
    }

    @Test
    @DisplayName("DELETE /pets/{id} devolve 404 quando o pet não existe")
    void informaPetInexistenteAoExcluir() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Pet não encontrado: 99"))
                .when(facade).excluirPet(99L);

        mockMvc.perform(delete("/pets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Pet não encontrado: 99"));
    }
}

