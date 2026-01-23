package org.example.cinema.service;

import org.example.cinema.model.Seance;
import org.example.cinema.repository.SeanceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return repository.findUpcomingSeances(LocalDateTime.now());
    }

    public List<Seance> findUpcoming(int limit) {
        return repository.findUpcomingSeances(LocalDateTime.now()).stream().limit(limit).toList();
    }

    public List<Seance> findByFilmId(Long filmId) {
        return repository.findByFilmIdAndDebutAfter(filmId, LocalDateTime.now());
    }

    public List<Seance> findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return repository.findByDateRange(start, end);
    }

    public List<Seance> findBySalleId(Long salleId) {
        return repository.findBySalleId(salleId, LocalDateTime.now());
    }

    public List<Seance> findBySalleIdAndDate(Long salleId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return repository.findBySalleIdAndDateRange(salleId, start, end);
    }

    public List<Seance> findByFilmIdAndDate(Long filmId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return repository.findByFilmIdAndDateRange(filmId, start, end);
    }

    public List<Seance> findByFilmIdAndSalleId(Long filmId, Long salleId) {
        return repository.findByFilmIdAndSalleId(filmId, salleId, LocalDateTime.now());
    }

    public List<Seance> findWithFilters(Long filmId, Long salleId, LocalDate dateFrom, LocalDate dateTo) {
        boolean hasDateFilter = dateFrom != null || dateTo != null;
        LocalDateTime start = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime end = dateTo != null ? dateTo.plusDays(1).atStartOfDay() : null;

        // Both dates provided -> use interval queries (results may include past if dates are in past)
        if (hasDateFilter && start != null && end != null) {
            if (filmId != null && salleId != null) {
                return repository.findByFilmIdAndSalleIdAndDateInterval(filmId, salleId, start, end);
            } else if (filmId != null) {
                return repository.findByFilmIdAndDateInterval(filmId, start, end);
            } else if (salleId != null) {
                return repository.findBySalleIdAndDateInterval(salleId, start, end);
            } else {
                return repository.findByDateInterval(start, end);
            }
        }

        // Only dateFrom -> all seances from that date
        if (start != null && end == null) {
            List<Seance> base;
            if (filmId != null && salleId != null) {
                base = repository.findByFilmIdAndSalleIdAll(filmId, salleId);
            } else if (filmId != null) {
                base = repository.findByFilmIdAll(filmId);
            } else if (salleId != null) {
                base = repository.findBySalleIdAll(salleId);
            } else {
                base = repository.findAllWithDetails();
            }
            final LocalDateTime filterStart = start;
            return base.stream().filter(s -> !s.getDebut().isBefore(filterStart)).toList();
        }

        // Only dateTo -> all seances before that date
        if (start == null && end != null) {
            List<Seance> base;
            if (filmId != null && salleId != null) {
                base = repository.findByFilmIdAndSalleIdAll(filmId, salleId);
            } else if (filmId != null) {
                base = repository.findByFilmIdAll(filmId);
            } else if (salleId != null) {
                base = repository.findBySalleIdAll(salleId);
            } else {
                base = repository.findAllWithDetails();
            }
            final LocalDateTime filterEnd = end;
            return base.stream().filter(s -> s.getDebut().isBefore(filterEnd)).toList();
        }

        // No date filter -> keep original upcoming logic
        if (filmId != null && salleId != null) {
            return findByFilmIdAndSalleId(filmId, salleId);
        } else if (filmId != null) {
            return findByFilmId(filmId);
        } else if (salleId != null) {
            return findBySalleId(salleId);
        } else {
            return findUpcoming();
        }
    }
    
    /**
     * Pour le backoffice : inclut TOUTES les séances (passées et futures)
     * Supporte un intervalle de dates (dateFrom et dateTo)
     */
    public List<Seance> findWithFiltersBackoffice(Long filmId, Long salleId, LocalDate dateFrom, LocalDate dateTo) {
        boolean hasDateFilter = dateFrom != null || dateTo != null;
        LocalDateTime start = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime end = dateTo != null ? dateTo.plusDays(1).atStartOfDay() : null;
        
        // Cas avec les deux dates
        if (hasDateFilter && start != null && end != null) {
            if (filmId != null && salleId != null) {
                return repository.findByFilmIdAndSalleIdAndDateInterval(filmId, salleId, start, end);
            } else if (filmId != null) {
                return repository.findByFilmIdAndDateInterval(filmId, start, end);
            } else if (salleId != null) {
                return repository.findBySalleIdAndDateInterval(salleId, start, end);
            } else {
                return repository.findByDateInterval(start, end);
            }
        }
        
        // Cas avec seulement dateFrom (depuis cette date)
        if (start != null && end == null) {
            List<Seance> allSeances;
            if (filmId != null && salleId != null) {
                allSeances = repository.findByFilmIdAndSalleIdAll(filmId, salleId);
            } else if (filmId != null) {
                allSeances = repository.findByFilmIdAll(filmId);
            } else if (salleId != null) {
                allSeances = repository.findBySalleIdAll(salleId);
            } else {
                allSeances = repository.findAllWithDetails();
            }
            final LocalDateTime filterStart = start;
            return allSeances.stream().filter(s -> !s.getDebut().isBefore(filterStart)).toList();
        }
        
        // Cas avec seulement dateTo (jusqu'à cette date)
        if (start == null && end != null) {
            List<Seance> allSeances;
            if (filmId != null && salleId != null) {
                allSeances = repository.findByFilmIdAndSalleIdAll(filmId, salleId);
            } else if (filmId != null) {
                allSeances = repository.findByFilmIdAll(filmId);
            } else if (salleId != null) {
                allSeances = repository.findBySalleIdAll(salleId);
            } else {
                allSeances = repository.findAllWithDetails();
            }
            final LocalDateTime filterEnd = end;
            return allSeances.stream().filter(s -> s.getDebut().isBefore(filterEnd)).toList();
        }
        
        // Pas de filtre de dates
        if (filmId != null && salleId != null) {
            return repository.findByFilmIdAndSalleIdAll(filmId, salleId);
        } else if (filmId != null) {
            return repository.findByFilmIdAll(filmId);
        } else if (salleId != null) {
            return repository.findBySalleIdAll(salleId);
        } else {
            return repository.findAllWithDetails();
        }
    }

    /**
     * Retourne toutes les séances avec film et salle chargés
     */
    public List<Seance> findAllWithFilmAndSalle() {
        return repository.findAllWithDetails();
    }

    public Seance save(Seance seance) {
        return repository.save(seance);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
