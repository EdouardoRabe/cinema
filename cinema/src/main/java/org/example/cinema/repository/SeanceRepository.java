package org.example.cinema.repository;

import org.example.cinema.model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    // Pour le backoffice : toutes les séances (sans filtre de date)
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle ORDER BY s.debut DESC")
    List<Seance> findAllWithDetails();
    
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.salle.id = :salleId ORDER BY s.debut DESC")
    List<Seance> findBySalleIdAll(@Param("salleId") Long salleId);
    
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.film.id = :filmId ORDER BY s.debut DESC")
    List<Seance> findByFilmIdAll(@Param("filmId") Long filmId);
    
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.debut >= :start AND s.debut < :end ORDER BY s.debut DESC")
    List<Seance> findByDateRangeAll(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.salle.id = :salleId AND s.debut >= :start AND s.debut < :end ORDER BY s.debut DESC")
    List<Seance> findBySalleIdAndDateRangeAll(@Param("salleId") Long salleId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.film.id = :filmId AND s.debut >= :start AND s.debut < :end ORDER BY s.debut DESC")
    List<Seance> findByFilmIdAndDateRangeAll(@Param("filmId") Long filmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.film.id = :filmId AND s.salle.id = :salleId ORDER BY s.debut DESC")
    List<Seance> findByFilmIdAndSalleIdAll(@Param("filmId") Long filmId, @Param("salleId") Long salleId);
    
    // Filtres avec intervalle de dates
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.debut >= :dateFrom AND s.debut < :dateTo ORDER BY s.debut DESC")
    List<Seance> findByDateInterval(@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);
    
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.film.id = :filmId AND s.debut >= :dateFrom AND s.debut < :dateTo ORDER BY s.debut DESC")
    List<Seance> findByFilmIdAndDateInterval(@Param("filmId") Long filmId, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);
    
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.salle.id = :salleId AND s.debut >= :dateFrom AND s.debut < :dateTo ORDER BY s.debut DESC")
    List<Seance> findBySalleIdAndDateInterval(@Param("salleId") Long salleId, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);
    
    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.film.id = :filmId AND s.salle.id = :salleId AND s.debut >= :dateFrom AND s.debut < :dateTo ORDER BY s.debut DESC")
    List<Seance> findByFilmIdAndSalleIdAndDateInterval(@Param("filmId") Long filmId, @Param("salleId") Long salleId, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.debut >= :now ORDER BY s.debut ASC")
    List<Seance> findUpcomingSeances(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.film.id = :filmId AND s.debut >= :now ORDER BY s.debut ASC")
    List<Seance> findByFilmIdAndDebutAfter(@Param("filmId") Long filmId, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.debut >= :start AND s.debut < :end ORDER BY s.debut ASC")
    List<Seance> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.salle.id = :salleId AND s.debut >= :now ORDER BY s.debut ASC")
    List<Seance> findBySalleId(@Param("salleId") Long salleId, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.salle.id = :salleId AND s.debut >= :start AND s.debut < :end ORDER BY s.debut ASC")
    List<Seance> findBySalleIdAndDateRange(@Param("salleId") Long salleId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.film.id = :filmId AND s.debut >= :start AND s.debut < :end ORDER BY s.debut ASC")
    List<Seance> findByFilmIdAndDateRange(@Param("filmId") Long filmId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Seance s LEFT JOIN FETCH s.film LEFT JOIN FETCH s.salle WHERE s.film.id = :filmId AND s.salle.id = :salleId AND s.debut >= :now ORDER BY s.debut ASC")
    List<Seance> findByFilmIdAndSalleId(@Param("filmId") Long filmId, @Param("salleId") Long salleId, @Param("now") LocalDateTime now);
}
