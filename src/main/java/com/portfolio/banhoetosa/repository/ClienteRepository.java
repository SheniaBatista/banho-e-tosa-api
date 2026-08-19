package com.portfolio.banhoetosa.repository;

import com.portfolio.banhoetosa.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
