package org.example.cinema.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.example.cinema.model.CategoriePersonne;
import org.example.cinema.model.Remise;
import org.example.cinema.model.Seance;
import org.example.cinema.model.TarifSeance;
import org.example.cinema.model.TypePlace;
import org.example.cinema.repository.RemiseRepository;
import org.example.cinema.repository.TarifSeanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemiseService {

    private final RemiseRepository remiseRepository;
    private final TarifSeanceRepository tarifSeanceRepository;

    public RemiseService(RemiseRepository remiseRepository, TarifSeanceRepository tarifSeanceRepository) {
        this.remiseRepository = remiseRepository;
        this.tarifSeanceRepository = tarifSeanceRepository;
    }

    /**
     * Trouve la remise active la plus récente pour une combinaison donnée
     */
    public Optional<Remise> findLatestActiveRemise(Long seanceId, Long typePlaceId, Long categoriePersonneCibleId) {
        return remiseRepository.findLatestActiveBySeanceIdAndTypePlaceIdAndCategoriePersonneCibleId(
                seanceId, typePlaceId, categoriePersonneCibleId);
    }

    /**
     * Trouve toutes les remises actives les plus récentes pour une séance
     */
    public List<Remise> findLatestActiveBySeanceId(Long seanceId) {
        return remiseRepository.findLatestActiveBySeanceId(seanceId);
    }

    /**
     * Calcule le prix en utilisant la remise active la plus récente
     */
    public BigDecimal calculerPrixAvecRemise(Long seanceId, Long typePlaceId, Long categoriePersonneCibleId) {
        Optional<Remise> remiseOpt = findLatestActiveRemise(seanceId, typePlaceId, categoriePersonneCibleId);
        if (remiseOpt.isEmpty()) {
            return null;
        }

        Remise remise = remiseOpt.get();
        Long categorieRepereId = remise.getCategoriePersonneRepere().getId();

        // Chercher le tarif le plus récent de la catégorie de référence
        Optional<TarifSeance> tarifRepereOpt = tarifSeanceRepository
                .findLatestBySeanceIdAndTypePlaceIdAndCategoriePersonneId(seanceId, typePlaceId, categorieRepereId);

        if (tarifRepereOpt.isEmpty() || tarifRepereOpt.get().getPrix() == null) {
            return null;
        }

        BigDecimal prixRepere = tarifRepereOpt.get().getPrix();
        BigDecimal pourcentage = remise.getPourcentage();

        return prixRepere.multiply(pourcentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public Remise save(Remise remise) {
        return remiseRepository.save(remise);
    }

    @Transactional
    public Remise createRemise(Seance seance, TypePlace typePlace, 
                               CategoriePersonne categorieCible, 
                               CategoriePersonne categorieRepere, 
                               BigDecimal pourcentage) {
        Remise remise = Remise.builder()
                .seance(seance)
                .typePlace(typePlace)
                .categoriePersonneCible(categorieCible)
                .categoriePersonneRepere(categorieRepere)
                .pourcentage(pourcentage)
                .build();
        return remiseRepository.save(remise);
    }

    /**
     * Désactive toutes les remises d'une séance (met le pourcentage en négatif)
     */
    @Transactional
    public void deactivateBySeanceId(Long seanceId) {
        remiseRepository.deactivateBySeanceId(seanceId);
    }
    
    /**
     * Désactive une remise spécifique (met le pourcentage en négatif)
     */
    @Transactional
    public void deactivateRemise(Long seanceId, Long typePlaceId, Long categorieId) {
        remiseRepository.deactivateBySeanceIdAndTypePlaceIdAndCategorieId(seanceId, typePlaceId, categorieId);
    }
}
