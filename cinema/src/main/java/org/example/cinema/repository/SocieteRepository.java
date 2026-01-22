package org.example.cinema.repository;

import org.example.cinema.model.Societe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocieteRepository extends JpaRepository<Societe, Long> {
    
    Optional<Societe> findByLibelle(String libelle);
    
    boolean existsByLibelle(String libelle);
}
