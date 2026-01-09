package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "historique_statut_reservation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistoriqueStatutReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reservation")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_statut")
    private StatutReservation statut;

    @Column(name = "date_changement")
    private OffsetDateTime dateChangement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_par")
    private Client changePar;

    private String commentaire;

    @PrePersist
    protected void onCreate() {
        if (dateChangement == null) dateChangement = OffsetDateTime.now();
    }
}