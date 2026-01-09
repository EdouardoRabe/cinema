package org.example.cinema.service;

import org.example.cinema.model.Seance;
import org.example.cinema.repository.SeanceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class SeanceService {

    private final SeanceRepository repository;

    public SeanceService(SeanceRepository repository) {
        this.repository = repository;
    }

    public List<Seance> findAll() {
        return repository.findAll();
    }

    public Optional<Seance> findById(Long id) {
        return repository.findById(id);
    }

    public List<Seance> findUpcoming() {
        return repository.findUpcomingSeances(OffsetDateTime.now());
    }

    public List<Seance> findUpcoming(int limit) {
        return repository.findUpcomingSeances(OffsetDateTime.now()).stream().limit(limit).toList();
    }

    public List<Seance> findByFilmId(Long filmId) {
        return repository.findByFilmIdAndDebutAfter(filmId, OffsetDateTime.now());
    }

    public List<Seance> findByDate(LocalDate date) {
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime start = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        return repository.findByDateRange(start, end);
    }

    public Seance save(Seance seance) {
        return repository.save(seance);
    }
}
