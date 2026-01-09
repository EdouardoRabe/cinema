package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(
        name = "film_genre",
        joinColumns = @JoinColumn(name = "id_film"),
        inverseJoinColumns = @JoinColumn(name = "id_genre")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

}
