package org.example.cinema.controller.backoffice;

import org.example.cinema.model.PaiementPublicite;
import org.example.cinema.model.Publicite;
import org.example.cinema.model.PubliciteDetail;
import org.example.cinema.service.PaiementPubliciteService;
import org.example.cinema.service.PubliciteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/backoffice/paiements-publicite")
public class PaiementPubliciteBackofficeController {

    private final PaiementPubliciteService paiementService;
    private final PubliciteService publiciteService;

    public PaiementPubliciteBackofficeController(PaiementPubliciteService paiementService,
                                                  PubliciteService publiciteService) {
        this.paiementService = paiementService;
        this.publiciteService = publiciteService;
    }

    /**
     * Affiche la page de paiement pour une publicité
     */
    @GetMapping("/{publiciteId}")
    public String afficherPaiements(@PathVariable("publiciteId") Long publiciteId, Model model, RedirectAttributes ra) {
        Publicite pub = publiciteService.findByIdWithDetails(publiciteId);
        if (pub == null) {
            ra.addFlashAttribute("errorMessage", "Publicité introuvable");
            return "redirect:/backoffice/publicites";
        }

        List<PaiementPublicite> paiements = paiementService.findByPubliciteId(publiciteId);
        
        BigDecimal montantTotal = publiciteService.calculerMontantTotal(pub);
        BigDecimal totalPaye = paiementService.getTotalPaye(publiciteId);
        BigDecimal resteAPayer = paiementService.getResteAPayer(pub);
        BigDecimal pourcentagePaye = paiementService.getPourcentagePaye(publiciteId);

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
        model.addAttribute("paiements", paiements);
        model.addAttribute("montantTotal", montantTotal);
        model.addAttribute("totalPaye", totalPaye);
        model.addAttribute("resteAPayer", resteAPayer);
        model.addAttribute("pourcentagePaye", pourcentagePaye);
        model.addAttribute("montantsDetails", montantsDetails);
        model.addAttribute("montantsPayesDetails", montantsPayesDetails);
        model.addAttribute("restesPayerDetails", restesPayerDetails);

        return "backoffice/paiements-publicite";
    }

    /**
     * Enregistre un nouveau paiement
     */
    @PostMapping("/{publiciteId}/payer")
    public String payer(@PathVariable("publiciteId") Long publiciteId,
                        @RequestParam("montant") BigDecimal montant,
                        RedirectAttributes ra) {
        try {
            paiementService.enregistrerPaiement(publiciteId, montant);
            ra.addFlashAttribute("successMessage", "Paiement de " + montant + " Ar enregistré avec succès");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Erreur lors de l'enregistrement du paiement");
        }
        return "redirect:/backoffice/paiements-publicite/" + publiciteId;
    }

    /**
     * Supprime un paiement
     */
    @PostMapping("/{publiciteId}/supprimer/{paiementId}")
    public String supprimerPaiement(@PathVariable("publiciteId") Long publiciteId,
                                    @PathVariable("paiementId") Long paiementId,
                                    RedirectAttributes ra) {
        try {
            paiementService.deleteById(paiementId);
            ra.addFlashAttribute("successMessage", "Paiement supprimé");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Erreur lors de la suppression");
        }
        return "redirect:/backoffice/paiements-publicite/" + publiciteId;
    }
}
