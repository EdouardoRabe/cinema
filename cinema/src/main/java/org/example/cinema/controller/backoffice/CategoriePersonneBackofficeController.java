package org.example.cinema.controller.backoffice;

import org.example.cinema.model.CategoriePersonne;
import org.example.cinema.service.CategoriePersonneService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/backoffice/categories-personne")
public class CategoriePersonneBackofficeController {

    private final CategoriePersonneService categoriePersonneService;

    public CategoriePersonneBackofficeController(CategoriePersonneService categoriePersonneService) {
        this.categoriePersonneService = categoriePersonneService;
    }

    @GetMapping
    public String list(Model model) {
        List<CategoriePersonne> categories = categoriePersonneService.findAll();
        model.addAttribute("categories", categories);
        return "backoffice/categories-personne";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("categorie", new CategoriePersonne());
        return "backoffice/categorie-personne-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        var catOpt = categoriePersonneService.findById(id);
        if (catOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Catégorie non trouvée");
            return "redirect:/backoffice/categories-personne";
        }
        model.addAttribute("categorie", catOpt.get());
        return "backoffice/categorie-personne-form";
    }

    @PostMapping("/save")
    public String save(CategoriePersonne categorie, RedirectAttributes redirectAttributes, Model model) {
        // Validation
        if (categorie.getLibelle() == null || categorie.getLibelle().isBlank()) {
            model.addAttribute("errorMessage", "Le libellé est obligatoire");
            model.addAttribute("categorie", categorie);
            return "backoffice/categorie-personne-form";
        }

        try {
            categoriePersonneService.save(categorie);
            redirectAttributes.addFlashAttribute("successMessage", "Catégorie enregistrée avec succès !");
            return "redirect:/backoffice/categories-personne";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors de l'enregistrement : " + e.getMessage());
            model.addAttribute("categorie", categorie);
            return "backoffice/categorie-personne-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriePersonneService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Catégorie supprimée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/backoffice/categories-personne";
    }
}
