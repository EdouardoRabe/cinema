package org.example.cinema.controller.backoffice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.cinema.model.Place;
import org.example.cinema.model.Seance;
import org.example.cinema.model.TarifSeance;
import org.example.cinema.model.TypePlace;
import org.example.cinema.repository.TarifSeanceRepository;
import org.example.cinema.repository.TypePlaceRepository;
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

@Controller
@RequestMapping("/backoffice/seances")
public class SeanceBackofficeController {

    private final SeanceService seanceService;
    private final PlaceService placeService;
    private final ReservationService reservationService;
    private final TarifService tarifService;
    private final TypePlaceRepository typePlaceRepository;
    private final TarifSeanceRepository tarifSeanceRepository;
    private final CategoriePersonneService categoriePersonneService;

    private final org.example.cinema.service.FilmService filmService;
    private final org.example.cinema.service.SalleService salleService;

    public SeanceBackofficeController(SeanceService seanceService, PlaceService placeService,
            ReservationService reservationService, TarifService tarifService,
            TypePlaceRepository typePlaceRepository, TarifSeanceRepository tarifSeanceRepository,
            CategoriePersonneService categoriePersonneService,
            org.example.cinema.service.FilmService filmService, org.example.cinema.service.SalleService salleService) {
        this.seanceService = seanceService;
        this.placeService = placeService;
        this.reservationService = reservationService;
        this.tarifService = tarifService;
        this.typePlaceRepository = typePlaceRepository;
        this.tarifSeanceRepository = tarifSeanceRepository;
        this.categoriePersonneService = categoriePersonneService;
        this.filmService = filmService;
        this.salleService = salleService;
    }

    @GetMapping
    public String list(@RequestParam(name = "filmId", required = false) Long filmId,
            @RequestParam(name = "salleId", required = false) Long salleId,
            @RequestParam(name = "dateFrom", required = false) String dateFromStr,
            @RequestParam(name = "dateTo", required = false) String dateToStr,
            Model model) {
        java.time.LocalDate dateFrom = null;
        java.time.LocalDate dateTo = null;
        if (dateFromStr != null && !dateFromStr.isBlank()) {
            dateFrom = java.time.LocalDate.parse(dateFromStr);
        }
        if (dateToStr != null && !dateToStr.isBlank()) {
            dateTo = java.time.LocalDate.parse(dateToStr);
        }
        // Utiliser findWithFiltersBackoffice pour inclure TOUTES les séances (passées et futures)
        List<Seance> seances = seanceService.findWithFiltersBackoffice(filmId, salleId, dateFrom, dateTo);

        List<Long> seanceIds = seances.stream().map(Seance::getId).toList();
        java.util.Map<Long, java.math.BigDecimal> chiffresAffaires = reservationService
                .getChiffreAffairesBySeances(seanceIds);

        // CA maximal théorique par séance (somme des places * tarif max par type de
        // place)
        java.util.Map<Long, java.math.BigDecimal> capacitesMax = new java.util.HashMap<>();
        for (Seance seance : seances) {
            java.util.Map<Long, java.math.BigDecimal> maxTarifs = tarifService
                    .getMaxTarifByTypePlaceForSeance(seance.getId());
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (Place p : placeService.findBySalleId(seance.getSalle().getId())) {
                if (p.getTypePlace() == null) {
                    continue;
                }
                java.math.BigDecimal prix = maxTarifs.get(p.getTypePlace().getId());
                if (prix != null) {
                    total = total.add(prix);
                }
            }
            capacitesMax.put(seance.getId(), total);
        }

        model.addAttribute("seances", seances);
        model.addAttribute("chiffresAffaires", chiffresAffaires);
        model.addAttribute("capacitesMax", capacitesMax);
        model.addAttribute("films", filmService.findAll());
        model.addAttribute("salles", salleService.findAll());
        model.addAttribute("selectedFilm", filmId);
        model.addAttribute("selectedSalle", salleId);
        model.addAttribute("selectedDateFrom", dateFromStr);
        model.addAttribute("selectedDateTo", dateToStr);
        return "backoffice/seances";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("seance", new Seance());
        model.addAttribute("films", filmService.findAll());
        model.addAttribute("salles", salleService.findAll());
        model.addAttribute("typePlaces", typePlaceRepository.findAll());
        model.addAttribute("tarifsExistants", Map.of());
        return "backoffice/seance-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        var sOpt = seanceService.findById(id);
        if (sOpt.isEmpty())
            return "redirect:/backoffice/seances";
        model.addAttribute("seance", sOpt.get());
        model.addAttribute("films", filmService.findAll());
        model.addAttribute("salles", salleService.findAll());
        model.addAttribute("typePlaces", typePlaceRepository.findAll());
        Map<Long, BigDecimal> tarifsExistants = new HashMap<>();
        for (TarifSeance ts : tarifSeanceRepository.findBySeanceId(id)) {
            if (ts.getTypePlace() != null && !tarifsExistants.containsKey(ts.getTypePlace().getId())) {
                tarifsExistants.put(ts.getTypePlace().getId(), ts.getPrix());
            }
        }
        model.addAttribute("tarifsExistants", tarifsExistants);
        return "backoffice/seance-form";
    }

    @PostMapping("/save")
    public String save(Seance seance, @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes, Model model) {
        
        // Validation manuelle
        List<String> errors = new ArrayList<>();
        
        if (seance.getFilm() == null || seance.getFilm().getId() == null) {
            errors.add("Veuillez sélectionner un film");
        }
        if (seance.getSalle() == null || seance.getSalle().getId() == null) {
            errors.add("Veuillez sélectionner une salle");
        }
        if (seance.getDebut() == null) {
            errors.add("Veuillez saisir une date et heure de début");
        }
        if (seance.getLangue() == null || seance.getLangue().isBlank()) {
            errors.add("Veuillez sélectionner une langue");
        }
        
        // Si erreurs de validation, retourner au formulaire
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("seance", seance);
            model.addAttribute("films", filmService.findAll());
            model.addAttribute("salles", salleService.findAll());
            model.addAttribute("typePlaces", typePlaceRepository.findAll());
            model.addAttribute("tarifsExistants", Map.of());
            return "backoffice/seance-form";
        }
        
        try {
            seanceService.save(seance);

            // Supprimer les tarifs existants de la séance (si édition)
            tarifSeanceRepository.deleteBySeanceId(seance.getId());

            List<TypePlace> typePlaces = typePlaceRepository.findAll();
            Map<Long, TypePlace> typePlaceMap = new HashMap<>();
            for (TypePlace tp : typePlaces) {
                typePlaceMap.put(tp.getId(), tp);
            }

            var categories = categoriePersonneService.findAll();
            List<TarifSeance> toSave = new ArrayList<>();

            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();
                if (!key.startsWith("tarifsTypePlace[")) {
                    continue;
                }
                if (key.length() <= "tarifsTypePlace[".length())
                    continue;
                String rawId = key.substring("tarifsTypePlace[".length(), key.length() - 1);
                if (rawId.isBlank())
                    continue;
                Long tpId = Long.valueOf(rawId);
                String value = entry.getValue();
                if (value == null || value.isBlank())
                    continue;
                BigDecimal prix = new BigDecimal(value);
                TypePlace tp = typePlaceMap.get(tpId);
                if (tp == null)
                    continue;
                for (var cat : categories) {
                    toSave.add(TarifSeance.builder()
                            .seance(seance)
                            .typePlace(tp)
                            .categoriePersonne(cat)
                            .prix(prix)
                            .build());
                }
            }

            if (!toSave.isEmpty()) {
                tarifSeanceRepository.saveAll(toSave);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Séance enregistrée avec succès !");
            return "redirect:/backoffice/seances";
        } catch (Exception e) {
            // En cas d'erreur, retourner au formulaire avec le message d'erreur
            model.addAttribute("errorMessage", "Erreur lors de l'enregistrement : " + e.getMessage());
            model.addAttribute("seance", seance);
            model.addAttribute("films", filmService.findAll());
            model.addAttribute("salles", salleService.findAll());
            model.addAttribute("typePlaces", typePlaceRepository.findAll());
            
            // Récupérer les tarifs saisis pour les réafficher
            Map<Long, BigDecimal> tarifsExistants = new HashMap<>();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("tarifsTypePlace[") && key.endsWith("]")) {
                    String rawId = key.substring("tarifsTypePlace[".length(), key.length() - 1);
                    if (!rawId.isBlank() && entry.getValue() != null && !entry.getValue().isBlank()) {
                        try {
                            tarifsExistants.put(Long.valueOf(rawId), new BigDecimal(entry.getValue()));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            model.addAttribute("tarifsExistants", tarifsExistants);
            return "backoffice/seance-form";
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
