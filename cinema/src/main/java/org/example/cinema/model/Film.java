package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "film")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(columnDefinition = "text")
    private String description;

    private Integer dureeMinutes;

    private LocalDate dateSortie;

    private Integer ageMin;

    private String langueOriginale;

}
