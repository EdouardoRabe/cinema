package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "statut_ticket")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatutTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "est_final")
    private Boolean estFinal;
}
