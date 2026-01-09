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
                              @RequestParam(required = false) Long filmId,
                              @RequestParam(required = false) Long salleId,
                              @RequestParam(required = false) String date) {
        
        if (date != null && !date.isEmpty()) {
            model.addAttribute("seances", seanceService.findByDate(LocalDate.parse(date)));
        } else if (filmId != null) {
            model.addAttribute("seances", seanceService.findByFilmId(filmId));
        } else {
            model.addAttribute("seances", seanceService.findUpcoming());
        }

        model.addAttribute("films", filmService.findAll());
        model.addAttribute("salles", salleService.findAll());
        model.addAttribute("selectedFilmId", filmId);
        model.addAttribute("selectedSalleId", salleId);
        model.addAttribute("selectedDate", date);
        return "seances";
    }
}
