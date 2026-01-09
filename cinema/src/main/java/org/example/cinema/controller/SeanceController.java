package org.example.cinema.controller;

import org.example.cinema.service.FilmService;
import org.example.cinema.service.SalleService;
import org.example.cinema.service.SeanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/seances")
public class SeanceController {

    private final SeanceService seanceService;
    private final FilmService filmService;
    private final SalleService salleService;

    public SeanceController(SeanceService seanceService, FilmService filmService, SalleService salleService) {
        this.seanceService = seanceService;
        this.filmService = filmService;
        this.salleService = salleService;
    }

    @GetMapping
    public String listSeances(Model model,
                              @RequestParam(name = "filmId", required = false) Long filmId,
                              @RequestParam(name = "salleId", required = false) Long salleId,
                              @RequestParam(name = "date", required = false) String date) {
        
        LocalDate filterDate = null;
        if (date != null && !date.isEmpty()) {
            filterDate = LocalDate.parse(date);
        }

        // Utilise la méthode avec tous les filtres combinés
        model.addAttribute("seances", seanceService.findWithFilters(filmId, salleId, filterDate));

        model.addAttribute("films", filmService.findAll());
        model.addAttribute("salles", salleService.findAll());
        model.addAttribute("selectedFilmId", filmId);
        model.addAttribute("selectedSalleId", salleId);
        model.addAttribute("selectedDate", date);
        return "seances";
    }
}
