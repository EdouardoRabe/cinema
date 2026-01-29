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

        BigDecimal caTickets = chiffreAffaireService.getCATicketsByMoisAnnee(mois, annee);
        BigDecimal caPublicites = chiffreAffaireService.getCAPublicitesByMoisAnnee(mois, annee);
        BigDecimal caVentes = chiffreAffaireService.getCAVentesByMoisAnnee(mois, annee);
        BigDecimal caTotal = chiffreAffaireService.getCATotalByMoisAnnee(mois, annee);

        model.addAttribute("selectedMois", mois);
        model.addAttribute("selectedAnnee", annee);
        model.addAttribute("nomMois", getNomMois(mois));
        model.addAttribute("caTickets", caTickets);
        model.addAttribute("caPublicites", caPublicites);
        model.addAttribute("caVentes", caVentes);
        model.addAttribute("caTotal", caTotal);

        return "backoffice/chiffre-affaire";
    }

    private String getNomMois(int mois) {
        String[] noms = {"", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return noms[mois];
    }
}
