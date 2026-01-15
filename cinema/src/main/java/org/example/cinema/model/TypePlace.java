package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_place")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TypePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String libelle;
    
    @Column(name = "couleur")
    private String couleur; // Couleur CSS (ex: #FFD700, gold, etc.)
}
