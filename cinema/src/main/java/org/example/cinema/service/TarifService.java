package org.example.cinema.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.cinema.model.TarifDefaut;
import org.example.cinema.model.TarifSeance;
import org.example.cinema.model.Ticket;
import org.example.cinema.repository.TarifDefautRepository;
import org.example.cinema.repository.TarifSeanceRepository;
import org.example.cinema.repository.TicketRepository;
import org.springframework.stereotype.Service;

@Service
public class TarifService {

    private final TarifDefautRepository repository;
    private final TarifSeanceRepository tarifSeanceRepository;
    private final TicketRepository ticketRepository;
    private final RemiseService remiseService;

    public TarifService(TarifDefautRepository repository, TarifSeanceRepository tarifSeanceRepository,
                        TicketRepository ticketRepository, RemiseService remiseService) {
        this.repository = repository;
        this.tarifSeanceRepository = tarifSeanceRepository;
        this.ticketRepository = ticketRepository;
        this.remiseService = remiseService;
    }

    public List<TarifDefaut> findAll() {
        return repository.findAllWithDetails();
    }

    public Optional<TarifDefaut> findByTypePlaceAndCategorie(Long typePlaceId, Long categorieId) {
        return repository.findByTypePlaceIdAndCategoriePersonneId(typePlaceId, categorieId);
    }

    /**
     * Retourne un tarif le plus récent spécifique à une séance si présent.
     */
    public Optional<TarifSeance> findSeanceTarif(Long seanceId, Long typePlaceId, Long categorieId) {
        return tarifSeanceRepository.findLatestBySeanceIdAndTypePlaceIdAndCategoriePersonneId(seanceId, typePlaceId,
                categorieId);
    }

    /**
     * MÉTHODE CENTRALE : Calcule le prix pour une séance donnée.
     * Priorité: 
     * 1. Tarif de séance (prix direct si non NULL)
     * 2. Remise (si prix NULL dans tarif_seance, calcul via pourcentage)
     * 3. Tarif par défaut
     * 
     * @param seanceId ID de la séance
     * @param typePlaceId ID du type de place
     * @param categorieId ID de la catégorie de personne
     * @return Le prix calculé, ou null si aucun tarif trouvé
     */
    public BigDecimal getPrixPourSeance(Long seanceId, Long typePlaceId, Long categorieId) {
        // 1. Chercher tarif de séance le plus récent
        Optional<TarifSeance> tarifSeanceOpt = findSeanceTarif(seanceId, typePlaceId, categorieId);
        
        if (tarifSeanceOpt.isPresent()) {
            TarifSeance tarifSeance = tarifSeanceOpt.get();
            if (tarifSeance.getPrix() != null) {
                // Prix direct dans tarif_seance
                return tarifSeance.getPrix();
            } else {
                // Prix NULL => chercher dans remise
                BigDecimal prixCalcule = remiseService.calculerPrixAvecRemise(seanceId, typePlaceId, categorieId);
                if (prixCalcule != null) {
                    return prixCalcule;
                }
            }
        }
        
        // 3. Sinon, tarif par défaut
        return findByTypePlaceAndCategorie(typePlaceId, categorieId)
                .map(TarifDefaut::getPrix)
                .orElse(null);
    }

    /**
     * Priorité: tarif de séance (avec support remise si prix NULL) puis tarif par défaut.
     * @deprecated Utiliser getPrixPourSeance() à la place
     */
    @Deprecated
    public Optional<BigDecimal> findTarifForSeanceOrDefault(Long seanceId, Long typePlaceId, Long categorieId) {
        return Optional.ofNullable(getPrixPourSeance(seanceId, typePlaceId, categorieId));
    }

    /**
     * Calcule le chiffre d'affaires fictif d'une séance.
     * Utilise les tickets existants mais avec les tarifs actuels (les plus récents).
     * Permet de voir combien la séance rapporterait avec les prix actuels.
     * 
     * @param seanceId ID de la séance
     * @return Le CA fictif calculé avec les tarifs actuels
     */
    public BigDecimal calculerCAFictif(Long seanceId) {
        List<Ticket> tickets = ticketRepository.findBySeanceIdWithDetails(seanceId);
        BigDecimal caFictif = BigDecimal.ZERO;
        
        for (Ticket ticket : tickets) {
            if (ticket.getPlace() == null || ticket.getPlace().getTypePlace() == null || ticket.getCategoriePersonne() == null) {
                continue;
            }
            
            Long typePlaceId = ticket.getPlace().getTypePlace().getId();
            Long categorieId = ticket.getCategoriePersonne().getId();
            
            BigDecimal prix = getPrixPourSeance(seanceId, typePlaceId, categorieId);
            if (prix != null) {
                caFictif = caFictif.add(prix);
            }
        }
        
        return caFictif;
    }
    
    /**
     * Calcule les CA fictifs pour plusieurs séances.
     * 
     * @param seanceIds Liste des IDs de séances
     * @return Map seanceId -> CA fictif
     */
    public Map<Long, BigDecimal> calculerCAFictifsBySeances(List<Long> seanceIds) {
        Map<Long, BigDecimal> result = new HashMap<>();
        for (Long seanceId : seanceIds) {
            result.put(seanceId, calculerCAFictif(seanceId));
        }
        return result;
    }

    /**
     * Retourne, pour chaque type de place, le tarif maximal disponible pour une
     * séance (priorité tarifs séance les plus récents, sinon tarifs par défaut).
     * Prend en compte les remises si prix NULL.
     */
    public Map<Long, BigDecimal> getMaxTarifByTypePlaceForSeance(Long seanceId) {
        Map<Long, BigDecimal> result = new HashMap<>();

        // Tarifs de séance les plus récents groupés par type de place
        Map<Long, List<TarifSeance>> seanceTarifs = tarifSeanceRepository.findLatestBySeanceId(seanceId)
                .stream()
                .collect(Collectors.groupingBy(ts -> ts.getTypePlace().getId()));

        for (Map.Entry<Long, List<TarifSeance>> entry : seanceTarifs.entrySet()) {
            Long typePlaceId = entry.getKey();
            BigDecimal max = null;
            
            for (TarifSeance ts : entry.getValue()) {
                // Utiliser la méthode centrale
                BigDecimal prix = getPrixPourSeance(seanceId, typePlaceId, ts.getCategoriePersonne().getId());
                
                if (prix != null) {
                    if (max == null || prix.compareTo(max) > 0) {
                        max = prix;
                    }
                }
            }
            
            if (max != null) {
                result.put(typePlaceId, max);
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
