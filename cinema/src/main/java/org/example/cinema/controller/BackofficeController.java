package org.example.cinema.controller;

import org.example.cinema.model.Film;
import org.example.cinema.model.Reservation;
import org.example.cinema.model.Seance;
import org.example.cinema.service.FilmService;
import org.example.cinema.service.ReservationService;
import org.example.cinema.service.SeanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/backoffice")
public class BackofficeController {

    private final FilmService filmService;
    private final SeanceService seanceService;
    private final ReservationService reservationService;

    public BackofficeController(FilmService filmService, SeanceService seanceService, ReservationService reservationService) {
        this.filmService = filmService;
        this.seanceService = seanceService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public String index(Model model) {
        List<Film> films = filmService.findAll();
        List<Seance> seances = seanceService.findUpcoming();
        List<Reservation> reservations = reservationService.findAll();

        // Provide counts for quick overview
        model.addAttribute("filmCount", films != null ? films.size() : 0);
        model.addAttribute("seanceCount", seances != null ? seances.size() : 0);
        model.addAttribute("reservationCount", reservations != null ? reservations.size() : 0);

        return "backoffice/index";
    }


}
