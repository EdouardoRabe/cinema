package org.example.cinema.repository;

import org.example.cinema.model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.debut >= :now ORDER BY s.debut ASC")
    List<Seance> findUpcomingSeances(@Param("now") OffsetDateTime now);

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.film.id = :filmId AND s.debut >= :now ORDER BY s.debut ASC")
    List<Seance> findByFilmIdAndDebutAfter(@Param("filmId") Long filmId, @Param("now") OffsetDateTime now);

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.debut >= :start AND s.debut < :end ORDER BY s.debut ASC")
    List<Seance> findByDateRange(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);
}
