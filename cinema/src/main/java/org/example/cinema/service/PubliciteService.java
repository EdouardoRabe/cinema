package org.example.cinema.service;

import org.example.cinema.model.Publicite;
import org.example.cinema.model.Societe;
import org.example.cinema.repository.PubliciteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class PubliciteService {

    private final PubliciteRepository repository;
    private final SocieteService societeService;
    private final PrixPubliciteService prixPubliciteService;

    public PubliciteService(PubliciteRepository repository, 
                           SocieteService societeService,
                           PrixPubliciteService prixPubliciteService) {
        this.repository = repository;
        this.societeService = societeService;
        this.prixPubliciteService = prixPubliciteService;
    }

    public List<Publicite> findAll() {
        return repository.findAllWithSociete();
    }

    public Optional<Publicite> findById(Long id) {
        return repository.findById(id);
    }

    public List<Publicite> findByYearMonth(Integer annee, Integer mois) {
        return repository.findByYearMonth(annee, mois);
    }

    public List<Publicite> findByYear(Integer annee) {
        return repository.findByYear(annee);
    }

    public List<Publicite> findBySocieteId(Long societeId) {
        return repository.findBySocieteId(societeId);
    }

    /**
     * Ajoute ou met à jour une diffusion de publicité.
     * Si une entrée existe déjà pour cette société et ce mois, on update le nb_fois
     * @param societeId ID de la société
     * @param dateDiffusion Date de diffusion (seuls mois/année comptent)
     * @param nbFois Nombre de diffusions
     */
    @Transactional
    public Publicite addOrUpdateDiffusion(Long societeId, LocalDate dateDiffusion, Integer nbFois) {
        int annee = dateDiffusion.getYear();
        int mois = dateDiffusion.getMonthValue();
        
        Optional<Publicite> existing = repository.findBySocieteIdAndYearMonth(societeId, annee, mois);
        
        if (existing.isPresent()) {
            // Update: remplacer le nombre de fois
            Publicite pub = existing.get();
            pub.setNbFois(nbFois);
            // Normaliser la date au 1er du mois
            pub.setDateDiffusion(LocalDate.of(annee, mois, 1));
            return repository.save(pub);
        } else {
            // Créer une nouvelle entrée
            Societe societe = societeService.findById(societeId)
                    .orElseThrow(() -> new IllegalArgumentException("Société introuvable: " + societeId));
            
            Publicite pub = Publicite.builder()
                    .societe(societe)
                    .dateDiffusion(LocalDate.of(annee, mois, 1)) // Normaliser au 1er du mois
                    .nbFois(nbFois)
                    .build();
            return repository.save(pub);
        }
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
     * Calcule le chiffre d'affaires pour un mois/année donné
     * CA = somme(nb_fois) * prix_publicite applicable à ce mois
     * Retourne null si aucun prix n'est défini pour ce mois
     */
    public BigDecimal calculerCAPourMois(Integer annee, Integer mois) {
        BigDecimal prixUnitaire = prixPubliciteService.getPrixPourMois(annee, mois);
        if (prixUnitaire == null) {
            return null;
        }
        Integer totalDiffusions = repository.sumNbFoisByYearMonth(annee, mois);
        return prixUnitaire.multiply(new BigDecimal(totalDiffusions));
    }

    /**
     * Calcule le détail du CA pour un mois/année donné
     */
    public Map<String, Object> getDetailCAPourMois(Integer annee, Integer mois) {
        List<Publicite> publicites = repository.findByYearMonth(annee, mois);
        BigDecimal prixUnitaire = prixPubliciteService.getPrixPourMois(annee, mois);
        
        int totalDiffusions = publicites.stream()
                .mapToInt(Publicite::getNbFois)
                .sum();
        
        BigDecimal ca = null;
        if (prixUnitaire != null) {
            ca = prixUnitaire.multiply(new BigDecimal(totalDiffusions));
        }
        
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("annee", annee);
        detail.put("mois", mois);
        detail.put("moisLibelle", getNomMois(mois));
        detail.put("prixUnitaire", prixUnitaire);
        detail.put("totalDiffusions", totalDiffusions);
        detail.put("ca", ca);
        detail.put("publicites", publicites);
        detail.put("nbSocietes", publicites.size());
        
        return detail;
    }

    /**
     * Récupère les statistiques de CA groupées par mois pour une année
     */
    public List<Map<String, Object>> getStatistiquesParMois(Integer annee) {
        List<Map<String, Object>> stats = new ArrayList<>();
        List<Integer> moisAvecPubs = repository.findDistinctMonthsByYear(annee);
        
        for (Integer mois : moisAvecPubs) {
            stats.add(getDetailCAPourMois(annee, mois));
        }
        
        return stats;
    }

    /**
     * Récupère la liste des années ayant des publicités
     */
    public List<Integer> getAnneesDisponibles() {
        List<Integer> annees = repository.findDistinctYears();
        if (annees.isEmpty()) {
            annees = new ArrayList<>();
            annees.add(LocalDate.now().getYear());
        }
        return annees;
    }

    /**
     * Génère la liste des mois pour les 12 prochains mois (pour les selects)
     */
    public List<Map<String, Object>> getMoisDisponibles() {
        List<Map<String, Object>> mois = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        // 12 prochains mois
        for (int i = 0; i < 12; i++) {
            LocalDate date = now.plusMonths(i).withDayOfMonth(1);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", date);
            m.put("annee", date.getYear());
            m.put("mois", date.getMonthValue());
            m.put("libelle", getNomMois(date.getMonthValue()) + " " + date.getYear());
            mois.add(m);
        }
        
        return mois;
    }

    public String getNomMois(int mois) {
        String[] noms = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return noms[mois - 1];
    }
}
