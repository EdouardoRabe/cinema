package org.example.cinema.controller.backoffice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.cinema.model.CategoriePersonne;
import org.example.cinema.model.Place;
import org.example.cinema.model.Remise;
import org.example.cinema.model.Seance;
import org.example.cinema.model.TarifSeance;
import org.example.cinema.model.TypePlace;
import org.example.cinema.repository.RemiseRepository;
import org.example.cinema.repository.TarifSeanceRepository;
import org.example.cinema.repository.TypePlaceRepository;
import org.example.cinema.service.CategoriePersonneService;
import org.example.cinema.service.PaiementPubliciteService;
import org.example.cinema.service.PlaceService;
import org.example.cinema.service.PubliciteService;
import org.example.cinema.service.RemiseService;
import org.example.cinema.service.ReservationService;
import org.example.cinema.service.SeanceService;
import org.example.cinema.service.TarifService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    private final RemiseRepository remiseRepository;
    private final RemiseService remiseService;
    private final PubliciteService publiciteService;
    private final PaiementPubliciteService paiementPubliciteService;

    private final org.example.cinema.service.FilmService filmService;
    private final org.example.cinema.service.SalleService salleService;

    public SeanceBackofficeController(SeanceService seanceService, PlaceService placeService,
            ReservationService reservationService, TarifService tarifService,
            TypePlaceRepository typePlaceRepository, TarifSeanceRepository tarifSeanceRepository,
            CategoriePersonneService categoriePersonneService,
            RemiseRepository remiseRepository, RemiseService remiseService,
            PubliciteService publiciteService, PaiementPubliciteService paiementPubliciteService,
            org.example.cinema.service.FilmService filmService, org.example.cinema.service.SalleService salleService) {
        this.seanceService = seanceService;
        this.placeService = placeService;
        this.reservationService = reservationService;
        this.tarifService = tarifService;
        this.typePlaceRepository = typePlaceRepository;
        this.tarifSeanceRepository = tarifSeanceRepository;
        this.categoriePersonneService = categoriePersonneService;
        this.remiseRepository = remiseRepository;
        this.remiseService = remiseService;
        this.publiciteService = publiciteService;
        this.paiementPubliciteService = paiementPubliciteService;
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
        List<Seance> seances = seanceService.findWithFiltersBackoffice(filmId, salleId, dateFrom, dateTo);

        List<Long> seanceIds = seances.stream().map(Seance::getId).toList();
        
        // CA réel (basé sur les réservations payées) = Montant généré par ticket
        java.util.Map<Long, java.math.BigDecimal> chiffresAffaires = reservationService
                .getChiffreAffairesBySeances(seanceIds);

        // CA maximal théorique par séance (somme des places * tarif max par type de place)
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

        // Montant généré par pub (montant payé au prorata par séance)
        java.util.Map<Long, java.math.BigDecimal> montantsPub = publiciteService.calculerMontantsPubPayesPourSeances(
                seanceIds,
                pubId -> paiementPubliciteService.getTotalPaye(pubId)
        );

        model.addAttribute("seances", seances);
        model.addAttribute("chiffresAffaires", chiffresAffaires);
        model.addAttribute("montantsPub", montantsPub);
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
        model.addAttribute("categories", categoriePersonneService.findAll());
        model.addAttribute("tarifsExistants", Map.of());
        model.addAttribute("remisesExistantes", Map.of());
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
        model.addAttribute("categories", categoriePersonneService.findAll());
        
        Map<String, BigDecimal> tarifsExistants = new HashMap<>();
        for (TarifSeance ts : tarifSeanceRepository.findLatestBySeanceId(id)) {
            if (ts.getTypePlace() != null && ts.getCategoriePersonne() != null) {
                String key = ts.getTypePlace().getId() + "_" + ts.getCategoriePersonne().getId();
                tarifsExistants.put(key, ts.getPrix()); // peut être null si remise
            }
        }
        model.addAttribute("tarifsExistants", tarifsExistants);
        
        Map<String, String> remisesExistantes = new HashMap<>();
        for (Remise r : remiseService.findLatestActiveBySeanceId(id)) {
            if (r.getTypePlace() != null && r.getCategoriePersonneCible() != null && r.getCategoriePersonneRepere() != null) {
                String key = r.getTypePlace().getId() + "_" + r.getCategoriePersonneCible().getId();
                String value = r.getCategoriePersonneRepere().getId() + "_" + r.getPourcentage().stripTrailingZeros().toPlainString();
                remisesExistantes.put(key, value);
            }
        }
        model.addAttribute("remisesExistantes", remisesExistantes);
        
        return "backoffice/seance-form";
    }

    @PostMapping("/save")
    @Transactional
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
            model.addAttribute("categories", categoriePersonneService.findAll());
            model.addAttribute("tarifsExistants", Map.of());
            model.addAttribute("remisesExistantes", Map.of());
            return "backoffice/seance-form";
        }
        
        try {
            seanceService.save(seance);

            // Ne plus supprimer - on crée de nouvelles entrées avec date_creation
            // Les anciennes remises avec pourcentage >= 0 seront désactivées (pourcentage négatif) si on passe à un prix direct

            List<TypePlace> typePlaces = typePlaceRepository.findAll();
            Map<Long, TypePlace> typePlaceMap = new HashMap<>();
            for (TypePlace tp : typePlaces) {
                typePlaceMap.put(tp.getId(), tp);
            }

            var categories = categoriePersonneService.findAll();
            Map<Long, CategoriePersonne> catMap = new HashMap<>();
            for (var cat : categories) {
                catMap.put(cat.getId(), cat);
            }

            List<TarifSeance> tarifsToSave = new ArrayList<>();
            List<Remise> remisesToSave = new ArrayList<>();

            // Parse tarifs[tpId_catId] et remises[tpId_catId]
            // Format remise: "sourceId_pourcentage" (ex: "1_50" pour catégorie 1 à 50%)
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();
                
                // Traitement des tarifs
                if (key.startsWith("tarifs[") && key.endsWith("]")) {
                    String rawIds = key.substring("tarifs[".length(), key.length() - 1);
                    if (rawIds.isBlank() || !rawIds.contains("_"))
                        continue;
                    String[] parts = rawIds.split("_");
                    if (parts.length != 2)
                        continue;
                    Long tpId = Long.valueOf(parts[0]);
                    Long catId = Long.valueOf(parts[1]);
                    String value = entry.getValue();
                    
                    TypePlace tp = typePlaceMap.get(tpId);
                    CategoriePersonne cat = catMap.get(catId);
                    if (tp == null || cat == null)
                        continue;
                    
                    // Vérifier si une remise existe pour ce tarif
                    String remiseKey = "remises[" + tpId + "_" + catId + "]";
                    String remiseValue = allParams.get(remiseKey);
                    
                    if (remiseValue != null && !remiseValue.isBlank()) {
                        // Il y a une remise => prix NULL dans tarif_seance, nouvelle remise dans table remise
                        String[] remiseParts = remiseValue.split("_");
                        if (remiseParts.length == 2) {
                            Long sourceId = Long.valueOf(remiseParts[0]);
                            BigDecimal pourcentage = new BigDecimal(remiseParts[1]);
                            CategoriePersonne catSource = catMap.get(sourceId);
                            
                            if (catSource != null) {
                                // Créer nouveau tarif avec prix NULL (nouvelle date_creation)
                                tarifsToSave.add(TarifSeance.builder()
                                        .seance(seance)
                                        .typePlace(tp)
                                        .categoriePersonne(cat)
                                        .prix(null) // Prix NULL pour indiquer utilisation de remise
                                        .build());
                                
                                // Créer nouvelle remise (avec pourcentage positif)
                                remisesToSave.add(Remise.builder()
                                        .seance(seance)
                                        .typePlace(tp)
                                        .categoriePersonneCible(cat)
                                        .categoriePersonneRepere(catSource)
                                        .pourcentage(pourcentage)
                                        .build());
                            }
                        }
                    } else if (value != null && !value.isBlank()) {
                        // Pas de remise => prix direct
                        // Désactiver l'ancienne remise si elle existe (mettre pourcentage en négatif)
                        remiseService.deactivateRemise(seance.getId(), tpId, catId);
                        
                        BigDecimal prix = new BigDecimal(value);
                        tarifsToSave.add(TarifSeance.builder()
                                .seance(seance)
                                .typePlace(tp)
                                .categoriePersonne(cat)
                                .prix(prix)
                                .build());
                    }
                }
            }

            if (!tarifsToSave.isEmpty()) {
                tarifSeanceRepository.saveAll(tarifsToSave);
            }
            if (!remisesToSave.isEmpty()) {
                remiseRepository.saveAll(remisesToSave);
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
            model.addAttribute("categories", categoriePersonneService.findAll());
            
            // Récupérer les tarifs saisis pour les réafficher (format tpId_catId)
            Map<String, BigDecimal> tarifsExistants = new HashMap<>();
            Map<String, String> remisesExistantes = new HashMap<>();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("tarifs[") && key.endsWith("]")) {
                    String rawIds = key.substring("tarifs[".length(), key.length() - 1);
                    if (!rawIds.isBlank() && rawIds.contains("_") && entry.getValue() != null && !entry.getValue().isBlank()) {
                        try {
                            tarifsExistants.put(rawIds, new BigDecimal(entry.getValue()));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (key.startsWith("remises[") && key.endsWith("]")) {
                    String rawIds = key.substring("remises[".length(), key.length() - 1);
                    if (!rawIds.isBlank() && rawIds.contains("_") && entry.getValue() != null && !entry.getValue().isBlank()) {
                        remisesExistantes.put(rawIds, entry.getValue());
                    }
                }
            }
            model.addAttribute("tarifsExistants", tarifsExistants);
            model.addAttribute("remisesExistantes", remisesExistantes);
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
