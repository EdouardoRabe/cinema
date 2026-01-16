package org.example.cinema.service;

import org.example.cinema.model.Paiement;
import org.example.cinema.model.Reservation;
import org.example.cinema.model.StatutReservation;
import org.example.cinema.repository.PaiementRepository;
import org.example.cinema.repository.ReservationRepository;
import org.example.cinema.repository.StatutReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;
    private final StatutReservationRepository statutReservationRepository;

    public PaiementService(PaiementRepository paiementRepository,
                           ReservationRepository reservationRepository,
                           StatutReservationRepository statutReservationRepository) {
        this.paiementRepository = paiementRepository;
        this.reservationRepository = reservationRepository;
        this.statutReservationRepository = statutReservationRepository;
    }

    public List<Paiement> findByReservationId(Long reservationId) {
        return paiementRepository.findByReservationId(reservationId);
    }

    public BigDecimal getTotalPaieByReservationId(Long reservationId) {
        return paiementRepository.getTotalPaieByReservationId(reservationId);
    }

    @Transactional
    public Paiement payerReservation(Long reservationId, BigDecimal montant) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée: " + reservationId));

        // Créer le paiement
        Paiement paiement = Paiement.builder()
                .reservation(reservation)
                .montantPaye(montant)
                .build();
        paiement = paiementRepository.save(paiement);

        // Mettre à jour le statut de la réservation à "PAYEE"
        StatutReservation statutPayee = statutReservationRepository.findByCode("PAYEE")
                .orElseThrow(() -> new RuntimeException("Statut PAYEE non trouvé"));
        reservation.setStatut(statutPayee);
        reservationRepository.save(reservation);

        return paiement;
    }

    public boolean isReservationPayee(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(r -> r.getStatut() != null && "PAYEE".equals(r.getStatut().getCode()))
                .orElse(false);
    }

    public Optional<Paiement> findById(Long id) {
        return paiementRepository.findById(id);
    }
}
