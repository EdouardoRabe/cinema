package org.example.cinema.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.cinema.model.TarifDefaut;
import org.example.cinema.model.TarifSeance;
import org.example.cinema.repository.TarifDefautRepository;
import org.example.cinema.repository.TarifSeanceRepository;
import org.springframework.stereotype.Service;

@Service
public class TarifService {

    private final TarifDefautRepository repository;
    private final TarifSeanceRepository tarifSeanceRepository;

    public TarifService(TarifDefautRepository repository, TarifSeanceRepository tarifSeanceRepository) {
        this.repository = repository;
        this.tarifSeanceRepository = tarifSeanceRepository;
    }

    public List<TarifDefaut> findAll() {
        return repository.findAllWithDetails();
    }

    public Optional<TarifDefaut> findByTypePlaceAndCategorie(Long typePlaceId, Long categorieId) {
        return repository.findByTypePlaceIdAndCategoriePersonneId(typePlaceId, categorieId);
    }

    /**
     * Retourne un tarif spécifique à une séance si présent.
     */
    public Optional<TarifSeance> findSeanceTarif(Long seanceId, Long typePlaceId, Long categorieId) {
        return tarifSeanceRepository.findBySeanceIdAndTypePlaceIdAndCategoriePersonneId(seanceId, typePlaceId,
                categorieId);
    }

    /**
     * Priorité: tarif de séance puis tarif par défaut.
     */
    public Optional<BigDecimal> findTarifForSeanceOrDefault(Long seanceId, Long typePlaceId, Long categorieId) {
        return findSeanceTarif(seanceId, typePlaceId, categorieId).map(TarifSeance::getPrix)
                .or(() -> findByTypePlaceAndCategorie(typePlaceId, categorieId).map(TarifDefaut::getPrix));
    }

    /**
     * Retourne, pour chaque type de place, le tarif maximal disponible pour une
     * séance (priorité tarifs séance, sinon tarifs par défaut).
     */
    public Map<Long, BigDecimal> getMaxTarifByTypePlaceForSeance(Long seanceId) {
        Map<Long, BigDecimal> result = new HashMap<>();

        // Tarifs de séance groupés par type de place
        Map<Long, List<TarifSeance>> seanceTarifs = tarifSeanceRepository.findBySeanceId(seanceId)
                .stream()
                .collect(Collectors.groupingBy(ts -> ts.getTypePlace().getId()));

        for (Map.Entry<Long, List<TarifSeance>> entry : seanceTarifs.entrySet()) {
            BigDecimal max = entry.getValue().stream()
                    .map(TarifSeance::getPrix)
                    .max(BigDecimal::compareTo)
                    .orElse(null);
            if (max != null) {
                result.put(entry.getKey(), max);
            }
        }

        // Tarifs par défaut pour les types de place non couverts par la séance
        List<TarifDefaut> defaults = repository.findAllWithDetails();
        Map<Long, List<TarifDefaut>> defaultsByType = defaults.stream()
                .collect(Collectors.groupingBy(td -> td.getTypePlace().getId()));

        for (Map.Entry<Long, List<TarifDefaut>> entry : defaultsByType.entrySet()) {
            Long typePlaceId = entry.getKey();
            if (result.containsKey(typePlaceId)) {
                continue; // déjà couvert par un tarif de séance
            }
            BigDecimal max = entry.getValue().stream()
                    .map(TarifDefaut::getPrix)
                    .max(BigDecimal::compareTo)
                    .orElse(null);
            if (max != null) {
                result.put(typePlaceId, max);
            }
        }

        return result;
    }

    /**
     * Retourne une map des tarifs par catégorie de personne pour un type de place
     * donné.
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
     * Retourne une map des tarifs par catégorie de personne pour les places
     * STANDARD (id=1).
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
                .orElseThrow(() -> new IllegalStateException("Tarif par défaut introuvable (type=1, cat=1)"));
    }

    /**
     * Retourne tous les tarifs sous forme de map imbriquée: typePlace -> categorie
     * -> prix
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
