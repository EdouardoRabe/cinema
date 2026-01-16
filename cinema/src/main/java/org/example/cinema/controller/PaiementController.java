package org.example.cinema.controller;

import org.example.cinema.model.Client;
import org.example.cinema.model.Paiement;
import org.example.cinema.model.Reservation;
import org.example.cinema.service.PaiementService;
import org.example.cinema.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/paiement")
public class PaiementController {

    private final PaiementService paiementService;
    private final ReservationService reservationService;

    public PaiementController(PaiementService paiementService, ReservationService reservationService) {
        this.paiementService = paiementService;
        this.reservationService = reservationService;
    }

    @GetMapping("/{reservationId}")
    public String showPaiementPage(@PathVariable("reservationId") Long reservationId,
                                   Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Client client = (Client) session.getAttribute("client");
        if (client == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Veuillez vous connecter pour effectuer un paiement");
            return "redirect:/login";
        }

        var reservationOpt = reservationService.findById(reservationId);
        if (reservationOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Réservation non trouvée");
            return "redirect:/mes-reservations";
        }

        Reservation reservation = reservationOpt.get();

        // Vérifier que la réservation appartient au client connecté
        if (reservation.getClient() == null || !reservation.getClient().getId().equals(client.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cette réservation ne vous appartient pas");
            return "redirect:/mes-reservations";
        }

        // Vérifier si déjà payée
        if (paiementService.isReservationPayee(reservationId)) {
            redirectAttributes.addFlashAttribute("infoMessage", "Cette réservation est déjà payée");
            return "redirect:/mes-reservations";
        }

        model.addAttribute("reservation", reservation);
        return "paiement";
    }

    @PostMapping("/{reservationId}/payer")
    public String effectuerPaiement(@PathVariable("reservationId") Long reservationId,
                                    HttpSession session, RedirectAttributes redirectAttributes) {
        Client client = (Client) session.getAttribute("client");
        if (client == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Veuillez vous connecter pour effectuer un paiement");
            return "redirect:/login";
        }

        var reservationOpt = reservationService.findById(reservationId);
        if (reservationOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Réservation non trouvée");
            return "redirect:/mes-reservations";
        }

        Reservation reservation = reservationOpt.get();

        // Vérifier que la réservation appartient au client connecté
        if (reservation.getClient() == null || !reservation.getClient().getId().equals(client.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cette réservation ne vous appartient pas");
            return "redirect:/mes-reservations";
        }

        // Vérifier si déjà payée
        if (paiementService.isReservationPayee(reservationId)) {
            redirectAttributes.addFlashAttribute("infoMessage", "Cette réservation est déjà payée");
            return "redirect:/mes-reservations";
        }

        try {
            // Effectuer le paiement (montant total de la réservation)
            Paiement paiement = paiementService.payerReservation(reservationId, reservation.getMontantTotal());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Paiement effectué avec succès ! Montant: " + reservation.getMontantTotal() + " Ar");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors du paiement: " + e.getMessage());
        }

        return "redirect:/mes-reservations";
    }
}
