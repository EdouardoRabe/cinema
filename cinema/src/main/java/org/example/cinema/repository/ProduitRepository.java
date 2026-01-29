package org.example.cinema.repository;

import org.example.cinema.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    
    Optional<Produit> findByLibelle(String libelle);
    
    boolean existsByLibelle(String libelle);
}
