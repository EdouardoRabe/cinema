package org.example.cinema.controller.backoffice;

import org.example.cinema.model.Publicite;
import org.example.cinema.service.PubliciteService;
import org.example.cinema.service.SocieteService;
import org.example.cinema.service.PrixPubliciteService;
import org.example.cinema.service.PaiementPubliciteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/backoffice/publicites")
public class PubliciteBackofficeController {

    private final PubliciteService publiciteService;
    private final SocieteService societeService;
    private final PrixPubliciteService prixPubliciteService;
    private final PaiementPubliciteService paiementPubliciteService;

    public PubliciteBackofficeController(PubliciteService publiciteService, 
                                         SocieteService societeService,
                                         PrixPubliciteService prixPubliciteService,
                                         PaiementPubliciteService paiementPubliciteService) {
        this.publiciteService = publiciteService;
        this.societeService = societeService;
        this.prixPubliciteService = prixPubliciteService;
        this.paiementPubliciteService = paiementPubliciteService;
    }

    @GetMapping
    public String list(@RequestParam(name = "annee", required = false) Integer annee,
                       @RequestParam(name = "mois", required = false) Integer mois,
                       Model model) {
        
        List<Publicite> publicites;
        
        // Filtrage
        if (annee != null && mois != null) {
            publicites = publiciteService.findByYearMonth(annee, mois);
        } else if (annee != null) {
            publicites = publiciteService.findByYear(annee);
        } else {
            publicites = publiciteService.findAll();
        }
        
        // Calcul du CA pour le filtre actuel (basé sur les paiements)
        Map<String, Object> caDetail = null;
        if (annee != null && mois != null) {
            caDetail = publiciteService.getDetailCAPourMois(annee, mois);
            // Ajouter le CA réel (paiements effectués)
            BigDecimal caReel = paiementPubliciteService.getCAPourMois(annee, mois);
            caDetail.put("caReel", caReel);
        }
        
        // Statistiques par mois si une année est sélectionnée
        List<Map<String, Object>> statsParMois = null;
        if (annee != null) {
            statsParMois = publiciteService.getStatistiquesParMois(annee);
            // Ajouter le CA réel à chaque mois
            for (Map<String, Object> stat : statsParMois) {
                Integer m = (Integer) stat.get("mois");
                BigDecimal caReel = paiementPubliciteService.getCAPourMois(annee, m);
                stat.put("caReel", caReel);
            }
        }
        
        // Map des montants pour chaque publicité (calculés avec le prix du mois correspondant)
        Map<Long, java.math.BigDecimal> montantsMap = publiciteService.getMontantsMap(publicites);
        
        // Map des restes à payer pour chaque publicité
        Map<Long, BigDecimal> restesAPayerMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> totalPayeMap = new LinkedHashMap<>();
        for (Publicite pub : publicites) {
            restesAPayerMap.put(pub.getId(), paiementPubliciteService.getResteAPayer(pub));
            totalPayeMap.put(pub.getId(), paiementPubliciteService.getTotalPaye(pub.getId()));
        }
        
        model.addAttribute("publicites", publicites);
        model.addAttribute("montantsMap", montantsMap);
        model.addAttribute("restesAPayerMap", restesAPayerMap);
        model.addAttribute("totalPayeMap", totalPayeMap);
        model.addAttribute("selectedAnnee", annee);
        model.addAttribute("selectedMois", mois);
        model.addAttribute("caDetail", caDetail);
        model.addAttribute("statsParMois", statsParMois);
        model.addAttribute("prixActuel", prixPubliciteService.getPrixActuel());
        
        return "backoffice/publicites";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("societes", societeService.findAll());
        model.addAttribute("moisDisponibles", publiciteService.getMoisDisponibles());
        return "backoffice/publicite-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam("societeId") Long societeId,
                       @RequestParam("dateDiffusion") String dateDiffusionStr,
                       @RequestParam("nbFois") Integer nbFois,
                       RedirectAttributes ra) {
        try {
            LocalDate dateDiffusion = LocalDate.parse(dateDiffusionStr + "-01");
            publiciteService.addOrUpdateDiffusion(societeId, dateDiffusion, nbFois);
            ra.addFlashAttribute("successMessage", "Diffusion publicitaire enregistrée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
        }
        return "redirect:/backoffice/publicites";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        var pubOpt = publiciteService.findById(id);
        if (pubOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Publicité introuvable");
            return "redirect:/backoffice/publicites";
        }
        
        Publicite pub = pubOpt.get();
        model.addAttribute("publicite", pub);
        model.addAttribute("societes", societeService.findAll());
        model.addAttribute("prixActuel", prixPubliciteService.getPrixActuel());
        
        return "backoffice/publicite-edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @RequestParam("nbFois") Integer nbFois,
                         RedirectAttributes ra) {
        var pubOpt = publiciteService.findById(id);
        if (pubOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Publicité introuvable");
            return "redirect:/backoffice/publicites";
        }
        
        Publicite pub = pubOpt.get();
        pub.setNbFois(nbFois);
        publiciteService.save(pub);
        
        ra.addFlashAttribute("successMessage", "Diffusion mise à jour avec succès");
        return "redirect:/backoffice/publicites";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        publiciteService.deleteById(id);
        ra.addFlashAttribute("successMessage", "Diffusion supprimée avec succès");
        return "redirect:/backoffice/publicites";
    }
}
