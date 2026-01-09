package org.example.cinema.service;

import org.example.cinema.model.Salle;
import org.example.cinema.repository.SalleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalleService {

    private final SalleRepository repository;

    public SalleService(SalleRepository repository) {
        this.repository = repository;
    }

    public List<Salle> findAll() {
        return repository.findAll();
    }

    public Optional<Salle> findById(Long id) {
        return repository.findById(id);
    }
}
