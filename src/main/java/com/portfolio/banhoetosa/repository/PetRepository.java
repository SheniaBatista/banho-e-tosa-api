package com.portfolio.banhoetosa.repository;

import com.portfolio.banhoetosa.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}
