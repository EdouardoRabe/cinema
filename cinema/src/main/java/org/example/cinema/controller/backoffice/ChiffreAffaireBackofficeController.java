package org.example.cinema.controller.backoffice;

import org.example.cinema.service.ChiffreAffaireService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

@Controller
@RequestMapping("/backoffice/chiffre-affaire")
public class ChiffreAffaireBackofficeController {

    private final ChiffreAffaireService chiffreAffaireService;

    public ChiffreAffaireBackofficeController(ChiffreAffaireService chiffreAffaireService) {
        this.chiffreAffaireService = chiffreAffaireService;
    }

    @GetMapping
    public String index(@RequestParam(value = "mois", required = false) Integer mois,
                        @RequestParam(value = "annee", required = false) Integer annee,
                        Model model) {
        if (mois == null) mois = LocalDate.now().getMonthValue();
        if (annee == null) annee = Year.now().getValue();

        // CA Théorique (basé sur réservations/pubs/ventes, pas sur paiements)
        BigDecimal caTickets = chiffreAffaireService.getCATicketsTheoriqueByMoisAnnee(mois, annee);
        BigDecimal caPublicites = chiffreAffaireService.getCAPublicitesTheoriqueByMoisAnnee(mois, annee);
        BigDecimal caVentes = chiffreAffaireService.getCAVentesTheoriqueByMoisAnnee(mois, annee);
        BigDecimal caTotal = chiffreAffaireService.getCATotalTheoriqueByMoisAnnee(mois, annee);

        // Total payé
        BigDecimal payeTickets = chiffreAffaireService.getTotalPayeTicketsByMoisAnnee(mois, annee);
        BigDecimal payePublicites = chiffreAffaireService.getTotalPayePublicitesByMoisAnnee(mois, annee);
        BigDecimal payeVentes = chiffreAffaireService.getTotalPayeVentesByMoisAnnee(mois, annee);
        BigDecimal payeTotal = chiffreAffaireService.getTotalPayeByMoisAnnee(mois, annee);

        // Reste à payer
        BigDecimal resteTickets = chiffreAffaireService.getResteAPayerTicketsByMoisAnnee(mois, annee);
        BigDecimal restePublicites = chiffreAffaireService.getResteAPayerPublicitesByMoisAnnee(mois, annee);
        BigDecimal resteVentes = chiffreAffaireService.getResteAPayerVentesByMoisAnnee(mois, annee);
        BigDecimal resteTotal = chiffreAffaireService.getResteAPayerTotalByMoisAnnee(mois, annee);

        model.addAttribute("selectedMois", mois);
        model.addAttribute("selectedAnnee", annee);
        model.addAttribute("nomMois", getNomMois(mois));
        
        // CA Théorique
        model.addAttribute("caTickets", caTickets);
        model.addAttribute("caPublicites", caPublicites);
        model.addAttribute("caVentes", caVentes);
        model.addAttribute("caTotal", caTotal);
        
        // Total payé
        model.addAttribute("payeTickets", payeTickets);
        model.addAttribute("payePublicites", payePublicites);
        model.addAttribute("payeVentes", payeVentes);
        model.addAttribute("payeTotal", payeTotal);
        
        // Reste à payer
        model.addAttribute("resteTickets", resteTickets);
        model.addAttribute("restePublicites", restePublicites);
        model.addAttribute("resteVentes", resteVentes);
        model.addAttribute("resteTotal", resteTotal);

        return "backoffice/chiffre-affaire";
    }

    private String getNomMois(int mois) {
        String[] noms = {"", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return noms[mois];
    }
}
