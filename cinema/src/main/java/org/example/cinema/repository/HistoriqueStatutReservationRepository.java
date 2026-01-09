package org.example.cinema.repository;

import org.example.cinema.model.HistoriqueStatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueStatutReservationRepository extends JpaRepository<HistoriqueStatutReservation, Long> {
    List<HistoriqueStatutReservation> findByReservationIdOrderByDateChangementDesc(Long reservationId);
}