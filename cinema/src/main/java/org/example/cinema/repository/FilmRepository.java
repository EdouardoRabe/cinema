package org.example.cinema.repository;

import org.example.cinema.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface FilmRepository extends JpaRepository<Film, Long> {

    @Query("SELECT DISTINCT f FROM Film f JOIN f.genres g WHERE g.id = :genreId")
    List<Film> findByGenreId(@Param("genreId") Long genreId);

    @Query("SELECT f FROM Film f WHERE LOWER(f.langueOriginale) = LOWER(:langue)")
    List<Film> findByLangue(@Param("langue") String langue);

    @Query("SELECT DISTINCT f FROM Film f JOIN Seance s ON s.film = f WHERE s.debut >= :start AND s.debut < :end")
    List<Film> findBySeanceDate(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("SELECT DISTINCT f FROM Film f JOIN f.genres g WHERE g.id = :genreId AND LOWER(f.langueOriginale) = LOWER(:langue)")
    List<Film> findByGenreIdAndLangue(@Param("genreId") Long genreId, @Param("langue") String langue);
}
