package org.example.cinema.service;

import org.example.cinema.model.PaiementPublicite;
import org.example.cinema.model.Publicite;
import org.example.cinema.model.PubliciteDetail;
import org.example.cinema.repository.PaiementPubliciteRepository;
import org.example.cinema.repository.PubliciteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaiementPubliciteService {

    private final PaiementPubliciteRepository repository;
    private final PubliciteRepository publiciteRepository;
    private final PubliciteService publiciteService;

    public PaiementPubliciteService(PaiementPubliciteRepository repository, 
                                   PubliciteRepository publiciteRepository,
                                   PubliciteService publiciteService) {
        this.repository = repository;
        this.publiciteRepository = publiciteRepository;
        this.publiciteService = publiciteService;
    }

    public List<PaiementPublicite> findAll() {
        return repository.findAll();
    }

    public Optional<PaiementPublicite> findById(Long id) {
        return repository.findById(id);
    }

    public List<PaiementPublicite> findByPubliciteId(Long publiciteId) {
        return repository.findByPubliciteIdOrderByDatePaiementDesc(publiciteId);
    }

    /**
     * Retourne le total payé pour une publicité
     */
    public BigDecimal getTotalPaye(Long publiciteId) {
        BigDecimal total = repository.getTotalPayeParPublicite(publiciteId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Retourne le reste à payer pour une publicité
     */
    public BigDecimal getResteAPayer(Publicite pub) {
        BigDecimal montantTotal = publiciteService.calculerMontantTotal(pub);
        BigDecimal totalPaye = getTotalPaye(pub.getId());
        return montantTotal.subtract(totalPaye);
    }

    /**
     * Retourne le reste à payer pour une publicité (par ID)
     */
    public BigDecimal getResteAPayer(Long publiciteId) {
        Publicite pub = publiciteRepository.findByIdWithDetails(publiciteId);
        if (pub == null) {
            return BigDecimal.ZERO;
        }
        return getResteAPayer(pub);
    }

    /**
     * Retourne le pourcentage payé pour une publicité
     */
    public BigDecimal getPourcentagePaye(Long publiciteId) {
        Publicite pub = publiciteRepository.findByIdWithDetails(publiciteId);
        if (pub == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal montantTotal = publiciteService.calculerMontantTotal(pub);
        if (montantTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalPaye = getTotalPaye(publiciteId);
        return totalPaye.divide(montantTotal, 4, RoundingMode.HALF_UP)
                       .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Enregistre un nouveau paiement
     */
    @Transactional
    public PaiementPublicite enregistrerPaiement(Long publiciteId, BigDecimal montant) {
        Publicite pub = publiciteRepository.findByIdWithDetails(publiciteId);
        if (pub == null) {
            throw new IllegalArgumentException("Publicité introuvable: " + publiciteId);
        }

        // Vérifier que le montant ne dépasse pas le reste à payer
        BigDecimal resteAPayer = getResteAPayer(pub);
        if (montant.compareTo(resteAPayer) > 0) {
            throw new IllegalArgumentException("Le montant dépasse le reste à payer (" + resteAPayer + " Ar)");
        }

        PaiementPublicite paiement = new PaiementPublicite();
        paiement.setPublicite(pub);
        paiement.setMontant(montant);
        paiement.setDatePaiement(LocalDateTime.now());

        return repository.save(paiement);
    }

    /**
     * Enregistre un paiement avec une date spécifique
     */
    @Transactional
    public PaiementPublicite enregistrerPaiement(Long publiciteId, BigDecimal montant, LocalDateTime datePaiement) {
        Publicite pub = publiciteRepository.findByIdWithDetails(publiciteId);
        if (pub == null) {
            throw new IllegalArgumentException("Publicité introuvable: " + publiciteId);
        }

        PaiementPublicite paiement = new PaiementPublicite();
        paiement.setPublicite(pub);
        paiement.setMontant(montant);
        paiement.setDatePaiement(datePaiement != null ? datePaiement : LocalDateTime.now());

        return repository.save(paiement);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Calcule le CA publicitaire pour un mois/année donné
     * Basé sur les montants payés au prorata des détails dont la séance est dans ce mois
     */
    public BigDecimal getCAPubPourMois(int annee, int mois) {
        return publiciteService.calculerCAPubPourMois(annee, mois, this::getTotalPaye);
    }

    /**
     * Vérifie si une publicité est entièrement payée
     */
    public boolean estEntierementPayee(Publicite pub) {
        BigDecimal reste = getResteAPayer(pub);
        return reste.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Vérifie si une publicité est entièrement payée (par ID)
     */
    public boolean estEntierementPayee(Long publiciteId) {
        BigDecimal reste = getResteAPayer(publiciteId);
        return reste.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Crée une map des montants totaux payés pour une liste de publicités
     */
    public Map<Long, BigDecimal> getTotauxPayes(List<Publicite> publicites) {
        Map<Long, BigDecimal> totaux = new LinkedHashMap<>();
        for (Publicite pub : publicites) {
            totaux.put(pub.getId(), getTotalPaye(pub.getId()));
        }
        return totaux;
    }

    /**
     * Crée une map des restes à payer pour une liste de publicités
     */
    public Map<Long, BigDecimal> getRestesAPayer(List<Publicite> publicites) {
        Map<Long, BigDecimal> restes = new LinkedHashMap<>();
        for (Publicite pub : publicites) {
            restes.put(pub.getId(), getResteAPayer(pub));
        }
        return restes;
    }

    /**
     * Calcule le montant payé au prorata pour un détail donné
     */
    public BigDecimal getMontantPayeDetail(PubliciteDetail detail) {
        Publicite pub = detail.getPublicite();
        BigDecimal totalPaye = getTotalPaye(pub.getId());
        BigDecimal montantTotal = publiciteService.calculerMontantTotal(pub);
        return publiciteService.calculerMontantPayeDetail(detail, totalPaye, montantTotal);
    }

    /**
     * Calcule le montant restant au prorata pour un détail donné
     */
    public BigDecimal getMontantRestantDetail(PubliciteDetail detail) {
        Publicite pub = detail.getPublicite();
        BigDecimal totalPaye = getTotalPaye(pub.getId());
        BigDecimal montantTotal = publiciteService.calculerMontantTotal(pub);
        return publiciteService.calculerMontantRestantDetail(detail, totalPaye, montantTotal);
    }
}
