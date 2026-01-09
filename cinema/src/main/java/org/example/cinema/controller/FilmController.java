package org.example.cinema.controller;

import org.example.cinema.model.Film;
import org.example.cinema.service.FilmService;
import org.example.cinema.service.GenreService;
import org.example.cinema.service.SeanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final GenreService genreService;
    private final SeanceService seanceService;

    public FilmController(FilmService filmService, GenreService genreService, SeanceService seanceService) {
        this.filmService = filmService;
        this.genreService = genreService;
        this.seanceService = seanceService;
    }

    @GetMapping
    public String listFilms(Model model,
                            @RequestParam(name = "genre", required = false) Long genre,
                            @RequestParam(name = "langue", required = false) String langue,
                            @RequestParam(name = "date", required = false) String date) {
        
        LocalDate filterDate = null;
        if (date != null && !date.isEmpty()) {
            filterDate = LocalDate.parse(date);
        }

        // Utilise la méthode avec filtres combinés
        model.addAttribute("films", filmService.findWithFilters(genre, langue, filterDate));
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("selectedLangue", langue);
        model.addAttribute("selectedDate", date);
        return "films";
    }

    @GetMapping("/{id}")
    public String filmDetail(@PathVariable("id") Long id, Model model) {
        Optional<Film> filmOpt = filmService.findById(id);
        if (filmOpt.isEmpty()) {
            return "redirect:/films";
        }
        Film film = filmOpt.get();
        model.addAttribute("film", film);
        model.addAttribute("genres", genreService.findAll()); // TODO: filter by film
        model.addAttribute("seances", seanceService.findByFilmId(id));
        return "film-detail";
    }
}
