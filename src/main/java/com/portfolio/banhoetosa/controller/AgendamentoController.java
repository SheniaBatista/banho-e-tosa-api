package com.portfolio.banhoetosa.controller;

import com.portfolio.banhoetosa.dto.AgendamentoRequest;
import com.portfolio.banhoetosa.facade.AgendamentoFacade;
import com.portfolio.banhoetosa.model.Agendamento;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoFacade facade;

    public AgendamentoController(AgendamentoFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Agendamento agendar(@Valid @RequestBody AgendamentoRequest request) {
        return facade.criarAgendamento(request);
    }

    @GetMapping
    public List<Agendamento> listar() {
        return facade.listarAgendamentos();
    }

    @PatchMapping("/{id}/concluir")
    public Agendamento concluir(@PathVariable Long id) {
        return facade.concluirAgendamento(id);
    }

    @PatchMapping("/{id}/cancelar")
    public Agendamento cancelar(@PathVariable Long id) {
        return facade.cancelarAgendamento(id);
    }
}
