package org.example.cinema.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.example.cinema.model.CategoriePersonne;
import org.example.cinema.model.Client;
import org.example.cinema.model.Place;
import org.example.cinema.model.Reservation;
import org.example.cinema.model.Seance;
import org.example.cinema.service.CategoriePersonneService;
import org.example.cinema.service.PlaceService;
import org.example.cinema.service.ReservationService;
import org.example.cinema.service.SeanceService;
import org.example.cinema.service.TarifService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

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

        // Récupérer les tarifs depuis la base de données : priorité tarifs de séance,
        // sinon tarifs par défaut
        List<CategoriePersonne> categories = categorieService.findAll();
        // Identifier les types de place présents dans la salle de la séance
        Set<Long> typePlaceIds = places.stream()
                .filter(p -> p.getTypePlace() != null)
                .map(p -> p.getTypePlace().getId())
                .collect(Collectors.toSet());

        // Map imbriquée pour le front: categorieId -> { id, name, prices: { typePlaceId
        // -> prix } }
        Map<Long, Map<String, Object>> categoriesJson = new HashMap<>();
        for (CategoriePersonne cat : categories) {
            Map<String, Object> catData = new HashMap<>();
            catData.put("id", cat.getId());
            catData.put("name", cat.getLibelle());

            Map<Long, Integer> pricesByType = new HashMap<>();
            for (Long typePlaceId : typePlaceIds) {
                tarifService.findTarifForSeanceOrDefault(seanceId, typePlaceId, cat.getId())
                        .map(BigDecimal::intValue)
                        .ifPresent(price -> pricesByType.put(typePlaceId, price));
            }
            catData.put("prices", pricesByType);
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
                redirectAttributes.addAttribute("error",
                        "Format de sélection invalide. Assurez-vous d'avoir choisi une catégorie pour chaque place.");
                return "redirect:/reservation/" + seanceId;
            }

            // Vérifier que les places ne sont pas déjà occupées
            Set<Long> occupiedPlaces = reservationService.getOccupiedPlaceIds(seanceId);
            for (Long placeId : placesWithCategories.keySet()) {
                if (occupiedPlaces.contains(placeId)) {
                    redirectAttributes.addAttribute("error",
                            "Une ou plusieurs places sélectionnées sont déjà occupées");
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
