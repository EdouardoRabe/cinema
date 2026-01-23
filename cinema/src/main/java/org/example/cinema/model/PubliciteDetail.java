package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "publicite_detail", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_publicite", "id_seance"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PubliciteDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_publicite", nullable = false)
    private Publicite publicite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seance", nullable = false)
    private Seance seance;

    @Column(name = "nb_fois", nullable = false)
    @Builder.Default
    private Integer nbFois = 1;
}
