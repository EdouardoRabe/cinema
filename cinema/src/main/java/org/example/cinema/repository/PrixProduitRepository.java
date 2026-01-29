package org.example.cinema.repository;

import org.example.cinema.model.PrixProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface PrixProduitRepository extends JpaRepository<PrixProduit, Long> {
    
    @Query("SELECT pp FROM PrixProduit pp WHERE pp.produit.id = :produitId AND pp.datePrix <= :date ORDER BY pp.datePrix DESC LIMIT 1")
    Optional<PrixProduit> findPrixAtDate(@Param("produitId") Long produitId, @Param("date") LocalDate date);
    
    @Query("SELECT pp FROM PrixProduit pp WHERE pp.produit.id = :produitId ORDER BY pp.datePrix DESC LIMIT 1")
    Optional<PrixProduit> findLatestByProduitId(@Param("produitId") Long produitId);
    
    List<PrixProduit> findByProduitIdOrderByDatePrixDesc(Long produitId);
}
