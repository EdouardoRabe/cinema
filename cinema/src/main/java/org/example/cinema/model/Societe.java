package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "societe")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Societe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String libelle;

    @Column(name = "cree_le")
    @Builder.Default
    private OffsetDateTime creeLe = OffsetDateTime.now();
}
