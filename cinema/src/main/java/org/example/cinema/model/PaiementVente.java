package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "paiement_vente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaiementVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vente", nullable = false)
    private Vente vente;

    @Column(name = "montant_paye", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantPaye;

    @Column(name = "date_paiement", nullable = false)
    private LocalDate datePaiement;
}
