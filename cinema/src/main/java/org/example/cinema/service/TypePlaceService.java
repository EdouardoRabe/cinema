package org.example.cinema.service;

import org.example.cinema.model.TypePlace;
import org.example.cinema.repository.TypePlaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TypePlaceService {

    private final TypePlaceRepository repository;

    public TypePlaceService(TypePlaceRepository repository) {
        this.repository = repository;
    }

    public List<TypePlace> findAll() {
        return repository.findAll();
    }

    public Optional<TypePlace> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<TypePlace> findByLibelle(String libelle) {
        return repository.findByLibelle(libelle);
    }

    public TypePlace save(TypePlace typePlace) {
        return repository.save(typePlace);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    /**
     * Retourne le type de place "STANDARD" (créé par défaut)
     */
    public TypePlace getStandard() {
        return repository.findByLibelle("STANDARD")
                .orElseGet(() -> {
                    TypePlace standard = new TypePlace();
                    standard.setLibelle("STANDARD");
                    standard.setCouleur("#6c757d");
                    return repository.save(standard);
                });
    }
}
