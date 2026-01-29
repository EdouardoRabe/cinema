package org.example.cinema.service;

import org.example.cinema.model.Produit;
import org.example.cinema.model.PrixProduit;
import org.example.cinema.repository.ProduitRepository;
import org.example.cinema.repository.PrixProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final PrixProduitRepository prixProduitRepository;

    public ProduitService(ProduitRepository produitRepository, PrixProduitRepository prixProduitRepository) {
        this.produitRepository = produitRepository;
        this.prixProduitRepository = prixProduitRepository;
    }

    public List<Produit> findAll() {
        return produitRepository.findAll();
    }

    public Optional<Produit> findById(Long id) {
        return produitRepository.findById(id);
    }

    public boolean existsByLibelle(String libelle) {
        return produitRepository.existsByLibelle(libelle);
    }

    @Transactional
    public Produit save(Produit produit) {
        return produitRepository.save(produit);
    }

    @Transactional
    public void deleteById(Long id) {
        produitRepository.deleteById(id);
    }

    public Optional<PrixProduit> getPrixActuel(Long produitId) {
        return prixProduitRepository.findLatestByProduitId(produitId);
    }

    public Optional<PrixProduit> getPrixAtDate(Long produitId, LocalDate date) {
        return prixProduitRepository.findPrixAtDate(produitId, date);
    }

    public BigDecimal getPrixActuelValue(Long produitId) {
        return prixProduitRepository.findLatestByProduitId(produitId)
                .map(PrixProduit::getPrix)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getPrixAtDateValue(Long produitId, LocalDate date) {
        return prixProduitRepository.findPrixAtDate(produitId, date)
                .map(PrixProduit::getPrix)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public PrixProduit savePrix(PrixProduit prixProduit) {
        return prixProduitRepository.save(prixProduit);
    }

    public List<PrixProduit> getHistoriquePrix(Long produitId) {
        return prixProduitRepository.findByProduitIdOrderByDatePrixDesc(produitId);
    }
}
