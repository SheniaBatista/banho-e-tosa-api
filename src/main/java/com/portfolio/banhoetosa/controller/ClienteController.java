package com.portfolio.banhoetosa.controller;

import com.portfolio.banhoetosa.facade.AgendamentoFacade;
import com.portfolio.banhoetosa.model.Cliente;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final AgendamentoFacade facade;

    public ClienteController(AgendamentoFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente cadastrar(@Valid @RequestBody Cliente cliente) {
        cliente.setId(null);
        return facade.cadastrarCliente(cliente);
    }

    @GetMapping
    public List<Cliente> listar() {
        return facade.listarClientes();
    }
}
