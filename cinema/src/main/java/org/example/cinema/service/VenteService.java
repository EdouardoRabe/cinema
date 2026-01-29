package org.example.cinema.service;

import org.example.cinema.model.*;
import org.example.cinema.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VenteService {

    private final VenteRepository venteRepository;
    private final VenteDetailRepository venteDetailRepository;
    private final PaiementVenteRepository paiementVenteRepository;
    private final ProduitService produitService;

    public VenteService(VenteRepository venteRepository, 
                       VenteDetailRepository venteDetailRepository,
                       PaiementVenteRepository paiementVenteRepository,
                       ProduitService produitService) {
        this.venteRepository = venteRepository;
        this.venteDetailRepository = venteDetailRepository;
        this.paiementVenteRepository = paiementVenteRepository;
        this.produitService = produitService;
    }

    public List<Vente> findAll() {
        return venteRepository.findAllOrderByDateDesc();
    }

    public Optional<Vente> findById(Long id) {
        return venteRepository.findById(id);
    }

    public List<Vente> findByMoisAnnee(int mois, int annee) {
        return venteRepository.findByMoisAnnee(mois, annee);
    }

    @Transactional
    public Vente save(Vente vente) {
        return venteRepository.save(vente);
    }

    @Transactional
    public void deleteById(Long id) {
        venteRepository.deleteById(id);
    }

    @Transactional
    public VenteDetail addDetail(Vente vente, Produit produit, int quantite) {
        BigDecimal prixUnitaire = produitService.getPrixAtDateValue(produit.getId(), vente.getDateVente());
        
        VenteDetail detail = VenteDetail.builder()
                .vente(vente)
                .produit(produit)
                .quantite(quantite)
                .prixUnitaire(prixUnitaire)
                .build();
        
        return venteDetailRepository.save(detail);
    }

    @Transactional
    public VenteDetail saveDetail(VenteDetail detail) {
        return venteDetailRepository.save(detail);
    }

    @Transactional
    public void deleteDetail(Long detailId) {
        venteDetailRepository.deleteById(detailId);
    }

    public BigDecimal getMontantTotal(Long venteId) {
        return venteDetailRepository.getMontantTotalByVenteId(venteId);
    }

    public BigDecimal getTotalPaye(Long venteId) {
        return paiementVenteRepository.getTotalPayeByVenteId(venteId);
    }

    public BigDecimal getResteAPayer(Long venteId) {
        BigDecimal total = getMontantTotal(venteId);
        BigDecimal paye = getTotalPaye(venteId);
        return total.subtract(paye);
    }

    @Transactional
    public PaiementVente addPaiement(Vente vente, BigDecimal montant, LocalDate datePaiement) {
        PaiementVente paiement = PaiementVente.builder()
                .vente(vente)
                .montantPaye(montant)
                .datePaiement(datePaiement)
                .build();
        return paiementVenteRepository.save(paiement);
    }

    public List<PaiementVente> getPaiements(Long venteId) {
        return paiementVenteRepository.findByVenteId(venteId);
    }

    public BigDecimal getCAVentesByMoisAnnee(int mois, int annee) {
        return paiementVenteRepository.getTotalPayeByMoisAnnee(mois, annee);
    }
}
