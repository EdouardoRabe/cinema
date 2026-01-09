package org.example.cinema.repository.custom;

import org.example.cinema.model.Film;

import java.time.LocalDate;
import java.util.List;

public interface FilmRepositoryCustom {
    List<Film> findWithFilters(String title, Long genreId, String langue, LocalDate dateFrom, LocalDate dateTo);
}
