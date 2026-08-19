package com.portfolio.banhoetosa.facade;

import com.portfolio.banhoetosa.dto.AgendamentoRequest;
import com.portfolio.banhoetosa.dto.PetRequest;
import com.portfolio.banhoetosa.exception.RecursoNaoEncontradoException;
import com.portfolio.banhoetosa.exception.RegraNegocioException;
import com.portfolio.banhoetosa.model.Agendamento;
import com.portfolio.banhoetosa.model.Cliente;
import com.portfolio.banhoetosa.model.Pet;
import com.portfolio.banhoetosa.model.StatusAgendamento;
import com.portfolio.banhoetosa.model.TipoServico;
import com.portfolio.banhoetosa.repository.AgendamentoRepository;
import com.portfolio.banhoetosa.repository.ClienteRepository;
import com.portfolio.banhoetosa.repository.PetRepository;
import com.portfolio.banhoetosa.strategy.BanhoETosaPrecoStrategy;
import com.portfolio.banhoetosa.strategy.BanhoPrecoStrategy;
import com.portfolio.banhoetosa.strategy.PrecoServicoFactory;
import com.portfolio.banhoetosa.strategy.TosaPrecoStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoFacadeTest {
    private static final LocalDateTime AMANHA = LocalDateTime.now().plusDays(1);

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private AgendamentoRepository agendamentoRepository;

    private AgendamentoFacade facade;

    @BeforeEach
    void prepararFacade() {
        PrecoServicoFactory factory = new PrecoServicoFactory(List.of(
                new BanhoPrecoStrategy(),
                new TosaPrecoStrategy(),
                new BanhoETosaPrecoStrategy()));

        facade = new AgendamentoFacade(
                clienteRepository, petRepository, agendamentoRepository, factory);
    }

    private static Cliente cliente(long id, String nome) {
        Cliente cliente = new Cliente(nome, "11999999999", nome.toLowerCase() + "@email.com");
        cliente.setId(id);
        return cliente;
    }

    private static Pet pet(long id, Cliente tutor, double pesoKg) {
        Pet pet = new Pet();
        pet.setId(id);
        pet.setNome("Thor");
        pet.setEspecie("Cachorro");
        pet.setPesoKg(pesoKg);
        pet.setTutor(tutor);
        return pet;
    }

    private static Agendamento agendamentoCom(StatusAgendamento status) {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setStatus(status);
        return agendamento;
    }

    private void devolveOQueFoiSalvo() {
        when(agendamentoRepository.save(any(Agendamento.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Nested
    @DisplayName("cadastro de pet")
    class CadastroDePet {
        @Test
        @DisplayName("vincula o pet ao tutor informado")
        void vinculaPetAoTutor() {
            Cliente ana = cliente(1L, "Ana");
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(ana));
            when(petRepository.save(any(Pet.class)))
                    .thenAnswer(invocacao -> invocacao.getArgument(0));

            Pet salvo = facade.cadastrarPet(
                    new PetRequest("Thor", "Cachorro", "Shih-tzu", 7.5, 1L));

            assertThat(salvo.getTutor()).isSameAs(ana);
            assertThat(salvo.getNome()).isEqualTo("Thor");
            assertThat(salvo.getPesoKg()).isEqualTo(7.5);
        }

        @Test
        @DisplayName("recusa cadastro quando o tutor não existe")
        void recusaTutorInexistente() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> facade.cadastrarPet(
                    new PetRequest("Thor", "Cachorro", null, 7.5, 99L)))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("Cliente não encontrado");

            verify(petRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("criação de agendamento")
    class CriacaoDeAgendamento {
        @Test
        @DisplayName("calcula o valor pela estratégia do serviço e nasce AGENDADO")
        void criaAgendamentoComValorCalculado() {
            Cliente ana = cliente(1L, "Ana");
            Pet thor = pet(1L, ana, 7.5);
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(ana));
            when(petRepository.findById(1L)).thenReturn(Optional.of(thor));
            devolveOQueFoiSalvo();

            Agendamento criado = facade.criarAgendamento(new AgendamentoRequest(
                    1L, 1L, TipoServico.BANHO_E_TOSA, AMANHA));

            ArgumentCaptor<Agendamento> capturado = ArgumentCaptor.forClass(Agendamento.class);
            verify(agendamentoRepository).save(capturado.capture());

            assertThat(capturado.getValue().getValor()).isEqualByComparingTo(new BigDecimal("85.00"));
            assertThat(criado.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
            assertThat(criado.getCliente()).isSameAs(ana);
            assertThat(criado.getPet()).isSameAs(thor);
            assertThat(criado.getDataHora()).isEqualTo(AMANHA);
        }

        @Test
        @DisplayName("aplica a faixa de preço acima de 10 kg")
        void aplicaFaixaDePesoMaior() {
            Cliente ana = cliente(1L, "Ana");
            Pet grandao = pet(1L, ana, 25.0);
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(ana));
            when(petRepository.findById(1L)).thenReturn(Optional.of(grandao));
            devolveOQueFoiSalvo();

            Agendamento criado = facade.criarAgendamento(new AgendamentoRequest(
                    1L, 1L, TipoServico.BANHO, AMANHA));

            assertThat(criado.getValor()).isEqualByComparingTo(new BigDecimal("60.00"));
        }

        @Test
        @DisplayName("impede agendar um pet que pertence a outro tutor")
        void impedeAgendarPetDeOutroTutor() {
            Cliente ana = cliente(1L, "Ana");
            Cliente bruno = cliente(2L, "Bruno");
            Pet petDoBruno = pet(1L, bruno, 7.5);
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(ana));
            when(petRepository.findById(1L)).thenReturn(Optional.of(petDoBruno));

            assertThatThrownBy(() -> facade.criarAgendamento(new AgendamentoRequest(
                    1L, 1L, TipoServico.BANHO, AMANHA)))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("não pertence ao cliente");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("recusa agendamento quando o cliente não existe")
        void recusaClienteInexistente() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> facade.criarAgendamento(new AgendamentoRequest(
                    99L, 1L, TipoServico.BANHO, AMANHA)))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("Cliente não encontrado");
        }

        @Test
        @DisplayName("recusa agendamento quando o pet não existe")
        void recusaPetInexistente() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L, "Ana")));
            when(petRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> facade.criarAgendamento(new AgendamentoRequest(
                    1L, 99L, TipoServico.BANHO, AMANHA)))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("Pet não encontrado");
        }
    }

    @Nested
    @DisplayName("transições de status")
    class TransicoesDeStatus {
        @Test
        @DisplayName("conclui um agendamento que está AGENDADO")
        void concluiAgendamentoAberto() {
            when(agendamentoRepository.findById(1L))
                    .thenReturn(Optional.of(agendamentoCom(StatusAgendamento.AGENDADO)));
            devolveOQueFoiSalvo();

            assertThat(facade.concluirAgendamento(1L).getStatus())
                    .isEqualTo(StatusAgendamento.CONCLUIDO);
        }

        @Test
        @DisplayName("cancela um agendamento que está AGENDADO")
        void cancelaAgendamentoAberto() {
            when(agendamentoRepository.findById(1L))
                    .thenReturn(Optional.of(agendamentoCom(StatusAgendamento.AGENDADO)));
            devolveOQueFoiSalvo();

            assertThat(facade.cancelarAgendamento(1L).getStatus())
                    .isEqualTo(StatusAgendamento.CANCELADO);
        }

        @Test
        @DisplayName("não conclui um agendamento já cancelado")
        void naoConcluiAgendamentoCancelado() {
            when(agendamentoRepository.findById(1L))
                    .thenReturn(Optional.of(agendamentoCom(StatusAgendamento.CANCELADO)));

            assertThatThrownBy(() -> facade.concluirAgendamento(1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("AGENDADO");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("não cancela um agendamento já concluído")
        void naoCancelaAgendamentoConcluido() {
            when(agendamentoRepository.findById(1L))
                    .thenReturn(Optional.of(agendamentoCom(StatusAgendamento.CONCLUIDO)));

            assertThatThrownBy(() -> facade.cancelarAgendamento(1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("AGENDADO");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("informa quando o agendamento não existe")
        void informaAgendamentoInexistente() {
            when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> facade.concluirAgendamento(99L))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("Agendamento não encontrado");
        }
    }
}

