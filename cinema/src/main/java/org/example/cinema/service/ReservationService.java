package org.example.cinema.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.example.cinema.model.CategoriePersonne;
import org.example.cinema.model.Client;
import org.example.cinema.model.HistoriqueStatutReservation;
import org.example.cinema.model.Place;
import org.example.cinema.model.Reservation;
import org.example.cinema.model.Seance;
import org.example.cinema.model.StatutReservation;
import org.example.cinema.model.StatutTicket;
import org.example.cinema.model.Ticket;
import org.example.cinema.repository.PlaceRepository;
import org.example.cinema.repository.ReservationRepository;
import org.example.cinema.repository.StatutReservationRepository;
import org.example.cinema.repository.StatutTicketRepository;
import org.example.cinema.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final StatutReservationRepository statutReservationRepository;
    private final StatutTicketRepository statutTicketRepository;
    private final PlaceRepository placeRepository;
    private final TarifService tarifService;
    private final org.example.cinema.repository.HistoriqueStatutReservationRepository historiqueRepository;

    public ReservationService(ReservationRepository reservationRepository,
            TicketRepository ticketRepository,
            StatutReservationRepository statutReservationRepository,
            StatutTicketRepository statutTicketRepository,
            PlaceRepository placeRepository,
            TarifService tarifService,
            org.example.cinema.repository.HistoriqueStatutReservationRepository historiqueRepository) {
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.statutReservationRepository = statutReservationRepository;
        this.statutTicketRepository = statutTicketRepository;
        this.placeRepository = placeRepository;
        this.tarifService = tarifService;
        this.historiqueRepository = historiqueRepository;
    }

    public Set<Long> getOccupiedPlaceIds(Long seanceId) {
        return ticketRepository.findOccupiedPlaceIdsBySeanceId(seanceId);
    }

    public List<Reservation> findByClientId(Long clientId) {
        return reservationRepository.findByClientId(clientId);
    }

    public List<Reservation> findByClientIdWithFilters(Long clientId, Long statutId, String filmTitre,
            java.time.LocalDate dateFrom, java.time.LocalDate dateTo) {
        // Récupérer toutes les réservations du client
        List<Reservation> reservations = reservationRepository.findByClientId(clientId);

        // Filtrer en Java
        return reservations.stream()
                .filter(r -> {
                    // Filtre par statut
                    if (statutId != null && (r.getStatut() == null || !statutId.equals(r.getStatut().getId()))) {
                        return false;
                    }
                    // Filtre par titre de film
                    if (filmTitre != null && !filmTitre.isEmpty()) {
                        if (r.getSeance() == null || r.getSeance().getFilm() == null ||
                                !r.getSeance().getFilm().getTitre().toLowerCase().contains(filmTitre.toLowerCase())) {
                            return false;
                        }
                    }
                    // Filtre par date de début
                    if (dateFrom != null && r.getSeance() != null && r.getSeance().getDebut() != null) {
                        java.time.LocalDate seanceDate = r.getSeance().getDebut().toLocalDate();
                        if (seanceDate.isBefore(dateFrom)) {
                            return false;
                        }
                    }
                    // Filtre par date de fin
                    if (dateTo != null && r.getSeance() != null && r.getSeance().getDebut() != null) {
                        java.time.LocalDate seanceDate = r.getSeance().getDebut().toLocalDate();
                        if (seanceDate.isAfter(dateTo)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public java.math.BigDecimal getChiffreAffairesBySeanceId(Long seanceId) {
        // Ne compte que les réservations avec statut PAYEE
        List<Reservation> reservations = reservationRepository.findPayeesBySeanceId(seanceId);
        return reservations.stream()
                .map(Reservation::getMontantTotal)
                .filter(m -> m != null)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    public java.util.Map<Long, java.math.BigDecimal> getChiffreAffairesBySeances(List<Long> seanceIds) {
        java.util.Map<Long, java.math.BigDecimal> result = new java.util.HashMap<>();
        for (Long seanceId : seanceIds) {
            result.put(seanceId, getChiffreAffairesBySeanceId(seanceId));
        }
        return result;
    }

    @Transactional
    public Reservation createReservation(Client client, Seance seance, Map<Long, Long> placesWithCategories,
            Map<Long, CategoriePersonne> categoriesMap) {

        StatutReservation statutCreee = statutReservationRepository.findByCode("CREEE")
                .orElseThrow(() -> new RuntimeException("Statut CREEE non trouvé"));
        StatutTicket statutReserve = statutTicketRepository.findByCode("RESERVE")
                .orElseThrow(() -> new RuntimeException("Statut RESERVE non trouvé"));

        Reservation reservation = Reservation.builder()
                .client(client)
                .seance(seance)
                .statut(statutCreee)
                .montantTotal(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Long> entry : placesWithCategories.entrySet()) {
            Long placeId = entry.getKey();
            Long categorieId = entry.getValue();

            Place place = placeRepository.findById(placeId)
                    .orElseThrow(() -> new RuntimeException("Place non trouvée: " + placeId));
            CategoriePersonne categorie = categoriesMap.get(categorieId);

            BigDecimal prix = tarifService.findTarifForSeanceOrDefault(
                    seance.getId(),
                    place.getTypePlace().getId(),
                    categorieId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Aucun tarif disponible pour cette séance/type de place/catégorie"));

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
        Reservation saved = reservationRepository.save(reservation);

        HistoriqueStatutReservation h = HistoriqueStatutReservation.builder()
                .reservation(saved)
                .statut(statutCreee)
                .changePar(client)
                .commentaire("Création de la réservation")
                .build();
        historiqueRepository.save(h);

        return saved;
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
        Reservation saved = reservationRepository.save(res);

        HistoriqueStatutReservation h = HistoriqueStatutReservation.builder()
                .reservation(saved)
                .statut(statut)
                .commentaire("Changement de statut via backoffice")
                .build();
        historiqueRepository.save(h);

        return saved;
    }

    public java.util.List<HistoriqueStatutReservation> getHistoryForReservation(Long reservationId) {
        return historiqueRepository.findByReservationIdOrderByDateChangementDesc(reservationId);
    }
}
