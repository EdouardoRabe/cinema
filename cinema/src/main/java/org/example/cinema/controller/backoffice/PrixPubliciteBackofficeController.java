package org.example.cinema.controller.backoffice;

import org.example.cinema.model.PrixPublicite;
import org.example.cinema.service.PrixPubliciteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/backoffice/prix-publicite")
public class PrixPubliciteBackofficeController {

    private final PrixPubliciteService prixPubliciteService;

    public PrixPubliciteBackofficeController(PrixPubliciteService prixPubliciteService) {
        this.prixPubliciteService = prixPubliciteService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("prixList", prixPubliciteService.findAll());
        model.addAttribute("prixActuel", prixPubliciteService.getPrixActuel());
        return "backoffice/prix-publicite";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("prixPublicite", new PrixPublicite());
        model.addAttribute("prixActuel", prixPubliciteService.getPrixActuel());
        return "backoffice/prix-publicite-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam("prix") BigDecimal prix,
                       @RequestParam("dateCreation") String dateCreationStr,
                       RedirectAttributes ra) {
        try {
            LocalDateTime dateCreation = LocalDateTime.parse(dateCreationStr);
            prixPubliciteService.createNewPrix(prix, dateCreation);
            ra.addFlashAttribute("successMessage", "Nouveau prix enregistré avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
        }
        return "redirect:/backoffice/prix-publicite";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        prixPubliciteService.deleteById(id);
        ra.addFlashAttribute("successMessage", "Prix supprimé");
        return "redirect:/backoffice/prix-publicite";
    }
}
