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
                            @RequestParam(required = false) Long genre,
                            @RequestParam(required = false) String langue,
                            @RequestParam(required = false) String date) {
        model.addAttribute("films", filmService.findAll());
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("selectedLangue", langue);
        model.addAttribute("selectedDate", date);
        return "films";
    }

    @GetMapping("/{id}")
    public String filmDetail(@PathVariable Long id, Model model) {
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
