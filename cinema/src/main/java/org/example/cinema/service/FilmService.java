package org.example.cinema.service;

import org.example.cinema.model.Film;
import org.example.cinema.repository.FilmRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FilmService {

    private final FilmRepository repository;

    public FilmService(FilmRepository repository) {
        this.repository = repository;
    }

    public List<Film> findAll() {
        return repository.findAll();
    }

    public Optional<Film> findById(Long id) {
        return repository.findById(id);
    }

    public List<Film> findByGenreId(Long genreId) {
        return repository.findByGenreId(genreId);
    }

    public List<Film> findByLangue(String langue) {
        return repository.findByLangue(langue);
    }

    public List<Film> findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return repository.findBySeanceDate(start, end);
    }

    public List<Film> findByGenreIdAndLangue(Long genreId, String langue) {
        return repository.findByGenreIdAndLangue(genreId, langue);
    }

    /**
     * Recherche de films avec filtres combinés
     */
    public List<Film> findWithFilters(Long genreId, String langue, LocalDate date) {
        // backward compatible simple filters (kept) -> delegate to more advanced method if needed
        if ((genreId == null || genreId == 0) && (langue == null || langue.isBlank()) && date == null) {
            return findAll();
        }
        return repository.findWithFilters(null, genreId, langue, date, date);
    }

    /**
     * Backoffice advanced filters: title, genre, langue, date range
     */
    public List<Film> findWithFiltersAdvanced(String title, Long genreId, String langue, LocalDate dateFrom, LocalDate dateTo) {
        return repository.findWithFilters(title, genreId, langue, dateFrom, dateTo);
    }

    public Film save(Film film) {
        return repository.save(film);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public void delete(Film film) {
        repository.delete(film);
    }
}
