package org.example.cinema.service;

import org.example.cinema.model.PrixPublicite;
import org.example.cinema.repository.PrixPubliciteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PrixPubliciteService {
    
    private final PrixPubliciteRepository repository;

    public PrixPubliciteService(PrixPubliciteRepository repository) {
        this.repository = repository;
    }

    public List<PrixPublicite> findAll() {
        return repository.findAll();
    }

    public Optional<PrixPublicite> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Récupère le prix actuel (le plus récent)
     */
    public Optional<PrixPublicite> findLatest() {
        return repository.findLatest();
    }

    /**
     * Récupère le prix actuel (le plus récent)
     * Retourne null si aucun prix n'est défini
     */
    public BigDecimal getPrixActuel() {
        return repository.findLatest()
                .map(PrixPublicite::getPrix)
                .orElse(null);
    }

    /**
     * Récupère le prix valide pour un mois/année donné
     * On prend le prix le plus récent dont la date_creation est <= dernier jour du mois de diffusion
     * Retourne null si aucun prix n'est défini pour cette période
     */
    public BigDecimal getPrixPourMois(int annee, int mois) {
        // On prend le dernier jour du mois à 23:59:59 pour inclure tous les prix créés pendant ce mois
        LocalDate dernierJourDuMois = LocalDate.of(annee, mois, 1).plusMonths(1).minusDays(1);
        LocalDateTime finDuMois = dernierJourDuMois.atTime(23, 59, 59);
        
        return repository.findPrixValidAt(finDuMois)
                .map(PrixPublicite::getPrix)
                .orElse(null);
    }

    /**
     * Crée un nouveau prix avec une date spécifiée
     */
    @Transactional
    public PrixPublicite createNewPrix(BigDecimal prix, LocalDateTime dateCreation) {
        PrixPublicite prixPublicite = PrixPublicite.builder()
                .prix(prix)
                .dateCreation(dateCreation)
                .build();
        return repository.save(prixPublicite);
    }

    @Transactional
    public PrixPublicite save(PrixPublicite prixPublicite) {
        return repository.save(prixPublicite);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
