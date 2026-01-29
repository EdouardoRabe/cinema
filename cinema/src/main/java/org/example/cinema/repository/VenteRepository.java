package org.example.cinema.repository;

import org.example.cinema.model.Vente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {
    
    @Query("SELECT v FROM Vente v WHERE YEAR(v.dateVente) = :annee AND MONTH(v.dateVente) = :mois ORDER BY v.dateVente DESC")
    List<Vente> findByMoisAnnee(@Param("mois") int mois, @Param("annee") int annee);
    
    @Query("SELECT v FROM Vente v ORDER BY v.dateVente DESC")
    List<Vente> findAllOrderByDateDesc();
}
