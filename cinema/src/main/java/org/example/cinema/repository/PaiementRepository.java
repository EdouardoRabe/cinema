package org.example.cinema.repository;

import org.example.cinema.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    
    List<Paiement> findByReservationId(Long reservationId);
    
    @Query("SELECT COALESCE(SUM(p.montantPaye), 0) FROM Paiement p WHERE p.reservation.id = :reservationId")
    BigDecimal getTotalPaieByReservationId(@Param("reservationId") Long reservationId);
    
    @Query("SELECT p FROM Paiement p WHERE p.reservation.id = :reservationId ORDER BY p.datePaiement DESC")
    List<Paiement> findByReservationIdOrderByDateDesc(@Param("reservationId") Long reservationId);
    
    Optional<Paiement> findFirstByReservationIdOrderByDatePaiementDesc(Long reservationId);
    
    @Query("SELECT COALESCE(SUM(p.montantPaye), 0) FROM Paiement p WHERE YEAR(p.datePaiement) = :annee AND MONTH(p.datePaiement) = :mois")
    BigDecimal getTotalPayeByMoisAnnee(@Param("mois") int mois, @Param("annee") int annee);
}
