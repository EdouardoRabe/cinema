package org.example.cinema.service;

import org.example.cinema.model.PaiementPublicite;
import org.example.cinema.model.Publicite;
import org.example.cinema.repository.PaiementPubliciteRepository;
import org.example.cinema.repository.PubliciteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaiementPubliciteService {

    private final PaiementPubliciteRepository repository;
    private final PubliciteRepository publiciteRepository;
    private final PrixPubliciteService prixPubliciteService;

    public PaiementPubliciteService(PaiementPubliciteRepository repository, 
                                   PubliciteRepository publiciteRepository,
                                   PrixPubliciteService prixPubliciteService) {
        this.repository = repository;
        this.publiciteRepository = publiciteRepository;
        this.prixPubliciteService = prixPubliciteService;
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
     * Calcule le montant total pour une publicité
     */
    private BigDecimal calculerMontant(Publicite pub) {
        if (pub == null || pub.getDateDiffusion() == null) {
            return null;
        }
        BigDecimal prixUnitaire = prixPubliciteService.getPrixPourMois(pub.getAnnee(), pub.getMois());
        if (prixUnitaire == null) {
            return null;
        }
        return prixUnitaire.multiply(new BigDecimal(pub.getNbFois()));
    }

    /**
     * Retourne le reste à payer pour une publicité
     */
    public BigDecimal getResteAPayer(Publicite pub) {
        BigDecimal montantTotal = calculerMontant(pub);
        if (montantTotal == null) {
            return null;
        }
        BigDecimal totalPaye = getTotalPaye(pub.getId());
        return montantTotal.subtract(totalPaye);
    }

    /**
     * Retourne le reste à payer pour une publicité (par ID)
     */
    public BigDecimal getResteAPayer(Long publiciteId) {
        Optional<Publicite> pubOpt = publiciteRepository.findById(publiciteId);
        if (pubOpt.isEmpty()) {
            return null;
        }
        return getResteAPayer(pubOpt.get());
    }

    /**
     * Enregistre un nouveau paiement
     */
    @Transactional
    public PaiementPublicite enregistrerPaiement(Long publiciteId, BigDecimal montant) {
        Publicite pub = publiciteRepository.findById(publiciteId)
                .orElseThrow(() -> new IllegalArgumentException("Publicité introuvable: " + publiciteId));

        // Vérifier que le montant ne dépasse pas le reste à payer
        BigDecimal resteAPayer = getResteAPayer(pub);
        if (resteAPayer != null && montant.compareTo(resteAPayer) > 0) {
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
        Publicite pub = publiciteRepository.findById(publiciteId)
                .orElseThrow(() -> new IllegalArgumentException("Publicité introuvable: " + publiciteId));

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
     * Retourne le total des paiements pour un mois/année donné
     * C'est le vrai CA (seuls les montants payés comptent)
     */
    public BigDecimal getCAPourMois(int annee, int mois) {
        BigDecimal total = repository.getTotalPayePourMois(annee, mois);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Vérifie si une publicité est entièrement payée
     */
    public boolean estEntierementPayee(Publicite pub) {
        BigDecimal reste = getResteAPayer(pub);
        return reste != null && reste.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Vérifie si une publicité est entièrement payée (par ID)
     */
    public boolean estEntierementPayee(Long publiciteId) {
        Optional<Publicite> pubOpt = publiciteRepository.findById(publiciteId);
        return pubOpt.map(this::estEntierementPayee).orElse(false);
    }
}
