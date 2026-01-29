package org.example.cinema.repository;

import org.example.cinema.model.VenteDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VenteDetailRepository extends JpaRepository<VenteDetail, Long> {
    
    List<VenteDetail> findByVenteId(Long venteId);
    
    @Query("SELECT COALESCE(SUM(vd.prixUnitaire * vd.quantite), 0) FROM VenteDetail vd WHERE vd.vente.id = :venteId")
    BigDecimal getMontantTotalByVenteId(@Param("venteId") Long venteId);
}
