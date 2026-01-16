package org.example.cinema.repository;

import org.example.cinema.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.seance s JOIN FETCH s.film JOIN FETCH r.statut WHERE r.client.id = :clientId ORDER BY r.creeLe DESC")
    List<Reservation> findByClientId(@Param("clientId") Long clientId);
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.seance JOIN FETCH r.statut WHERE r.seance.id = :seanceId")
    List<Reservation> findBySeanceId(@Param("seanceId") Long seanceId);
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.seance JOIN FETCH r.statut WHERE r.seance.id = :seanceId AND r.statut.code = 'PAYEE'")
    List<Reservation> findPayeesBySeanceId(@Param("seanceId") Long seanceId);
}
