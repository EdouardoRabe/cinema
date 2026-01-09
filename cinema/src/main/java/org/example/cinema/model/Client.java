package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "personne")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_complet")
    private String nomComplet;

    @Column(unique = true)
    private String email;

    private String telephone;

    // Champ pour mot de passe (à ajouter dans la table)
    private String motDePasse;

    @Column(name = "cree_le")
    private OffsetDateTime creeLe;
}
