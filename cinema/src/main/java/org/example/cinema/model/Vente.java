package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_vente", nullable = false)
    private LocalDate dateVente;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VenteDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaiementVente> paiements = new ArrayList<>();
}
