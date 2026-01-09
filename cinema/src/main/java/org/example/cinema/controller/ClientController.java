package org.example.cinema.controller;

import jakarta.servlet.http.HttpSession;
import org.example.cinema.model.Client;
import org.example.cinema.model.Reservation;
import org.example.cinema.service.ClientService;
import org.example.cinema.service.ReservationService;
import org.example.cinema.service.StatutReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ClientController {

    private final ClientService clientService;
    private final ReservationService reservationService;
    private final StatutReservationService statutReservationService;

    public ClientController(ClientService clientService, ReservationService reservationService, 
                           StatutReservationService statutReservationService) {
        this.clientService = clientService;
        this.reservationService = reservationService;
        this.statutReservationService = statutReservationService;
    }

    @GetMapping("/login")
    public String showLogin(@RequestParam(name = "message", required = false) String message, 
                           @RequestParam(name = "error", required = false) String error,
                           @RequestParam(name = "redirect", required = false) String redirect,
                           HttpSession session,
                           Model model) {
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        if (redirect != null && !redirect.isEmpty()) {
            session.setAttribute("redirectAfterLogin", redirect);
        }
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("motDePasse") String motDePasse,
                        @RequestParam(name = "redirect", required = false) String redirect,
                        HttpSession session,
                        Model model) {
        if (clientService.authenticate(email, motDePasse)) {
            Client client = clientService.findByEmail(email).orElse(null);
            session.setAttribute("client", client);
            
            // Rediriger vers la page demandée avant connexion
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                session.removeAttribute("redirectAfterLogin");
                return "redirect:" + redirectUrl;
            }
            if (redirect != null && !redirect.isEmpty()) {
                return "redirect:" + redirect;
            }
            return "redirect:/";
        } else {
            model.addAttribute("error", "Email ou mot de passe incorrect");
            model.addAttribute("email", email);
            model.addAttribute("redirect", redirect);
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegister(@RequestParam(name = "redirect", required = false) String redirect,
                              HttpSession session,
                              Model model) {
        if (redirect != null && !redirect.isEmpty()) {
            session.setAttribute("redirectAfterLogin", redirect);
        }
        model.addAttribute("redirect", redirect);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("nomComplet") String nomComplet,
                          @RequestParam("email") String email,
                          @RequestParam(name = "telephone", required = false) String telephone,
                          @RequestParam("motDePasse") String motDePasse,
                          @RequestParam("confirmMotDePasse") String confirmMotDePasse,
                          @RequestParam(name = "redirect", required = false) String redirect,
                          HttpSession session,
                          Model model) {
        // Validation
        if (!motDePasse.equals(confirmMotDePasse)) {
            model.addAttribute("error", "Les mots de passe ne correspondent pas");
            model.addAttribute("nomComplet", nomComplet);
            model.addAttribute("email", email);
            model.addAttribute("telephone", telephone);
            model.addAttribute("redirect", redirect);
            return "register";
        }

        try {
            Client client = clientService.register(nomComplet, email, telephone, motDePasse);
            session.setAttribute("client", client);
            
            // Rediriger vers la page demandée avant inscription
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                session.removeAttribute("redirectAfterLogin");
                return "redirect:" + redirectUrl;
            }
            if (redirect != null && !redirect.isEmpty()) {
                return "redirect:" + redirect;
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nomComplet", nomComplet);
            model.addAttribute("email", email);
            model.addAttribute("telephone", telephone);
            model.addAttribute("redirect", redirect);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/mon-compte")
    public String monCompte(HttpSession session, Model model) {
        Client client = (Client) session.getAttribute("client");
        if (client == null) {
            return "redirect:/login";
        }
        model.addAttribute("client", client);
        return "mon-compte";
    }

    @GetMapping("/mes-reservations")
    public String mesReservations(
            @RequestParam(name = "statutId", required = false) Long statutId,
            @RequestParam(name = "film", required = false) String film,
            @RequestParam(name = "dateFrom", required = false) String dateFromStr,
            @RequestParam(name = "dateTo", required = false) String dateToStr,
            HttpSession session, Model model) {
        Client client = (Client) session.getAttribute("client");
        if (client == null) {
            return "redirect:/login?redirect=/mes-reservations";
        }
        
        // Parser les dates
        java.time.LocalDate dateFrom = null;
        java.time.LocalDate dateTo = null;
        if (dateFromStr != null && !dateFromStr.isEmpty()) {
            dateFrom = java.time.LocalDate.parse(dateFromStr);
        }
        if (dateToStr != null && !dateToStr.isEmpty()) {
            dateTo = java.time.LocalDate.parse(dateToStr);
        }
        
        // Récupérer les réservations filtrées
        List<Reservation> reservations;
        boolean hasFilters = statutId != null || (film != null && !film.isEmpty()) || dateFrom != null || dateTo != null;
        
        if (hasFilters) {
            reservations = reservationService.findByClientIdWithFilters(client.getId(), statutId, film, dateFrom, dateTo);
        } else {
            reservations = reservationService.findByClientId(client.getId());
        }
        
        // Compter toutes les réservations (sans filtre) pour les stats
        List<Reservation> allReservations = reservationService.findByClientId(client.getId());
        long confirmees = allReservations.stream()
            .filter(r -> r.getStatut() != null && "CONFIRMED".equals(r.getStatut().getCode()))
            .count();
        long enAttente = allReservations.stream()
            .filter(r -> r.getStatut() != null && "PENDING".equals(r.getStatut().getCode()))
            .count();
        
        // Récupérer les statuts pour le filtre
        model.addAttribute("statuts", statutReservationService.findAll());
        model.addAttribute("client", client);
        model.addAttribute("reservations", reservations);
        model.addAttribute("reservationsConfirmees", confirmees);
        model.addAttribute("reservationsEnAttente", enAttente);
        model.addAttribute("totalReservations", allReservations.size());
        
        // Conserver les valeurs des filtres
        model.addAttribute("selectedStatutId", statutId);
        model.addAttribute("selectedFilm", film);
        model.addAttribute("selectedDateFrom", dateFromStr);
        model.addAttribute("selectedDateTo", dateToStr);
        
        return "mes-reservations";
    }
}
