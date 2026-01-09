package org.example.cinema.controller;

import jakarta.servlet.http.HttpSession;
import org.example.cinema.model.Client;
import org.example.cinema.model.Reservation;
import org.example.cinema.service.ClientService;
import org.example.cinema.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ClientController {

    private final ClientService clientService;
    private final ReservationService reservationService;

    public ClientController(ClientService clientService, ReservationService reservationService) {
        this.clientService = clientService;
        this.reservationService = reservationService;
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
    public String mesReservations(HttpSession session, Model model) {
        Client client = (Client) session.getAttribute("client");
        if (client == null) {
            return "redirect:/login?redirect=/mes-reservations";
        }
        
        List<Reservation> reservations = reservationService.findByClientId(client.getId());
        
        // Compter les réservations par statut
        long confirmees = reservations.stream()
            .filter(r -> r.getStatut() != null && "CONFIRMED".equals(r.getStatut().getCode()))
            .count();
        long enAttente = reservations.stream()
            .filter(r -> r.getStatut() != null && "PENDING".equals(r.getStatut().getCode()))
            .count();
        
        model.addAttribute("client", client);
        model.addAttribute("reservations", reservations);
        model.addAttribute("reservationsConfirmees", confirmees);
        model.addAttribute("reservationsEnAttente", enAttente);
        return "mes-reservations";
    }
}
