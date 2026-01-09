package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "seance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_film")
    private Film film;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_salle")
    private Salle salle;

    @Column(nullable = false)
    private OffsetDateTime debut;

    private OffsetDateTime fin;

    private String langue;
}
