package org.example.cinema.repository;

import org.example.cinema.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    @Query("SELECT t FROM Ticket t WHERE t.seance.id = :seanceId")
    List<Ticket> findBySeanceId(@Param("seanceId") Long seanceId);
    
    /**
     * Récupère les tickets d'une séance avec leurs détails (place, type de place, catégorie)
     * pour le calcul du CA fictif
     */
    @Query("SELECT t FROM Ticket t " +
           "JOIN FETCH t.place p " +
           "JOIN FETCH p.typePlace " +
           "JOIN FETCH t.categoriePersonne " +
           "WHERE t.seance.id = :seanceId " +
           "AND t.statut.code NOT IN ('ANNULE', 'REMBOURSE')")
    List<Ticket> findBySeanceIdWithDetails(@Param("seanceId") Long seanceId);
    
    @Query("SELECT t.place.id FROM Ticket t WHERE t.seance.id = :seanceId AND t.statut.code NOT IN ('ANNULE', 'REMBOURSE')")
    Set<Long> findOccupiedPlaceIdsBySeanceId(@Param("seanceId") Long seanceId);
    
    @Query("SELECT t FROM Ticket t JOIN FETCH t.place JOIN FETCH t.categoriePersonne WHERE t.reservation.id = :reservationId")
    List<Ticket> findByReservationId(@Param("reservationId") Long reservationId);

    /**
     * Calcule le montant théorique (somme des prix des tickets) pour une séance
     * Exclut les tickets annulés/remboursés
     */
    @Query("SELECT COALESCE(SUM(t.prix), 0) FROM Ticket t WHERE t.seance.id = :seanceId AND t.statut.code NOT IN ('ANNULE', 'REMBOURSE')")
    java.math.BigDecimal getMontantTheoriqueBySeanceId(@Param("seanceId") Long seanceId);
}
