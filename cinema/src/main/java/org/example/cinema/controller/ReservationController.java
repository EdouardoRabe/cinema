package org.example.cinema.controller;

import jakarta.servlet.http.HttpSession;
import org.example.cinema.model.CategoriePersonne;
import org.example.cinema.model.Client;
import org.example.cinema.model.Place;
import org.example.cinema.model.Seance;
import org.example.cinema.service.CategoriePersonneService;
import org.example.cinema.service.PlaceService;
import org.example.cinema.service.SeanceService;
import org.example.cinema.service.TarifService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/reservation")
public class ReservationController {

    private final SeanceService seanceService;
    private final PlaceService placeService;
    private final CategoriePersonneService categorieService;
    private final TarifService tarifService;

    public ReservationController(SeanceService seanceService, PlaceService placeService, 
                                  CategoriePersonneService categorieService,
                                  TarifService tarifService) {
        this.seanceService = seanceService;
        this.placeService = placeService;
        this.categorieService = categorieService;
        this.tarifService = tarifService;
    }

    @GetMapping("/{seanceId}")
    public String showReservation(@PathVariable Long seanceId, Model model, HttpSession session) {
        Optional<Seance> seanceOpt = seanceService.findById(seanceId);
        if (seanceOpt.isEmpty()) {
            return "redirect:/seances";
        }

        Seance seance = seanceOpt.get();
        List<Place> places = placeService.findBySalleId(seance.getSalle().getId());
        Map<String, List<Place>> placesByRow = placeService.groupByRow(places);

        // TODO: fetch actually occupied seats from tickets
        Set<Long> placesOccupees = new HashSet<>();

        // Récupérer les tarifs depuis la base de données
        List<CategoriePersonne> categories = categorieService.findAll();
        Map<Long, BigDecimal> tarifsByCat = tarifService.getTarifsStandardByCategorie();
        BigDecimal tarifDefaut = tarifService.getTarifDefautAdulte();

        // Client connecté (peut être null)
        Client client = (Client) session.getAttribute("client");

        model.addAttribute("seance", seance);
        model.addAttribute("places", places);
        model.addAttribute("placesByRow", placesByRow);
        model.addAttribute("rows", new TreeSet<>(placesByRow.keySet()));
        model.addAttribute("placesOccupees", placesOccupees);
        model.addAttribute("categories", categories);
        model.addAttribute("tarifsByCat", tarifsByCat);
        model.addAttribute("tarifDefaut", tarifDefaut);
        model.addAttribute("client", client);
        model.addAttribute("isConnected", client != null);

        return "reservation";
    }

    @PostMapping("/confirm")
    public String confirmReservation(@RequestParam Long seanceId,
                                      @RequestParam String selectedSeats,
                                      @RequestParam(required = false) Long categoryId,
                                      HttpSession session) {
        // Vérifier si l'utilisateur est connecté
        Client client = (Client) session.getAttribute("client");
        if (client == null) {
            return "redirect:/login";
        }

        // TODO: implement reservation logic
        // 1. Parse selectedSeats (comma-separated place IDs)
        // 2. Create Reservation + Tickets
        // 3. Redirect to confirmation page

        return "redirect:/reservation/" + seanceId + "?success=true";
    }
}
