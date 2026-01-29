package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "prix_produit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrixProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @Column(name = "date_prix", nullable = false)
    private LocalDate datePrix;
}
