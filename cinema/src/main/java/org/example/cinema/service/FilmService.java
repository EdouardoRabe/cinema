package org.example.cinema.service;

import org.example.cinema.model.Film;
import org.example.cinema.repository.FilmRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime start = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        return repository.findBySeanceDate(start, end);
    }

    public List<Film> findByGenreIdAndLangue(Long genreId, String langue) {
        return repository.findByGenreIdAndLangue(genreId, langue);
    }

    /**
     * Recherche de films avec filtres combinés
     */
    public List<Film> findWithFilters(Long genreId, String langue, LocalDate date) {
        if (genreId != null && langue != null && !langue.isEmpty()) {
            return findByGenreIdAndLangue(genreId, langue);
        } else if (genreId != null) {
            return findByGenreId(genreId);
        } else if (langue != null && !langue.isEmpty()) {
            return findByLangue(langue);
        } else if (date != null) {
            return findByDate(date);
        } else {
            return findAll();
        }
    }

    public Film save(Film film) {
        return repository.save(film);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
