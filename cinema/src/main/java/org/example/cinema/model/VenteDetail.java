package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vente_detail")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VenteDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vente", nullable = false)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantite = 1;

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    public BigDecimal getMontantTotal() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}
