package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String libelle;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrixProduit> prixHistorique = new ArrayList<>();
}
