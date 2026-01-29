package org.example.cinema.controller.backoffice;

import org.example.cinema.model.*;
import org.example.cinema.repository.VenteDetailRepository;
import org.example.cinema.service.ProduitService;
import org.example.cinema.service.VenteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/backoffice/ventes")
public class VenteBackofficeController {

    private final VenteService venteService;
    private final ProduitService produitService;
    private final VenteDetailRepository venteDetailRepository;

    public VenteBackofficeController(VenteService venteService, ProduitService produitService, VenteDetailRepository venteDetailRepository) {
        this.venteService = venteService;
        this.produitService = produitService;
        this.venteDetailRepository = venteDetailRepository;
    }

    @GetMapping
    public String list(@RequestParam(value = "mois", required = false) Integer mois,
                       @RequestParam(value = "annee", required = false) Integer annee,
                       Model model) {
        if (mois == null) mois = LocalDate.now().getMonthValue();
        if (annee == null) annee = Year.now().getValue();

        var ventes = venteService.findByMoisAnnee(mois, annee);
        
        Map<Long, BigDecimal> montantsTotaux = new HashMap<>();
        Map<Long, BigDecimal> totalPayeMap = new HashMap<>();
        Map<Long, BigDecimal> resteAPayerMap = new HashMap<>();
        
        for (Vente v : ventes) {
            montantsTotaux.put(v.getId(), venteService.getMontantTotal(v.getId()));
            totalPayeMap.put(v.getId(), venteService.getTotalPaye(v.getId()));
            resteAPayerMap.put(v.getId(), venteService.getResteAPayer(v.getId()));
        }

        model.addAttribute("ventes", ventes);
        model.addAttribute("montantsTotaux", montantsTotaux);
        model.addAttribute("totalPayeMap", totalPayeMap);
        model.addAttribute("resteAPayerMap", resteAPayerMap);
        model.addAttribute("selectedMois", mois);
        model.addAttribute("selectedAnnee", annee);
        model.addAttribute("nomMois", getNomMois(mois));
        
        return "backoffice/ventes";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("produits", produitService.findAll());
        return "backoffice/vente-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam("dateVente") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateVente,
                       @RequestParam("produitIds") List<Long> produitIds,
                       @RequestParam("quantites") List<Integer> quantites,
                       RedirectAttributes ra) {
        
        if (produitIds == null || produitIds.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Veuillez ajouter au moins un produit");
            return "redirect:/backoffice/ventes/create";
        }
        
        // Créer la vente
        Vente vente = Vente.builder()
                .dateVente(dateVente)
                .build();
        vente = venteService.save(vente);
        
        // Ajouter tous les détails avec le prix automatique selon la date de vente
        for (int i = 0; i < produitIds.size(); i++) {
            var produitOpt = produitService.findById(produitIds.get(i));
            if (produitOpt.isPresent()) {
                // Récupérer le prix du produit à la date de la vente
                BigDecimal prixUnitaire = produitService.getPrixAtDateValue(produitIds.get(i), dateVente);
                if (prixUnitaire == null || prixUnitaire.compareTo(BigDecimal.ZERO) <= 0) {
                    ra.addFlashAttribute("errorMessage", "Le produit '" + produitOpt.get().getLibelle() + "' n'a pas de prix défini pour la date " + dateVente);
                    venteService.deleteById(vente.getId());
                    return "redirect:/backoffice/ventes/create";
                }
                
                VenteDetail detail = VenteDetail.builder()
                        .vente(vente)
                        .produit(produitOpt.get())
                        .quantite(quantites.get(i))
                        .prixUnitaire(prixUnitaire)
                        .build();
                venteDetailRepository.save(detail);
            }
        }
        
        ra.addFlashAttribute("successMessage", "Vente créée avec succès");
        return "redirect:/backoffice/ventes/view/" + vente.getId();
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        var venteOpt = venteService.findById(id);
        if (venteOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vente introuvable");
            return "redirect:/backoffice/ventes";
        }
        
        Vente vente = venteOpt.get();
        model.addAttribute("vente", vente);
        model.addAttribute("produits", produitService.findAll());
        model.addAttribute("montantTotal", venteService.getMontantTotal(id));
        model.addAttribute("totalPaye", venteService.getTotalPaye(id));
        model.addAttribute("resteAPayer", venteService.getResteAPayer(id));
        model.addAttribute("paiements", venteService.getPaiements(id));
        
        // Prix actuels des produits à la date de la vente
        Map<Long, BigDecimal> prixProduits = new HashMap<>();
        for (Produit p : produitService.findAll()) {
            prixProduits.put(p.getId(), produitService.getPrixAtDateValue(p.getId(), vente.getDateVente()));
        }
        model.addAttribute("prixProduits", prixProduits);
        
        return "backoffice/vente-view";
    }

    @PostMapping("/detail/add/{venteId}")
    public String addDetail(@PathVariable("venteId") Long venteId,
                            @RequestParam("produitId") Long produitId,
                            @RequestParam("quantite") Integer quantite,
                            RedirectAttributes ra) {
        var venteOpt = venteService.findById(venteId);
        var produitOpt = produitService.findById(produitId);
        
        if (venteOpt.isEmpty() || produitOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vente ou produit introuvable");
            return "redirect:/backoffice/ventes";
        }
        
        venteService.addDetail(venteOpt.get(), produitOpt.get(), quantite);
        ra.addFlashAttribute("successMessage", "Produit ajouté à la vente");
        return "redirect:/backoffice/ventes/view/" + venteId;
    }

    @PostMapping("/detail/delete/{detailId}")
    public String deleteDetail(@PathVariable("detailId") Long detailId,
                               @RequestParam("venteId") Long venteId,
                               RedirectAttributes ra) {
        venteService.deleteDetail(detailId);
        ra.addFlashAttribute("successMessage", "Ligne supprimée");
        return "redirect:/backoffice/ventes/view/" + venteId;
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            venteService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Vente supprimée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Impossible de supprimer cette vente");
        }
        return "redirect:/backoffice/ventes";
    }

    @GetMapping("/paiement/{id}")
    public String paiementForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        var venteOpt = venteService.findById(id);
        if (venteOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vente introuvable");
            return "redirect:/backoffice/ventes";
        }
        
        Vente vente = venteOpt.get();
        model.addAttribute("vente", vente);
        model.addAttribute("montantTotal", venteService.getMontantTotal(id));
        model.addAttribute("totalPaye", venteService.getTotalPaye(id));
        model.addAttribute("resteAPayer", venteService.getResteAPayer(id));
        model.addAttribute("paiements", venteService.getPaiements(id));
        
        return "backoffice/vente-paiement";
    }

    @PostMapping("/paiement/{id}/save")
    public String savePaiement(@PathVariable("id") Long id,
                               @RequestParam("montant") BigDecimal montant,
                               @RequestParam("datePaiement") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePaiement,
                               RedirectAttributes ra) {
        var venteOpt = venteService.findById(id);
        if (venteOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vente introuvable");
            return "redirect:/backoffice/ventes";
        }
        
        venteService.addPaiement(venteOpt.get(), montant, datePaiement);
        ra.addFlashAttribute("successMessage", "Paiement enregistré avec succès");
        return "redirect:/backoffice/ventes/paiement/" + id;
    }

    private String getNomMois(int mois) {
        String[] noms = {"", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return noms[mois];
    }
}
