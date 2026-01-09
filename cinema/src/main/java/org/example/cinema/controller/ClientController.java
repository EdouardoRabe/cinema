package org.example.cinema.controller;

import jakarta.servlet.http.HttpSession;
import org.example.cinema.model.Client;
import org.example.cinema.service.ClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/login")
    public String showLogin(@RequestParam(required = false) String message, 
                           @RequestParam(required = false) String error,
                           @RequestParam(required = false) String redirect,
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
    public String login(@RequestParam String email,
                        @RequestParam String motDePasse,
                        @RequestParam(required = false) String redirect,
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
    public String showRegister(@RequestParam(required = false) String redirect,
                              HttpSession session,
                              Model model) {
        if (redirect != null && !redirect.isEmpty()) {
            session.setAttribute("redirectAfterLogin", redirect);
        }
        model.addAttribute("redirect", redirect);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String nomComplet,
                          @RequestParam String email,
                          @RequestParam(required = false) String telephone,
                          @RequestParam String motDePasse,
                          @RequestParam String confirmMotDePasse,
                          @RequestParam(required = false) String redirect,
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
}
