package org.example.cinema.service;

import org.example.cinema.model.*;
import org.example.cinema.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final StatutReservationRepository statutReservationRepository;
    private final StatutTicketRepository statutTicketRepository;
    private final PlaceRepository placeRepository;
    private final TarifService tarifService;

    public ReservationService(ReservationRepository reservationRepository,
                               TicketRepository ticketRepository,
                               StatutReservationRepository statutReservationRepository,
                               StatutTicketRepository statutTicketRepository,
                               PlaceRepository placeRepository,
                               TarifService tarifService) {
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.statutReservationRepository = statutReservationRepository;
        this.statutTicketRepository = statutTicketRepository;
        this.placeRepository = placeRepository;
        this.tarifService = tarifService;
    }

    public Set<Long> getOccupiedPlaceIds(Long seanceId) {
        return ticketRepository.findOccupiedPlaceIdsBySeanceId(seanceId);
    }

    public List<Reservation> findByClientId(Long clientId) {
        return reservationRepository.findByClientId(clientId);
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    /**
     * Crée une réservation avec les places sélectionnées.
     * @param client Le client qui fait la réservation
     * @param seance La séance réservée
     * @param placesWithCategories Map de placeId -> categoriePersonneId
     * @return La réservation créée
     */
    @Transactional
    public Reservation createReservation(Client client, Seance seance, Map<Long, Long> placesWithCategories,
                                          Map<Long, CategoriePersonne> categoriesMap) {
        // Récupérer les statuts
        StatutReservation statutCreee = statutReservationRepository.findByCode("CREEE")
                .orElseThrow(() -> new RuntimeException("Statut CREEE non trouvé"));
        StatutTicket statutReserve = statutTicketRepository.findByCode("RESERVE")
                .orElseThrow(() -> new RuntimeException("Statut RESERVE non trouvé"));

        // Créer la réservation
        Reservation reservation = Reservation.builder()
                .client(client)
                .seance(seance)
                .statut(statutCreee)
                .montantTotal(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // Créer les tickets
        for (Map.Entry<Long, Long> entry : placesWithCategories.entrySet()) {
            Long placeId = entry.getKey();
            Long categorieId = entry.getValue();

            Place place = placeRepository.findById(placeId)
                    .orElseThrow(() -> new RuntimeException("Place non trouvée: " + placeId));
            CategoriePersonne categorie = categoriesMap.get(categorieId);

            // Calculer le prix
            BigDecimal prix = tarifService.findByTypePlaceAndCategorie(
                    place.getTypePlace().getId(), categorieId)
                    .map(t -> t.getPrix())
                    .orElse(tarifService.getTarifDefautAdulte());

            Ticket ticket = Ticket.builder()
                    .reservation(reservation)
                    .seance(seance)
                    .place(place)
                    .statut(statutReserve)
                    .categoriePersonne(categorie)
                    .prix(prix)
                    .build();

            reservation.getTickets().add(ticket);
            total = total.add(prix);
        }

        reservation.setMontantTotal(total);
        return reservationRepository.save(reservation);
    }

    public List<Ticket> getTicketsByReservation(Long reservationId) {
        return ticketRepository.findByReservationId(reservationId);
    }

    @Transactional
    public Reservation updateStatus(Long reservationId, Long statutId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        StatutReservation statut = statutReservationRepository.findById(statutId)
                .orElseThrow(() -> new RuntimeException("Statut non trouvé"));
        res.setStatut(statut);
        return reservationRepository.save(res);
    }
}
