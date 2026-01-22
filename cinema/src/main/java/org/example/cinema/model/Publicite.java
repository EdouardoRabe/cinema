package org.example.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

@Entity
@Table(name = "publicite", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_societe", "date_diffusion"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Publicite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_societe", nullable = false)
    private Societe societe;

    @Column(name = "nb_fois", nullable = false)
    @Builder.Default
    private Integer nbFois = 1;

    @Column(name = "date_diffusion", nullable = false)
    private LocalDate dateDiffusion;

    @Column(name = "cree_le")
    @Builder.Default
    private OffsetDateTime creeLe = OffsetDateTime.now();

    /**
     * Retourne l'année de la date de diffusion
     */
    public int getAnnee() {
        return dateDiffusion != null ? dateDiffusion.getYear() : 0;
    }

    /**
     * Retourne le mois de la date de diffusion (1-12)
     */
    public int getMois() {
        return dateDiffusion != null ? dateDiffusion.getMonthValue() : 0;
    }

    /**
     * Retourne le mois sous forme de texte (ex: "Janvier 2026")
     */
    public String getMoisAnneeFormate() {
        if (dateDiffusion == null) return "";
        String nomMois = dateDiffusion.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        return nomMois.substring(0, 1).toUpperCase() + nomMois.substring(1) + " " + dateDiffusion.getYear();
    }
}
