package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publicite")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Publicite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_societe", nullable = false)
    private Societe societe;

    @Column(name = "cree_le")
    @Builder.Default
    private LocalDateTime creeLe = LocalDateTime.now();

    @OneToMany(mappedBy = "publicite", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PubliciteDetail> details = new ArrayList<>();
}
