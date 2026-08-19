package com.portfolio.banhoetosa.controller;

import com.portfolio.banhoetosa.dto.PetRequest;
import com.portfolio.banhoetosa.facade.AgendamentoFacade;
import com.portfolio.banhoetosa.model.Pet;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final AgendamentoFacade facade;

    public PetController(AgendamentoFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pet cadastrar(@Valid @RequestBody PetRequest request) {
        return facade.cadastrarPet(request);
    }

    @GetMapping
    public List<Pet> listar() {
        return facade.listarPets();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        facade.excluirPet(id);
    }
}