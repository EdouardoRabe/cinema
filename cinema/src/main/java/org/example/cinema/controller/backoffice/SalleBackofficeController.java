package org.example.cinema.controller.backoffice;

import org.example.cinema.model.Place;
import org.example.cinema.model.Salle;
import org.example.cinema.model.TypePlace;
import org.example.cinema.service.PlaceService;
import org.example.cinema.service.SalleService;
import org.example.cinema.service.TypePlaceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/backoffice/salles")
public class SalleBackofficeController {

    private final SalleService salleService;
    private final PlaceService placeService;
    private final TypePlaceService typePlaceService;

    public SalleBackofficeController(SalleService salleService, PlaceService placeService, 
                                      TypePlaceService typePlaceService) {
        this.salleService = salleService;
        this.placeService = placeService;
        this.typePlaceService = typePlaceService;
    }

    @GetMapping
    public String list(Model model) {
        List<Salle> salles = salleService.findAll();
        
        // Compter les places par salle
        Map<Long, Long> placesCount = new HashMap<>();
        for (Salle salle : salles) {
            long count = placeService.findBySalleId(salle.getId()).size();
            placesCount.put(salle.getId(), count);
        }
        
        model.addAttribute("salles", salles);
        model.addAttribute("placesCount", placesCount);
        return "backoffice/salles";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("salle", new Salle());
        return "backoffice/salle-form";
    }

    @PostMapping("/create")
    public String createSubmit(@RequestParam(name = "nom") String nom,
                               @RequestParam(name = "capacite") int capacite,
                               @RequestParam(name = "colonnesParRangee", defaultValue = "10") int colonnesParRangee,
                               RedirectAttributes redirectAttributes) {
        try {
            if (nom == null || nom.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Le nom de la salle est obligatoire");
                return "redirect:/backoffice/salles/create";
            }
            if (capacite <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "La capacité doit être supérieure à 0");
                return "redirect:/backoffice/salles/create";
            }
            if (colonnesParRangee <= 0) {
                colonnesParRangee = 10;
            }
            
            Salle salle = salleService.createWithPlaces(nom, capacite, colonnesParRangee);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Salle \"" + salle.getNom() + "\" créée avec " + capacite + " places STANDARD");
            return "redirect:/backoffice/salles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création: " + e.getMessage());
            return "redirect:/backoffice/salles/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        var salleOpt = salleService.findById(id);
        if (salleOpt.isEmpty()) {
            return "redirect:/backoffice/salles";
        }
        
        Salle salle = salleOpt.get();
        List<Place> places = placeService.findBySalleId(id);
        Map<String, List<Place>> placesByRow = placeService.groupByRow(places);
        List<String> rows = placesByRow.keySet().stream().sorted().toList();
        
        List<TypePlace> typePlacesRaw = typePlaceService.findAll();
        // Deduplicate by libelle (preserve order)
        java.util.Map<String, TypePlace> unique = new java.util.LinkedHashMap<>();
        for (TypePlace tp : typePlacesRaw) {
            if (!unique.containsKey(tp.getLibelle())) {
                // ensure couleur not null
                if (tp.getCouleur() == null) tp.setCouleur("#6c757d");
                unique.put(tp.getLibelle(), tp);
            }
        }
        List<TypePlace> typePlaces = new java.util.ArrayList<>(unique.values());

        // Créer une Map pour les types de places (sera converti en JSON par Thymeleaf)
        Map<Long, Map<String, String>> typePlacesMap = new HashMap<>();
        for (TypePlace tp : typePlaces) {
            Map<String, String> data = new HashMap<>();
            data.put("libelle", tp.getLibelle());
            data.put("couleur", tp.getCouleur());
            typePlacesMap.put(tp.getId(), data);
        }
        
        model.addAttribute("salle", salle);
        model.addAttribute("places", places);
        model.addAttribute("placesByRow", placesByRow);
        model.addAttribute("rows", rows);
        model.addAttribute("typePlaces", typePlaces);
        model.addAttribute("typePlacesMap", typePlacesMap);
        
        return "backoffice/salle-edit";
    }

    @PostMapping("/edit/{id}")
    public String editSubmit(@PathVariable("id") Long id,
                             @RequestParam(name = "nom") String nom,
                             @RequestParam(name = "capacite") int capacite,
                             RedirectAttributes redirectAttributes) {
        try {
            var salleOpt = salleService.findById(id);
            if (salleOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Salle non trouvée");
                return "redirect:/backoffice/salles";
            }
            
            Salle salle = salleOpt.get();
            salle.setNom(nom);
            salle.setCapacite(capacite);
            salleService.save(salle);
            
            redirectAttributes.addFlashAttribute("successMessage", "Salle mise à jour avec succès");
            return "redirect:/backoffice/salles/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/backoffice/salles/edit/" + id;
        }
    }

    @PostMapping("/edit/{id}/places")
    @ResponseBody
    public Map<String, Object> updatePlaceType(@PathVariable("id") Long salleId,
                                               @RequestParam(name = "placeId") Long placeId,
                                               @RequestParam(name = "typePlaceId") Long typePlaceId) {
        Map<String, Object> response = new HashMap<>();
        try {
            var placeOpt = placeService.findById(placeId);
            var typePlaceOpt = typePlaceService.findById(typePlaceId);
            
            if (placeOpt.isEmpty() || typePlaceOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Place ou type de place non trouvé");
                return response;
            }
            
            Place place = placeOpt.get();
            if (!place.getSalle().getId().equals(salleId)) {
                response.put("success", false);
                response.put("message", "Cette place n'appartient pas à cette salle");
                return response;
            }
            
            place.setTypePlace(typePlaceOpt.get());
            placeService.save(place);
            
            response.put("success", true);
            response.put("message", "Type de place mis à jour");
            response.put("typePlace", Map.of(
                    "id", typePlaceOpt.get().getId(),
                    "libelle", typePlaceOpt.get().getLibelle(),
                    "couleur", typePlaceOpt.get().getCouleur() != null ? typePlaceOpt.get().getCouleur() : "#6c757d"
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            var salleOpt = salleService.findById(id);
            if (salleOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Salle non trouvée");
                return "redirect:/backoffice/salles";
            }
            
            String nom = salleOpt.get().getNom();
            salleService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Salle \"" + nom + "\" supprimée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/backoffice/salles";
    }
}
