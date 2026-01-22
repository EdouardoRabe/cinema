package org.example.cinema.service;

import org.example.cinema.model.PrixPublicite;
import org.example.cinema.repository.PrixPubliciteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
     * Récupère le prix valide pour une date donnée (le 1er du mois)
     * Retourne null si aucun prix n'est défini pour cette période
     */
    public BigDecimal getPrixPourMois(int annee, int mois) {
        // On prend le prix valide au 1er du mois
        LocalDate premierDuMois = LocalDate.of(annee, mois, 1);
        OffsetDateTime dateTime = premierDuMois.atStartOfDay().atOffset(ZoneOffset.UTC);
        
        return repository.findPrixValidAt(dateTime)
                .map(PrixPublicite::getPrix)
                .orElse(null);
    }

    /**
     * Crée un nouveau prix avec une date spécifiée
     */
    @Transactional
    public PrixPublicite createNewPrix(BigDecimal prix, OffsetDateTime dateCreation) {
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
