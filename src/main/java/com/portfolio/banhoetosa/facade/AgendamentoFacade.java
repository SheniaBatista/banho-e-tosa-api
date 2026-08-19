package com.portfolio.banhoetosa.facade;

import com.portfolio.banhoetosa.dto.AgendamentoRequest;
import com.portfolio.banhoetosa.dto.PetRequest;
import com.portfolio.banhoetosa.exception.RecursoNaoEncontradoException;
import com.portfolio.banhoetosa.exception.RegraNegocioException;
import com.portfolio.banhoetosa.model.Agendamento;
import com.portfolio.banhoetosa.model.Cliente;
import com.portfolio.banhoetosa.model.Pet;
import com.portfolio.banhoetosa.model.StatusAgendamento;
import com.portfolio.banhoetosa.repository.AgendamentoRepository;
import com.portfolio.banhoetosa.repository.ClienteRepository;
import com.portfolio.banhoetosa.repository.PetRepository;
import com.portfolio.banhoetosa.strategy.PrecoServicoFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgendamentoFacade {

    private final ClienteRepository clienteRepository;
    private final PetRepository petRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final PrecoServicoFactory precoServicoFactory;

    public AgendamentoFacade(
            ClienteRepository clienteRepository,
            PetRepository petRepository,
            AgendamentoRepository agendamentoRepository,
            PrecoServicoFactory precoServicoFactory) {
        this.clienteRepository = clienteRepository;
        this.petRepository = petRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.precoServicoFactory = precoServicoFactory;
    }

    public Cliente cadastrarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Pet cadastrarPet(PetRequest request) {
        Cliente tutor = buscarCliente(request.clienteId());

        Pet pet = new Pet();
        pet.setNome(request.nome());
        pet.setEspecie(request.especie());
        pet.setRaca(request.raca());
        pet.setPesoKg(request.pesoKg());
        pet.setTutor(tutor);
        return petRepository.save(pet);
    }

    public List<Pet> listarPets() {
        return petRepository.findAll();
    }

    @Transactional
    public void excluirPet(Long id) {
        Pet pet = buscarPet(id);
        petRepository.delete(pet);
    }

    @Transactional
    public Agendamento criarAgendamento(AgendamentoRequest request) {
        Cliente cliente = buscarCliente(request.clienteId());
        Pet pet = buscarPet(request.petId());

        if (!pet.getTutor().getId().equals(cliente.getId())) {
            throw new RegraNegocioException("O pet informado não pertence ao cliente selecionado.");
        }

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setPet(pet);
        agendamento.setTipoServico(request.tipoServico());
        agendamento.setDataHora(request.dataHora());
        agendamento.setValor(precoServicoFactory.obter(request.tipoServico()).calcular(pet));
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        return agendamentoRepository.save(agendamento);
    }

    public List<Agendamento> listarAgendamentos() {
        return agendamentoRepository.findAll();
    }

    @Transactional
    public Agendamento concluirAgendamento(Long id) {
        Agendamento agendamento = buscarAgendamento(id);
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException("Somente agendamentos com status AGENDADO podem ser concluídos.");
        }
        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        return agendamentoRepository.save(agendamento);
    }

    @Transactional
    public Agendamento cancelarAgendamento(Long id) {
        Agendamento agendamento = buscarAgendamento(id);
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException("Somente agendamentos com status AGENDADO podem ser cancelados.");
        }
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        return agendamentoRepository.save(agendamento);
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + id));
    }

    private Pet buscarPet(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pet não encontrado: " + id));
    }

    private Agendamento buscarAgendamento(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado: " + id));
    }
}
