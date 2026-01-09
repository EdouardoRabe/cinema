package org.example.cinema.controller.backoffice;

import org.example.cinema.model.CategoriePersonne;
import org.example.cinema.model.Client;
import org.example.cinema.model.Place;
import org.example.cinema.model.Reservation;
import org.example.cinema.model.Seance;
import org.example.cinema.service.CategoriePersonneService;
import org.example.cinema.service.ClientService;
import org.example.cinema.service.PlaceService;
import org.example.cinema.service.ReservationService;
import org.example.cinema.service.SeanceService;
import org.example.cinema.service.TarifService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/backoffice/reservations")
public class ReservationBackofficeController {

    private final ReservationService reservationService;
    private final SeanceService seanceService;
    private final PlaceService placeService;
    private final CategoriePersonneService categorieService;
    private final TarifService tarifService;
    private final ClientService clientService;
    private final org.example.cinema.service.StatutReservationService statutReservationService;

    public ReservationBackofficeController(ReservationService reservationService,
                                           SeanceService seanceService,
                                           PlaceService placeService,
                                           CategoriePersonneService categorieService,
                                           TarifService tarifService,
                                           ClientService clientService,
                                           org.example.cinema.service.StatutReservationService statutReservationService) {
        this.reservationService = reservationService;
        this.seanceService = seanceService;
        this.placeService = placeService;
        this.categorieService = categorieService;
        this.tarifService = tarifService;
        this.clientService = clientService;
        this.statutReservationService = statutReservationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reservations", reservationService.findAll());
        model.addAttribute("statuts", statutReservationService.findAll());
        return "backoffice/reservations";
    }

    @PostMapping("/change-status/{id}")
    public String changeStatus(@PathVariable("id") Long id, @RequestParam(name = "statutId") Long statutId, RedirectAttributes redirectAttributes) {
        try {
            reservationService.updateStatus(id, statutId);
            redirectAttributes.addFlashAttribute("successMessage", "Statut mis à jour");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
        }
        return "redirect:/backoffice/reservations";
    }

    @GetMapping("/create")
    public String create(@RequestParam(name = "seanceId", required = false) Long seanceId, Model model) {
        List<Client> clients = clientService.findAll();
        model.addAttribute("clients", clients);

        List<CategoriePersonne> categories = categorieService.findAll();
        model.addAttribute("categories", categories);

        if (seanceId != null) {
            Optional<Seance> seanceOpt = seanceService.findById(seanceId);
            if (seanceOpt.isEmpty()) {
                return "redirect:/backoffice/reservations";
            }
            Seance seance = seanceOpt.get();
            List<Place> places = placeService.findBySalleId(seance.getSalle().getId());
            Map<String, List<Place>> placesByRow = placeService.groupByRow(places);
            Set<Long> placesOccupees = reservationService.getOccupiedPlaceIds(seanceId);

            model.addAttribute("seance", seance);
            model.addAttribute("placesByRow", placesByRow);
            model.addAttribute("rows", new TreeSet<>(placesByRow.keySet()));
            model.addAttribute("placesOccupees", placesOccupees);
            model.addAttribute("tarifsByCat", tarifService.getTarifsStandardByCategorie());
            model.addAttribute("tarifDefaut", tarifService.getTarifDefautAdulte());
        }

        return "backoffice/reservation-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(name = "seanceId") Long seanceId,
                       @RequestParam(name = "selectedSeats") String selectedSeats,
                       @RequestParam(name = "clientId") Long clientId,
                       @RequestParam(name = "defaultCategoryId", required = false) Long defaultCategoryId,
                       RedirectAttributes redirectAttributes) {
        try {
            if (selectedSeats == null || selectedSeats.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Veuillez sélectionner au moins une place");
                return "redirect:/backoffice/reservations/create?seanceId=" + seanceId;
            }

            Optional<Seance> seanceOpt = seanceService.findById(seanceId);
            if (seanceOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Séance non trouvée");
                return "redirect:/backoffice/reservations";
            }
            Seance seance = seanceOpt.get();

            Map<Long, Long> placesWithCategories = new HashMap<>();
            String[] entries = selectedSeats.split(",");
            for (String entry : entries) {
                String[] parts = entry.trim().split(":");
                if (parts.length >= 2) {
                    Long placeId = Long.parseLong(parts[0]);
                    Long catId = Long.parseLong(parts[1]);
                    placesWithCategories.put(placeId, catId);
                } else if (parts.length == 1 && defaultCategoryId != null) {
                    Long placeId = Long.parseLong(parts[0]);
                    placesWithCategories.put(placeId, defaultCategoryId);
                }
            }

            if (placesWithCategories.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Format de sélection invalide. Assurez-vous d'avoir choisi une catégorie pour chaque place ou de définir une catégorie par défaut.");
                return "redirect:/backoffice/reservations/create?seanceId=" + seanceId;
            }

            // Check occupancy
            Set<Long> occupied = reservationService.getOccupiedPlaceIds(seanceId);
            for (Long pId : placesWithCategories.keySet()) {
                if (occupied.contains(pId)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Une ou plusieurs places sont déjà occupées");
                    return "redirect:/backoffice/reservations/create?seanceId=" + seanceId;
                }
            }

            Client client = clientService.findById(clientId).orElseThrow(() -> new RuntimeException("Client non trouvé"));
            List<CategoriePersonne> allCategories = categorieService.findAll();
            Map<Long, CategoriePersonne> categoriesMap = new HashMap<>();
            for (CategoriePersonne c : allCategories) categoriesMap.put(c.getId(), c);

            Reservation reservation = reservationService.createReservation(client, seance, placesWithCategories, categoriesMap);
            redirectAttributes.addFlashAttribute("successMessage", "Réservation créée (ID: " + reservation.getId() + ")");
            return "redirect:/backoffice/reservations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/backoffice/reservations";
        }
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        var opt = reservationService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Réservation introuvable");
            return "redirect:/backoffice/reservations";
        }
        var reservation = opt.get();
        model.addAttribute("reservation", reservation);
        model.addAttribute("history", reservationService.getHistoryForReservation(reservation.getId()));
        return "backoffice/reservation-view";
    }
}