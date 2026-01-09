package org.example.cinema.controller;

import org.example.cinema.service.FilmService;
import org.example.cinema.service.SeanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final FilmService filmService;
    private final SeanceService seanceService;

    public HomeController(FilmService filmService, SeanceService seanceService) {
        this.filmService = filmService;
        this.seanceService = seanceService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("films", filmService.findAll());
        model.addAttribute("seances", seanceService.findUpcoming(5));
        return "index";
    }
}
