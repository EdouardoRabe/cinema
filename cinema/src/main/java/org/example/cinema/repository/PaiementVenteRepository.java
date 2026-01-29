package org.example.cinema.repository;

import org.example.cinema.model.PaiementVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaiementVenteRepository extends JpaRepository<PaiementVente, Long> {
    
    List<PaiementVente> findByVenteId(Long venteId);
    
    @Query("SELECT COALESCE(SUM(pv.montantPaye), 0) FROM PaiementVente pv WHERE pv.vente.id = :venteId")
    BigDecimal getTotalPayeByVenteId(@Param("venteId") Long venteId);
    
    @Query("SELECT COALESCE(SUM(pv.montantPaye), 0) FROM PaiementVente pv WHERE YEAR(pv.datePaiement) = :annee AND MONTH(pv.datePaiement) = :mois")
    BigDecimal getTotalPayeByMoisAnnee(@Param("mois") int mois, @Param("annee") int annee);
    
    @Query("SELECT pv FROM PaiementVente pv WHERE YEAR(pv.datePaiement) = :annee AND MONTH(pv.datePaiement) = :mois ORDER BY pv.datePaiement DESC")
    List<PaiementVente> findByMoisAnnee(@Param("mois") int mois, @Param("annee") int annee);
}
