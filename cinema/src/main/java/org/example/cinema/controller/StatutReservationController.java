package org.example.cinema.controller;

import org.example.cinema.service.StatutReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatutReservationController {

    private final StatutReservationService service;

    public StatutReservationController(StatutReservationService service) {
        this.service = service;
    }

    @GetMapping("/statuts")
    public String listStatuts(Model model) {
        model.addAttribute("statuts", service.findAll());
        return "statuts";
    }
}
