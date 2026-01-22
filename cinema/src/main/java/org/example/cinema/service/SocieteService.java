package org.example.cinema.service;

import org.example.cinema.model.Societe;
import org.example.cinema.repository.SocieteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SocieteService {

    private final SocieteRepository repository;

    public SocieteService(SocieteRepository repository) {
        this.repository = repository;
    }

    public List<Societe> findAll() {
        return repository.findAll();
    }

    public Optional<Societe> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Societe> findByLibelle(String libelle) {
        return repository.findByLibelle(libelle);
    }

    public boolean existsByLibelle(String libelle) {
        return repository.existsByLibelle(libelle);
    }

    @Transactional
    public Societe save(Societe societe) {
        return repository.save(societe);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
