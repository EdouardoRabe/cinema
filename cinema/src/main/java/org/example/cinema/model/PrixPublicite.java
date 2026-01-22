package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "prix_publicite")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrixPublicite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @Column(name = "date_creation")
    @Builder.Default
    private OffsetDateTime dateCreation = OffsetDateTime.now();
}
