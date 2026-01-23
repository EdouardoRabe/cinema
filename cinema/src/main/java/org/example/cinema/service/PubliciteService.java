package org.example.cinema.service;

import org.example.cinema.model.Publicite;
import org.example.cinema.model.PubliciteDetail;
import org.example.cinema.model.Seance;
import org.example.cinema.model.Societe;
import org.example.cinema.repository.PubliciteDetailRepository;
import org.example.cinema.repository.PubliciteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class PubliciteService {

    private final PubliciteRepository repository;
    private final PubliciteDetailRepository detailRepository;
    private final SocieteService societeService;
    private final SeanceService seanceService;
    private final PrixPubliciteService prixPubliciteService;

    public PubliciteService(PubliciteRepository repository, 
                           PubliciteDetailRepository detailRepository,
                           SocieteService societeService,
                           SeanceService seanceService,
                           PrixPubliciteService prixPubliciteService) {
        this.repository = repository;
        this.detailRepository = detailRepository;
        this.societeService = societeService;
        this.seanceService = seanceService;
        this.prixPubliciteService = prixPubliciteService;
    }

    public List<Publicite> findAll() {
        return repository.findAllWithSocieteAndDetails();
    }

    public Optional<Publicite> findById(Long id) {
        return repository.findById(id);
    }

    public Publicite findByIdWithDetails(Long id) {
        return repository.findByIdWithDetails(id);
    }

    public List<Publicite> findBySocieteId(Long societeId) {
        return repository.findBySocieteId(societeId);
    }

    /**
     * Crée une nouvelle publicité avec ses détails (séances + nbFois)
     * @param societeId ID de la société
     * @param seanceIds Liste des IDs de séances
     * @param nbFoisList Liste des nombres de fois correspondant à chaque séance
     */
    @Transactional
    public Publicite creerPublicite(Long societeId, List<Long> seanceIds, List<Integer> nbFoisList) {
        if (seanceIds == null || seanceIds.isEmpty()) {
            throw new IllegalArgumentException("Au moins une séance doit être sélectionnée");
        }
        if (seanceIds.size() != nbFoisList.size()) {
            throw new IllegalArgumentException("Le nombre de séances et de nbFois doivent correspondre");
        }

        Societe societe = societeService.findById(societeId)
                .orElseThrow(() -> new IllegalArgumentException("Société introuvable: " + societeId));

        Publicite pub = Publicite.builder()
                .societe(societe)
                .build();
        
        // Créer les détails
        for (int i = 0; i < seanceIds.size(); i++) {
            Long seanceId = seanceIds.get(i);
            Integer nbFois = nbFoisList.get(i);
            
            if (nbFois == null || nbFois <= 0) {
                continue; // Ignorer les entrées sans nb de fois valide
            }
            
            Seance seance = seanceService.findById(seanceId)
                    .orElseThrow(() -> new IllegalArgumentException("Séance introuvable: " + seanceId));
            
            PubliciteDetail detail = PubliciteDetail.builder()
                    .publicite(pub)
                    .seance(seance)
                    .nbFois(nbFois)
                    .build();
            
            pub.getDetails().add(detail);
        }
        
        if (pub.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Au moins un détail valide doit être ajouté");
        }

        return repository.save(pub);
    }

    @Transactional
    public Publicite save(Publicite publicite) {
        return repository.save(publicite);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Calcule le montant d'un détail (nbFois * prix du mois de la séance)
     */
    public BigDecimal calculerMontantDetail(PubliciteDetail detail) {
        if (detail == null || detail.getSeance() == null) {
            return BigDecimal.ZERO;
        }
        Seance seance = detail.getSeance();
        int annee = seance.getDebut().getYear();
        int mois = seance.getDebut().getMonthValue();
        
        BigDecimal prixUnitaire = prixPubliciteService.getPrixPourMois(annee, mois);
        if (prixUnitaire == null) {
            return BigDecimal.ZERO;
        }
        return prixUnitaire.multiply(new BigDecimal(detail.getNbFois()));
    }

    /**
     * Calcule le montant total d'une publicité (somme des montants de tous les détails)
     */
    public BigDecimal calculerMontantTotal(Publicite pub) {
        if (pub == null || pub.getDetails() == null || pub.getDetails().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return pub.getDetails().stream()
                .map(this::calculerMontantDetail)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcule le montant total d'une publicité par ID
     */
    public BigDecimal calculerMontantTotal(Long publiciteId) {
        Publicite pub = findByIdWithDetails(publiciteId);
        return calculerMontantTotal(pub);
    }

    /**
     * Calcule le nombre total de diffusions pour une publicité
     */
    public int getTotalNbFois(Publicite pub) {
        if (pub == null || pub.getDetails() == null) {
            return 0;
        }
        return pub.getDetails().stream()
                .mapToInt(PubliciteDetail::getNbFois)
                .sum();
    }

    /**
     * Retourne le nom du mois en français
     */
    public String getNomMois(int mois) {
        String[] noms = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return noms[mois - 1];
    }

    /**
     * Crée une map des montants totaux pour une liste de publicités
     * Clé: ID de la publicité, Valeur: montant total calculé
     */
    public Map<Long, BigDecimal> getMontantsTotaux(List<Publicite> publicites) {
        Map<Long, BigDecimal> montants = new LinkedHashMap<>();
        for (Publicite pub : publicites) {
            montants.put(pub.getId(), calculerMontantTotal(pub));
        }
        return montants;
    }

    /**
     * Calcule le CA publicitaire pour un mois/année donné
     * (basé sur les montants payés au prorata des détails dont la séance est dans ce mois)
     */
    public BigDecimal calculerCAPubPourMois(int annee, int mois, 
                                            java.util.function.Function<Long, BigDecimal> totalPayeProvider) {
        // Trouver tous les détails de publicités pour des séances de ce mois
        List<PubliciteDetail> detailsDuMois = detailRepository.findBySeanceMoisAnnee(annee, mois);
        
        BigDecimal totalCA = BigDecimal.ZERO;
        
        for (PubliciteDetail detail : detailsDuMois) {
            Publicite pub = detail.getPublicite();
            BigDecimal montantDetail = calculerMontantDetail(detail);
            BigDecimal montantTotalPub = calculerMontantTotal(pub);
            
            if (montantTotalPub.compareTo(BigDecimal.ZERO) > 0) {
                // Obtenir le total payé pour cette publicité
                BigDecimal totalPaye = totalPayeProvider.apply(pub.getId());
                // Calculer le pourcentage payé
                BigDecimal pourcentagePaye = totalPaye.divide(montantTotalPub, 10, RoundingMode.HALF_UP);
                // Appliquer au montant du détail
                BigDecimal montantPayeDetail = montantDetail.multiply(pourcentagePaye);
                totalCA = totalCA.add(montantPayeDetail);
            }
        }
        
        return totalCA.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Trouve tous les détails de publicités pour une séance donnée
     */
    public List<PubliciteDetail> findDetailsBySeanceId(Long seanceId) {
        return detailRepository.findBySeanceId(seanceId);
    }

    /**
     * Calcule le montant payé au prorata pour un détail donné
     */
    public BigDecimal calculerMontantPayeDetail(PubliciteDetail detail, BigDecimal totalPaye, BigDecimal montantTotalPub) {
        if (montantTotalPub.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal montantDetail = calculerMontantDetail(detail);
        BigDecimal pourcentagePaye = totalPaye.divide(montantTotalPub, 10, RoundingMode.HALF_UP);
        return montantDetail.multiply(pourcentagePaye).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcule le montant restant à payer au prorata pour un détail donné
     */
    public BigDecimal calculerMontantRestantDetail(PubliciteDetail detail, BigDecimal totalPaye, BigDecimal montantTotalPub) {
        BigDecimal montantDetail = calculerMontantDetail(detail);
        BigDecimal montantPayeDetail = calculerMontantPayeDetail(detail, totalPaye, montantTotalPub);
        return montantDetail.subtract(montantPayeDetail);
    }

    /**
     * Calcule le montant payé de pub pour une séance donnée.
     * Pour chaque PubliciteDetail de cette séance, calcule au prorata du pourcentage payé de la publicité.
     * @param seanceId ID de la séance
     * @param totalPayeProvider fonction qui retourne le total payé pour une publicité
     * @param montantTotalProvider fonction qui retourne le montant total d'une publicité
     * @return Le montant de pub payé pour cette séance
     */
    public BigDecimal calculerMontantPubPayePourSeance(Long seanceId, 
            java.util.function.Function<Long, BigDecimal> totalPayeProvider) {
        List<PubliciteDetail> details = detailRepository.findBySeanceId(seanceId);
        BigDecimal total = BigDecimal.ZERO;
        
        for (PubliciteDetail detail : details) {
            Publicite pub = detail.getPublicite();
            BigDecimal montantDetail = calculerMontantDetail(detail);
            BigDecimal montantTotalPub = calculerMontantTotal(pub);
            
            if (montantTotalPub.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalPaye = totalPayeProvider.apply(pub.getId());
                BigDecimal pourcentagePaye = totalPaye.divide(montantTotalPub, 10, RoundingMode.HALF_UP);
                BigDecimal montantPayeDetail = montantDetail.multiply(pourcentagePaye);
                total = total.add(montantPayeDetail);
            }
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcule les montants payés de pub pour une liste de séances
     * @param seanceIds Liste des IDs de séances
     * @param totalPayeProvider fonction qui retourne le total payé pour une publicité
     * @return Map avec seanceId -> montant payé de pub
     */
    public Map<Long, BigDecimal> calculerMontantsPubPayesPourSeances(List<Long> seanceIds,
            java.util.function.Function<Long, BigDecimal> totalPayeProvider) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (Long seanceId : seanceIds) {
            result.put(seanceId, calculerMontantPubPayePourSeance(seanceId, totalPayeProvider));
        }
        return result;
    }

  
    public BigDecimal calculerMontantTotalPubPourSeance(Long seanceId) {
        List<PubliciteDetail> details = detailRepository.findBySeanceId(seanceId);
        BigDecimal total = BigDecimal.ZERO;
        
        for (PubliciteDetail detail : details) {
            BigDecimal montantDetail = calculerMontantDetail(detail);
            total = total.add(montantDetail);
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public Map<Long, BigDecimal> calculerMontantsTotauxPubPourSeances(List<Long> seanceIds) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (Long seanceId : seanceIds) {
            result.put(seanceId, calculerMontantTotalPubPourSeance(seanceId));
        }
        return result;
    }

  
    public BigDecimal calculerRestePubPourSeance(Long seanceId, 
            java.util.function.Function<Long, BigDecimal> totalPayeProvider) {
        BigDecimal montantTotal = calculerMontantTotalPubPourSeance(seanceId);
        BigDecimal montantPaye = calculerMontantPubPayePourSeance(seanceId, totalPayeProvider);
        return montantTotal.subtract(montantPaye).setScale(2, RoundingMode.HALF_UP);
    }

   
    public Map<Long, BigDecimal> calculerRestesPubPourSeances(List<Long> seanceIds,
            java.util.function.Function<Long, BigDecimal> totalPayeProvider) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (Long seanceId : seanceIds) {
            result.put(seanceId, calculerRestePubPourSeance(seanceId, totalPayeProvider));
        }
        return result;
    }
}
