package org.example.cinema.service;

import org.example.cinema.model.TarifDefaut;
import org.example.cinema.repository.TarifDefautRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TarifService {

    private final TarifDefautRepository repository;

    public TarifService(TarifDefautRepository repository) {
        this.repository = repository;
    }

    public List<TarifDefaut> findAll() {
        return repository.findAllWithDetails();
    }

    public Optional<TarifDefaut> findByTypePlaceAndCategorie(Long typePlaceId, Long categorieId) {
        return repository.findByTypePlaceIdAndCategoriePersonneId(typePlaceId, categorieId);
    }

    /**
     * Retourne une map des tarifs par catégorie de personne pour un type de place donné.
     * Clé: ID de la catégorie, Valeur: prix en BigDecimal
     */
    public Map<Long, BigDecimal> getTarifsByCategorieForTypePlace(Long typePlaceId) {
        List<TarifDefaut> tarifs = repository.findByTypePlaceId(typePlaceId);
        Map<Long, BigDecimal> result = new HashMap<>();
        for (TarifDefaut tarif : tarifs) {
            result.put(tarif.getCategoriePersonne().getId(), tarif.getPrix());
        }
        return result;
    }

    /**
     * Retourne une map des tarifs par catégorie de personne pour les places STANDARD (id=1).
     */
    public Map<Long, BigDecimal> getTarifsStandardByCategorie() {
        return getTarifsByCategorieForTypePlace(1L);
    }

    /**
     * Retourne le tarif par défaut pour adulte standard (cat=1, type=1).
     */
    public BigDecimal getTarifDefautAdulte() {
        return repository.findByTypePlaceIdAndCategoriePersonneId(1L, 1L)
                .map(TarifDefaut::getPrix)
                .orElse(new BigDecimal("10000"));
    }

    /**
     * Retourne tous les tarifs sous forme de map imbriquée: typePlace -> categorie -> prix
     */
    public Map<Long, Map<Long, BigDecimal>> getAllTarifsMap() {
        List<TarifDefaut> allTarifs = repository.findAllWithDetails();
        Map<Long, Map<Long, BigDecimal>> result = new HashMap<>();
        
        for (TarifDefaut tarif : allTarifs) {
            Long typePlaceId = tarif.getTypePlace().getId();
            Long categorieId = tarif.getCategoriePersonne().getId();
            
            result.computeIfAbsent(typePlaceId, k -> new HashMap<>())
                  .put(categorieId, tarif.getPrix());
        }
        
        return result;
    }
}
