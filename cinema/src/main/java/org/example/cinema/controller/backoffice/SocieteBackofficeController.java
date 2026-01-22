package org.example.cinema.controller.backoffice;

import org.example.cinema.model.Societe;
import org.example.cinema.service.SocieteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/backoffice/societes")
public class SocieteBackofficeController {

    private final SocieteService societeService;

    public SocieteBackofficeController(SocieteService societeService) {
        this.societeService = societeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("societes", societeService.findAll());
        return "backoffice/societes";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("societe", new Societe());
        return "backoffice/societe-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Societe societe, RedirectAttributes ra) {
        // Vérifier si le libellé existe déjà (pour une nouvelle société)
        if (societe.getId() == null && societeService.existsByLibelle(societe.getLibelle())) {
            ra.addFlashAttribute("errorMessage", "Une société avec ce nom existe déjà");
            return "redirect:/backoffice/societes/create";
        }
        
        societeService.save(societe);
        ra.addFlashAttribute("successMessage", "Société enregistrée avec succès");
        return "redirect:/backoffice/societes";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        var societeOpt = societeService.findById(id);
        if (societeOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Société introuvable");
            return "redirect:/backoffice/societes";
        }
        model.addAttribute("societe", societeOpt.get());
        return "backoffice/societe-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            societeService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Société supprimée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Impossible de supprimer cette société (elle a des publicités associées)");
        }
        return "redirect:/backoffice/societes";
    }
}
