package org.example.cinema.controller.backoffice;

import org.example.cinema.model.Publicite;
import org.example.cinema.model.PubliciteDetail;
import org.example.cinema.model.Seance;
import org.example.cinema.service.PubliciteService;
import org.example.cinema.service.SeanceService;
import org.example.cinema.service.SocieteService;
import org.example.cinema.service.PrixPubliciteService;
import org.example.cinema.service.PaiementPubliciteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/backoffice/publicites")
public class PubliciteBackofficeController {

    private final PubliciteService publiciteService;
    private final SocieteService societeService;
    private final SeanceService seanceService;
    private final PrixPubliciteService prixPubliciteService;
    private final PaiementPubliciteService paiementPubliciteService;

    public PubliciteBackofficeController(PubliciteService publiciteService, 
                                         SocieteService societeService,
                                         SeanceService seanceService,
                                         PrixPubliciteService prixPubliciteService,
                                         PaiementPubliciteService paiementPubliciteService) {
        this.publiciteService = publiciteService;
        this.societeService = societeService;
        this.seanceService = seanceService;
        this.prixPubliciteService = prixPubliciteService;
        this.paiementPubliciteService = paiementPubliciteService;
    }

    @GetMapping
    public String list(Model model) {
        List<Publicite> publicites = publiciteService.findAll();
        
        // Map des montants totaux pour chaque publicité
        Map<Long, BigDecimal> montantsTotaux = publiciteService.getMontantsTotaux(publicites);
        
        // Map des totaux payés et restes à payer
        Map<Long, BigDecimal> totalPayeMap = paiementPubliciteService.getTotauxPayes(publicites);
        Map<Long, BigDecimal> restesAPayerMap = paiementPubliciteService.getRestesAPayer(publicites);
        
        // Map du nombre total de diffusions
        Map<Long, Integer> totalNbFoisMap = new LinkedHashMap<>();
        for (Publicite pub : publicites) {
            totalNbFoisMap.put(pub.getId(), publiciteService.getTotalNbFois(pub));
        }
        
        model.addAttribute("publicites", publicites);
        model.addAttribute("montantsTotaux", montantsTotaux);
        model.addAttribute("totalPayeMap", totalPayeMap);
        model.addAttribute("restesAPayerMap", restesAPayerMap);
        model.addAttribute("totalNbFoisMap", totalNbFoisMap);
        model.addAttribute("prixActuel", prixPubliciteService.getPrixActuel());
        
        return "backoffice/publicites";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("societes", societeService.findAll());
        model.addAttribute("seances", seanceService.findAllWithFilmAndSalle());
        return "backoffice/publicite-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam("societeId") Long societeId,
                       @RequestParam(value = "seanceIds", required = false) List<Long> seanceIds,
                       @RequestParam(value = "nbFoisList", required = false) List<Integer> nbFoisList,
                       RedirectAttributes ra) {
        try {
            if (seanceIds == null || seanceIds.isEmpty()) {
                ra.addFlashAttribute("errorMessage", "Veuillez ajouter au moins une séance");
                return "redirect:/backoffice/publicites/create";
            }
            if (nbFoisList == null || nbFoisList.size() != seanceIds.size()) {
                ra.addFlashAttribute("errorMessage", "Erreur: nombre de diffusions manquant pour certaines séances");
                return "redirect:/backoffice/publicites/create";
            }
            publiciteService.creerPublicite(societeId, seanceIds, nbFoisList);
            ra.addFlashAttribute("successMessage", "Publicité créée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
        }
        return "redirect:/backoffice/publicites";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Publicite pub = publiciteService.findByIdWithDetails(id);
        if (pub == null) {
            ra.addFlashAttribute("errorMessage", "Publicité introuvable");
            return "redirect:/backoffice/publicites";
        }
        
        BigDecimal montantTotal = publiciteService.calculerMontantTotal(pub);
        BigDecimal totalPaye = paiementPubliciteService.getTotalPaye(id);
        BigDecimal resteAPayer = paiementPubliciteService.getResteAPayer(pub);
        BigDecimal pourcentagePaye = paiementPubliciteService.getPourcentagePaye(id);
        
        // Calcul des montants par détail
        Map<Long, BigDecimal> montantsDetails = new LinkedHashMap<>();
        Map<Long, BigDecimal> montantsPayesDetails = new LinkedHashMap<>();
        Map<Long, BigDecimal> restesPayerDetails = new LinkedHashMap<>();
        
        for (PubliciteDetail detail : pub.getDetails()) {
            BigDecimal montantDetail = publiciteService.calculerMontantDetail(detail);
            BigDecimal montantPayeDetail = publiciteService.calculerMontantPayeDetail(detail, totalPaye, montantTotal);
            BigDecimal resteDetail = publiciteService.calculerMontantRestantDetail(detail, totalPaye, montantTotal);
            
            montantsDetails.put(detail.getId(), montantDetail);
            montantsPayesDetails.put(detail.getId(), montantPayeDetail);
            restesPayerDetails.put(detail.getId(), resteDetail);
        }
        
        model.addAttribute("publicite", pub);
        model.addAttribute("montantTotal", montantTotal);
        model.addAttribute("totalPaye", totalPaye);
        model.addAttribute("resteAPayer", resteAPayer);
        model.addAttribute("pourcentagePaye", pourcentagePaye);
        model.addAttribute("montantsDetails", montantsDetails);
        model.addAttribute("montantsPayesDetails", montantsPayesDetails);
        model.addAttribute("restesPayerDetails", restesPayerDetails);
        
        return "backoffice/publicite-detail";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            publiciteService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Publicité supprimée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/backoffice/publicites";
    }
}
