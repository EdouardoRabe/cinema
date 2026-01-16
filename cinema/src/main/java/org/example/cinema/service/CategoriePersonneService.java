package org.example.cinema.service;

import org.example.cinema.model.CategoriePersonne;
import org.example.cinema.repository.CategoriePersonneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriePersonneService {

    private final CategoriePersonneRepository repository;

    public CategoriePersonneService(CategoriePersonneRepository repository) {
        this.repository = repository;
    }

    public List<CategoriePersonne> findAll() {
        return repository.findAll();
    }

    public Optional<CategoriePersonne> findById(Long id) {
        return repository.findById(id);
    }

    public CategoriePersonne save(CategoriePersonne categoriePersonne) {
        return repository.save(categoriePersonne);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
