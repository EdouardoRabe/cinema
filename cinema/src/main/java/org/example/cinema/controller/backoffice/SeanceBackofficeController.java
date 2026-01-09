package org.example.cinema.controller.backoffice;

import org.example.cinema.model.Place;
import org.example.cinema.model.Seance;
import org.example.cinema.service.PlaceService;
import org.example.cinema.service.ReservationService;
import org.example.cinema.service.SeanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/backoffice/seances")
public class SeanceBackofficeController {

    private final SeanceService seanceService;
    private final PlaceService placeService;
    private final ReservationService reservationService;

    private final org.example.cinema.service.FilmService filmService;
    private final org.example.cinema.service.SalleService salleService;

    public SeanceBackofficeController(SeanceService seanceService, PlaceService placeService, ReservationService reservationService, org.example.cinema.service.FilmService filmService, org.example.cinema.service.SalleService salleService) {
        this.seanceService = seanceService;
        this.placeService = placeService;
        this.reservationService = reservationService;
        this.filmService = filmService;
        this.salleService = salleService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("seances", seanceService.findUpcoming());
        return "backoffice/seances";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("seance", new Seance());
        model.addAttribute("films", filmService.findAll());
        model.addAttribute("salles", salleService.findAll());
        return "backoffice/seance-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        var sOpt = seanceService.findById(id);
        if (sOpt.isEmpty()) return "redirect:/backoffice/seances";
        model.addAttribute("seance", sOpt.get());
        model.addAttribute("films", filmService.findAll());
        model.addAttribute("salles", salleService.findAll());
        return "backoffice/seance-form";
    }

    @PostMapping("/save")
    public String save(Seance seance, RedirectAttributes redirectAttributes) {
        try {
            seanceService.save(seance);
            redirectAttributes.addFlashAttribute("successMessage", "Séance enregistrée");
            return "redirect:/backoffice/seances";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/backoffice/seances";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            seanceService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Séance supprimée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
        }
        return "redirect:/backoffice/seances";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") Long id, Model model) {
        var seanceOpt = seanceService.findById(id);
        if (seanceOpt.isEmpty()) {
            return "redirect:/backoffice/seances";
        }
        Seance seance = seanceOpt.get();
        List<Place> places = placeService.findBySalleId(seance.getSalle().getId());
        Map<String, List<Place>> placesByRow = placeService.groupByRow(places);
        var occupied = reservationService.getOccupiedPlaceIds(id);

        model.addAttribute("seance", seance);
        model.addAttribute("placesByRow", placesByRow);
        model.addAttribute("rows", new java.util.TreeSet<>(placesByRow.keySet()));
        model.addAttribute("placesOccupees", occupied);
        return "backoffice/seance-view";
    }
}
