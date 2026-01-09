package org.example.cinema.controller;

import jakarta.servlet.http.HttpSession;
import org.example.cinema.model.CategoriePersonne;
import org.example.cinema.model.Client;
import org.example.cinema.model.Place;
import org.example.cinema.model.Seance;
import org.example.cinema.model.Reservation;
import org.example.cinema.service.CategoriePersonneService;
import org.example.cinema.service.PlaceService;
import org.example.cinema.service.ReservationService;
import org.example.cinema.service.SeanceService;
import org.example.cinema.service.TarifService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/reservation")
public class ReservationController {

    private final SeanceService seanceService;
    private final PlaceService placeService;
    private final CategoriePersonneService categorieService;
    private final TarifService tarifService;
    private final ReservationService reservationService;

    public ReservationController(SeanceService seanceService, PlaceService placeService, 
                                  CategoriePersonneService categorieService,
                                  TarifService tarifService,
                                  ReservationService reservationService) {
        this.seanceService = seanceService;
        this.placeService = placeService;
        this.categorieService = categorieService;
        this.tarifService = tarifService;
        this.reservationService = reservationService;
    }

    @GetMapping("/{seanceId}")
    public String showReservation(@PathVariable("seanceId") Long seanceId, Model model, HttpSession session,
                                   @RequestParam(name = "success", required = false) String success,
                                   @RequestParam(name = "error", required = false) String error) {
        Optional<Seance> seanceOpt = seanceService.findById(seanceId);
        if (seanceOpt.isEmpty()) {
            return "redirect:/seances";
        }

        Seance seance = seanceOpt.get();
        List<Place> places = placeService.findBySalleId(seance.getSalle().getId());
        Map<String, List<Place>> placesByRow = placeService.groupByRow(places);

        // Récupérer les places déjà occupées pour cette séance
        Set<Long> placesOccupees = reservationService.getOccupiedPlaceIds(seanceId);

        // Récupérer les tarifs depuis la base de données
        List<CategoriePersonne> categories = categorieService.findAll();
        Map<Long, BigDecimal> tarifsByCat = tarifService.getTarifsStandardByCategorie();
        BigDecimal tarifDefaut = tarifService.getTarifDefautAdulte();

        // Construire le JSON des catégories pour JavaScript
        Map<Long, Map<String, Object>> categoriesJson = new HashMap<>();
        for (CategoriePersonne cat : categories) {
            Map<String, Object> catData = new HashMap<>();
            catData.put("id", cat.getId());
            catData.put("name", cat.getLibelle());
            BigDecimal prix = tarifsByCat.get(cat.getId());
            catData.put("price", prix != null ? prix.intValue() : tarifDefaut.intValue());
            categoriesJson.put(cat.getId(), catData);
        }

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
        model.addAttribute("categoriesJson", categoriesJson);
        model.addAttribute("client", client);
        model.addAttribute("isConnected", client != null);
        
        // Messages de succès/erreur
        if (success != null) {
            model.addAttribute("successMessage", "Votre réservation a été confirmée avec succès !");
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "reservation";
    }

    @PostMapping("/confirm")
    public String confirmReservation(@RequestParam("seanceId") Long seanceId,
                                      @RequestParam("selectedSeats") String selectedSeats,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        // Vérifier si l'utilisateur est connecté
        Client client = (Client) session.getAttribute("client");
        if (client == null) {
            return "redirect:/login";
        }

        try {
            // Vérifier que des places ont été sélectionnées
            if (selectedSeats == null || selectedSeats.isEmpty()) {
                redirectAttributes.addAttribute("error", "Veuillez sélectionner au moins une place");
                return "redirect:/reservation/" + seanceId;
            }

            // Récupérer la séance
            Optional<Seance> seanceOpt = seanceService.findById(seanceId);
            if (seanceOpt.isEmpty()) {
                redirectAttributes.addAttribute("error", "Séance non trouvée");
                return "redirect:/seances";
            }
            Seance seance = seanceOpt.get();

            // Parser les places sélectionnées avec leurs catégories
            // Format: "placeId:categorieId,placeId:categorieId,..."
            Map<Long, Long> placesWithCategories = new HashMap<>();
            String[] seatEntries = selectedSeats.split(",");
            
            for (String entry : seatEntries) {
                String[] parts = entry.trim().split(":");
                if (parts.length >= 2) {
                    Long placeId = Long.parseLong(parts[0]);
                    Long categorieId = Long.parseLong(parts[1]);
                    placesWithCategories.put(placeId, categorieId);
                }
            }

            if (placesWithCategories.isEmpty()) {
                redirectAttributes.addAttribute("error", "Format de sélection invalide");
                return "redirect:/reservation/" + seanceId;
            }

            // Vérifier que les places ne sont pas déjà occupées
            Set<Long> occupiedPlaces = reservationService.getOccupiedPlaceIds(seanceId);
            for (Long placeId : placesWithCategories.keySet()) {
                if (occupiedPlaces.contains(placeId)) {
                    redirectAttributes.addAttribute("error", "Une ou plusieurs places sélectionnées sont déjà occupées");
                    return "redirect:/reservation/" + seanceId;
                }
            }

            // Récupérer la map des catégories
            List<CategoriePersonne> allCategories = categorieService.findAll();
            Map<Long, CategoriePersonne> categoriesMap = new HashMap<>();
            for (CategoriePersonne cat : allCategories) {
                categoriesMap.put(cat.getId(), cat);
            }

            // Créer la réservation
            reservationService.createReservation(client, seance, placesWithCategories, categoriesMap);

            redirectAttributes.addAttribute("success", "true");
            return "redirect:/reservation/" + seanceId;

        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Erreur lors de la réservation: " + e.getMessage());
            return "redirect:/reservation/" + seanceId;
        }
    }

    @GetMapping("/mes-reservations")
    public String mesReservations(Model model, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        if (client == null) {
            return "redirect:/login";
        }

        List<Reservation> reservations = reservationService.findByClientId(client.getId());
        model.addAttribute("reservations", reservations);
        return "mes-reservations";
    }
}
