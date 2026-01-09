package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "statut_reservation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatutReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Builder.Default
    private Boolean estFinal = false;
}
