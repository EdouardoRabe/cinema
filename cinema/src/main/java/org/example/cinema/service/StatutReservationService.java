package org.example.cinema.service;

import org.example.cinema.model.StatutReservation;
import org.example.cinema.repository.StatutReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatutReservationService {

    private final StatutReservationRepository repository;

    public StatutReservationService(StatutReservationRepository repository) {
        this.repository = repository;
    }

    public List<StatutReservation> findAll() {
        return repository.findAll();
    }
}
