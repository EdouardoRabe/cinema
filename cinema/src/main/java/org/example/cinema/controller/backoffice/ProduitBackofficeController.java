package org.example.cinema.controller.backoffice;

import org.example.cinema.model.Produit;
import org.example.cinema.model.PrixProduit;
import org.example.cinema.service.ProduitService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/backoffice/produits")
public class ProduitBackofficeController {

    private final ProduitService produitService;

    public ProduitBackofficeController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @GetMapping
    public String list(Model model) {
        var produits = produitService.findAll();
        Map<Long, BigDecimal> prixActuels = new HashMap<>();
        for (Produit p : produits) {
            prixActuels.put(p.getId(), produitService.getPrixActuelValue(p.getId()));
        }
        model.addAttribute("produits", produits);
        model.addAttribute("prixActuels", prixActuels);
        return "backoffice/produits";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("produit", new Produit());
        return "backoffice/produit-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Produit produit, RedirectAttributes ra) {
        if (produit.getId() == null && produitService.existsByLibelle(produit.getLibelle())) {
            ra.addFlashAttribute("errorMessage", "Un produit avec ce nom existe déjà");
            return "redirect:/backoffice/produits/create";
        }
        produitService.save(produit);
        ra.addFlashAttribute("successMessage", "Produit enregistré avec succès");
        return "redirect:/backoffice/produits";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        var produitOpt = produitService.findById(id);
        if (produitOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Produit introuvable");
            return "redirect:/backoffice/produits";
        }
        model.addAttribute("produit", produitOpt.get());
        return "backoffice/produit-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            produitService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Produit supprimé avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Impossible de supprimer ce produit");
        }
        return "redirect:/backoffice/produits";
    }

    @GetMapping("/prix/{id}")
    public String prixForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        var produitOpt = produitService.findById(id);
        if (produitOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Produit introuvable");
            return "redirect:/backoffice/produits";
        }
        model.addAttribute("produit", produitOpt.get());
        model.addAttribute("historique", produitService.getHistoriquePrix(id));
        return "backoffice/produit-prix-form";
    }

    @PostMapping("/prix/{id}/save")
    public String savePrix(@PathVariable("id") Long id,
                           @RequestParam("prix") BigDecimal prix,
                           @RequestParam("datePrix") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datePrix,
                           RedirectAttributes ra) {
        var produitOpt = produitService.findById(id);
        if (produitOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Produit introuvable");
            return "redirect:/backoffice/produits";
        }
        
        PrixProduit prixProduit = PrixProduit.builder()
                .produit(produitOpt.get())
                .prix(prix)
                .datePrix(datePrix)
                .build();
        produitService.savePrix(prixProduit);
        ra.addFlashAttribute("successMessage", "Prix enregistré avec succès");
        return "redirect:/backoffice/produits/prix/" + id;
    }
}
