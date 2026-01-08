package org.example.cinema.repository;

import org.example.cinema.model.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutReservationRepository extends JpaRepository<StatutReservation, Long> {
    // additional query methods if needed
}
