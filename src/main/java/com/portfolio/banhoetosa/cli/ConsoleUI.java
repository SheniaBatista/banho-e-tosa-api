package com.portfolio.banhoetosa.cli;

import com.portfolio.banhoetosa.dto.AgendamentoRequest;
import com.portfolio.banhoetosa.dto.PetRequest;
import com.portfolio.banhoetosa.exception.RecursoNaoEncontradoException;
import com.portfolio.banhoetosa.exception.RegraNegocioException;
import com.portfolio.banhoetosa.facade.AgendamentoFacade;
import com.portfolio.banhoetosa.model.Agendamento;
import com.portfolio.banhoetosa.model.Cliente;
import com.portfolio.banhoetosa.model.Pet;
import com.portfolio.banhoetosa.model.TipoServico;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("cli")
public class ConsoleUI implements CommandLineRunner {

    private static final int COLUNA_MENU = 33;

    private final AgendamentoFacade facade;
    private final Validator validator;

    public ConsoleUI(AgendamentoFacade facade, Validator validator) {
        this.facade = facade;
        this.validator = validator;
    }

    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Console console = new Console(scanner);
            console.banner();
            console.rodapeDoBanner("Java 25  ·  Spring Boot 4  ·  PostgreSQL", "conectado");

            boolean executando = true;
            while (executando) {
                exibirMenu(console);
                String opcao = console.lerOpcao("Opção");
                executando = executarOpcao(console, opcao);
            }
        }
    }

    private void exibirMenu(Console console) {
        console.cabecalho("MENU PRINCIPAL");
        console.escrever(par("1", "Cadastrar cliente", "5", "Criar agendamento"));
        console.escrever(par("2", "Listar clientes", "6", "Listar agendamentos"));
        console.escrever(par("3", "Cadastrar pet", "7", "Concluir agendamento"));
        console.escrever(par("4", "Listar pets", "8", "Cancelar agendamento"));
        console.escrever(par("0", "Sair", "9", "Tabela de preços"));
        console.separador();
    }

    private static String par(String numEsq, String rotEsq, String numDir, String rotDir) {
        String esquerda = item(numEsq, rotEsq);
        int visivel = 4 + rotEsq.length();
        String recheio = " ".repeat(Math.max(1, COLUNA_MENU - visivel));
        return " " + esquerda + recheio + item(numDir, rotDir);
    }

    private static String item(String numero, String rotulo) {
        return Console.ciano("[" + numero + "]") + " " + rotulo;
    }

    private boolean executarOpcao(Console console, String opcao) {
        try {
            switch (opcao) {
                case "1" -> cadastrarCliente(console);
                case "2" -> listarClientes(console);
                case "3" -> cadastrarPet(console);
                case "4" -> listarPets(console);
                case "5" -> criarAgendamento(console);
                case "6" -> listarAgendamentos(console);
                case "7" -> concluirAgendamento(console);
                case "8" -> cancelarAgendamento(console);
                case "9" -> exibirTabelaDePrecos(console);
                case "0" -> {
                    console.linhaEmBranco();
                    console.sucesso("Até logo!");
                    console.linhaEmBranco();
                    return false;
                }
                default -> console.erro("Opção inválida. Escolha um número entre 0 e 9.");
            }
        } catch (RecursoNaoEncontradoException | RegraNegocioException ex) {
            console.linhaEmBranco();
            console.erro(ex.getMessage());
            console.aguardarEnter();
        } catch (RuntimeException ex) {
            console.linhaEmBranco();
            console.erro("Não foi possível concluir a operação: " + ex.getMessage());
            console.aguardarEnter();
        }
        return true;
    }

    private void cadastrarCliente(Console console) {
        console.secao("Cadastrar cliente");

        Cliente cliente = new Cliente(
                console.lerTextoObrigatorio("Nome"),
                console.lerTextoObrigatorio("Telefone"),
                console.lerTextoObrigatorio("E-mail"));

        if (invalido(console, cliente)) {
            return;
        }

        Cliente salvo = facade.cadastrarCliente(cliente);
        console.linhaEmBranco();
        console.sucesso("Cliente cadastrado com o id " + salvo.getId() + ".");
        console.aguardarEnter();
    }

    private void listarClientes(Console console) {
        console.secao("Clientes cadastrados");
        List<Cliente> clientes = facade.listarClientes();

        if (vazio(console, clientes.isEmpty(), "Nenhum cliente cadastrado ainda.")) {
            return;
        }

        console.escrever(Console.negrito(
                Console.coluna("ID", 6) + Console.coluna("NOME", 24)
                        + Console.coluna("TELEFONE", 17) + "E-MAIL"));
        console.regua(6, 24, 17, 21);

        for (Cliente cliente : clientes) {
            console.escrever(
                    Console.coluna(String.valueOf(cliente.getId()), 6)
                            + Console.coluna(cliente.getNome(), 24)
                            + Console.coluna(cliente.getTelefone(), 17)
                            + (cliente.getEmail() == null ? "-" : cliente.getEmail()));
        }

        rodapeDaLista(console, clientes.size(), "cliente");
    }

    private void cadastrarPet(Console console) {
        console.secao("Cadastrar pet");

        if (semClientes(console)) {
            return;
        }

        PetRequest request = new PetRequest(
                console.lerTextoObrigatorio("Nome do pet"),
                console.lerTextoObrigatorio("Espécie"),
                console.lerTextoOpcional("Raça"),
                console.lerPeso("Peso em kg"),
                console.lerId("Id do tutor"));

        if (invalido(console, request)) {
            return;
        }

        Pet salvo = facade.cadastrarPet(request);
        console.linhaEmBranco();
        console.sucesso("Pet cadastrado com o id " + salvo.getId()
                + " e vinculado a " + salvo.getTutor().getNome() + ".");
        console.aguardarEnter();
    }

    private void listarPets(Console console) {
        console.secao("Pets cadastrados");
        List<Pet> pets = facade.listarPets();

        if (vazio(console, pets.isEmpty(), "Nenhum pet cadastrado ainda.")) {
            return;
        }

        console.escrever(Console.negrito(
                Console.coluna("ID", 6) + Console.coluna("NOME", 18)
                        + Console.coluna("ESPÉCIE", 15) + Console.coluna("PESO", 11) + "TUTOR"));
        console.regua(6, 18, 15, 11, 18);

        for (Pet pet : pets) {
            console.escrever(
                    Console.coluna(String.valueOf(pet.getId()), 6)
                            + Console.coluna(pet.getNome(), 18)
                            + Console.coluna(pet.getEspecie(), 15)
                            + Console.coluna(pet.getPesoKg() + " kg", 11)
                            + pet.getTutor().getNome());
        }

        rodapeDaLista(console, pets.size(), "pet");
    }

    private void criarAgendamento(Console console) {
        console.secao("Criar agendamento");

        if (semClientes(console)) {
            return;
        }

        Long clienteId = console.lerId("Id do cliente");
        Long petId = console.lerId("Id do pet");
        TipoServico tipoServico = escolherServico(console);
        LocalDateTime dataHora = console.lerDataHoraFutura("Data e hora");

        AgendamentoRequest request =
                new AgendamentoRequest(clienteId, petId, tipoServico, dataHora);
        if (invalido(console, request)) {
            return;
        }

        Agendamento agendamento = facade.criarAgendamento(request);
        console.linhaEmBranco();
        console.sucesso("Agendamento " + agendamento.getId() + " criado.");
        console.linhaEmBranco();
        console.detalhe("Pet", agendamento.getPet().getNome());
        console.detalhe("Serviço", rotuloServico(agendamento.getTipoServico()));
        console.detalhe("Data e hora", Console.dataHora(agendamento.getDataHora()));
        console.detalhe("Valor", Console.verde(Console.negrito(
                Console.moeda(agendamento.getValor()))));
        console.aguardarEnter();
    }

    private void listarAgendamentos(Console console) {
        console.secao("Agendamentos");
        List<Agendamento> agendamentos = facade.listarAgendamentos();

        if (vazio(console, agendamentos.isEmpty(), "Nenhum agendamento registrado ainda.")) {
            return;
        }

        console.escrever(Console.negrito(
                Console.coluna("ID", 5) + Console.coluna("PET", 15)
                        + Console.coluna("SERVIÇO", 15) + Console.coluna("DATA/HORA", 18)
                        + Console.coluna("VALOR", 12) + "STATUS"));
        console.regua(5, 15, 15, 18, 12, 11);

        for (Agendamento agendamento : agendamentos) {
            console.escrever(
                    Console.coluna(String.valueOf(agendamento.getId()), 5)
                            + Console.coluna(agendamento.getPet().getNome(), 15)
                            + Console.coluna(rotuloServico(agendamento.getTipoServico()), 15)
                            + Console.coluna(Console.dataHora(agendamento.getDataHora()), 18)
                            + Console.coluna(Console.moeda(agendamento.getValor()), 12)
                            + colorirStatus(agendamento));
        }

        rodapeDaLista(console, agendamentos.size(), "agendamento");
    }

    private void concluirAgendamento(Console console) {
        console.secao("Concluir agendamento");
        Agendamento agendamento = facade.concluirAgendamento(console.lerId("Id do agendamento"));
        console.linhaEmBranco();
        console.sucesso("Agendamento " + agendamento.getId() + " concluído.");
        console.aguardarEnter();
    }

    private void cancelarAgendamento(Console console) {
        console.secao("Cancelar agendamento");
        Agendamento agendamento = facade.cancelarAgendamento(console.lerId("Id do agendamento"));
        console.linhaEmBranco();
        console.sucesso("Agendamento " + agendamento.getId() + " cancelado.");
        console.aguardarEnter();
    }

    private void exibirTabelaDePrecos(Console console) {
        console.secao("Tabela de preços");

        console.escrever(Console.negrito(
                Console.coluna("SERVIÇO", 20) + Console.coluna("ATÉ 10 KG", 16)
                        + "ACIMA DE 10 KG"));
        console.regua(20, 16, 16);
        console.escrever(Console.coluna("Banho", 20) + Console.coluna("R$ 45,00", 16) + "R$ 60,00");
        console.escrever(Console.coluna("Tosa", 20) + Console.coluna("R$ 55,00", 16) + "R$ 75,00");
        console.escrever(Console.coluna("Banho + Tosa", 20) + Console.coluna("R$ 85,00", 16) + "R$ 115,00");

        console.linhaEmBranco();
        console.informacao("Calculado no servidor pela Strategy do serviço escolhido.");
        console.aguardarEnter();
    }

    private TipoServico escolherServico(Console console) {
        while (true) {
            console.linhaEmBranco();
            console.escrever(" " + item("1", "Banho") + "   " + item("2", "Tosa")
                    + "   " + item("3", "Banho + Tosa"));
            console.linhaEmBranco();
            switch (console.lerOpcao("Serviço")) {
                case "1" -> {
                    return TipoServico.BANHO;
                }
                case "2" -> {
                    return TipoServico.TOSA;
                }
                case "3" -> {
                    return TipoServico.BANHO_E_TOSA;
                }
                default -> console.erro("Escolha 1, 2 ou 3.");
            }
        }
    }

    private <T> boolean invalido(Console console, T objeto) {
        Set<ConstraintViolation<T>> violacoes = validator.validate(objeto);
        if (violacoes.isEmpty()) {
            return false;
        }
        console.linhaEmBranco();
        console.erro(violacoes.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(" ")));
        console.aguardarEnter();
        return true;
    }

    private boolean semClientes(Console console) {
        if (!facade.listarClientes().isEmpty()) {
            return false;
        }
        console.linhaEmBranco();
        console.aviso("Cadastre um cliente antes (opção 1).");
        console.aguardarEnter();
        return true;
    }

    private boolean vazio(Console console, boolean semRegistros, String mensagem) {
        if (!semRegistros) {
            return false;
        }
        console.linhaEmBranco();
        console.aviso(mensagem);
        console.aguardarEnter();
        return true;
    }

    private void rodapeDaLista(Console console, int total, String substantivo) {
        console.linhaEmBranco();
        console.informacao(total + " " + substantivo + (total == 1 ? "" : "s") + ".");
        console.aguardarEnter();
    }

    private static String rotuloServico(TipoServico tipoServico) {
        return switch (tipoServico) {
            case BANHO -> "Banho";
            case TOSA -> "Tosa";
            case BANHO_E_TOSA -> "Banho + Tosa";
        };
    }

    private static String colorirStatus(Agendamento agendamento) {
        String status = agendamento.getStatus().name();
        return switch (agendamento.getStatus()) {
            case AGENDADO -> Console.azul(status);
            case CONCLUIDO -> Console.verde(status);
            case CANCELADO -> Console.vermelho(status);
        };
    }
}
