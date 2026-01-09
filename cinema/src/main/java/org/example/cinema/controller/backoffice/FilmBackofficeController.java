package org.example.cinema.controller.backoffice;

import org.example.cinema.model.Film;
import org.example.cinema.service.FilmService;
import org.example.cinema.service.GenreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/backoffice/films")
public class FilmBackofficeController {

    private final FilmService filmService;
    private final GenreService genreService;
    private final org.example.cinema.service.SeanceService seanceService;

    public FilmBackofficeController(FilmService filmService, GenreService genreService, org.example.cinema.service.SeanceService seanceService) {
        this.filmService = filmService;
        this.genreService = genreService;
        this.seanceService = seanceService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String title,
                       @RequestParam(required = false, name = "genreId") String genreIdStr,
                       @RequestParam(required = false) String langue,
                       @RequestParam(required = false) String dateFrom,
                       @RequestParam(required = false) String dateTo,
                       Model model) {
        LocalDate from = dateFrom != null && !dateFrom.isEmpty() ? LocalDate.parse(dateFrom) : null;
        LocalDate to = dateTo != null && !dateTo.isEmpty() ? LocalDate.parse(dateTo) : null;
        Long genreId = null;
        if (genreIdStr != null && !genreIdStr.isBlank()) {
            try {
                genreId = Long.parseLong(genreIdStr);
            } catch (NumberFormatException ignored) {
            }
        }
        List<Film> films = filmService.findWithFiltersAdvanced(title, genreId, langue, from, to);
        model.addAttribute("films", films);
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("title", title);
        model.addAttribute("selectedGenre", genreId);
        model.addAttribute("selectedLangue", langue);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        return "backoffice/films-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("film", new Film());
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("selectedGenreIds", java.util.Collections.emptyList());
        return "backoffice/film-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Film film,
                       BindingResult br,
                       @RequestParam(required = false, name = "genreIds") java.util.List<Long> genreIds,
                       Model model,
                       RedirectAttributes ra) {
        if (br.hasErrors()) {
            // repopulate required model attributes for the form
            model.addAttribute("genres", genreService.findAll());
            model.addAttribute("selectedGenreIds", genreIds != null ? genreIds : java.util.Collections.emptyList());
            return "backoffice/film-form";
        }
        if (genreIds != null && !genreIds.isEmpty()) {
            var genres = genreService.findByIds(genreIds);
            film.setGenres(new java.util.HashSet<>(genres));
        } else {
            film.setGenres(new java.util.HashSet<>());
        }
        filmService.save(film);
        ra.addFlashAttribute("successMessage", "Film enregistré avec succès");
        return "redirect:/backoffice/films";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        var filmOpt = filmService.findById(id);
        if (filmOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Film introuvable");
            return "redirect:/backoffice/films";
        }
        var film = filmOpt.get();
        model.addAttribute("film", film);
        model.addAttribute("genres", genreService.findAll());
        // prepare selected ids for the multi-select
        var selected = film.getGenres().stream().map(g -> g.getId()).toList();
        model.addAttribute("selectedGenreIds", selected);
        return "backoffice/film-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        filmService.deleteById(id);
        ra.addFlashAttribute("successMessage", "Film supprimé");
        return "redirect:/backoffice/films";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes ra) {
        var filmOpt = filmService.findById(id);
        if (filmOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Film introuvable");
            return "redirect:/backoffice/films";
        }
        var film = filmOpt.get();
        model.addAttribute("film", film);
        model.addAttribute("seances", seanceService.findByFilmId(film.getId()));
        return "backoffice/film-view";
    }
}
