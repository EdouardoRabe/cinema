package org.example.cinema.service;

import org.example.cinema.model.Film;
import org.example.cinema.repository.FilmRepository;
import org.springframework.stereotype.Service;

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

    public Film save(Film film) {
        return repository.save(film);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
