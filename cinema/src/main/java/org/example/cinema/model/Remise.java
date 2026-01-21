package org.example.cinema.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "remise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Remise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seance")
    private Seance seance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_place")
    private TypePlace typePlace;

    /**
     * La catégorie de personne qui reçoit le prix calculé (cible du pourcentage)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie_personne_cible")
    private CategoriePersonne categoriePersonneCible;

    /**
     * La catégorie de personne dont le prix sert de référence (multiplié par le pourcentage)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie_personne_repere")
    private CategoriePersonne categoriePersonneRepere;

    /**
     * Le pourcentage à appliquer au prix de référence (ex: 50 pour 50%)
     * Si négatif, la remise est désactivée (historique)
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal pourcentage;
    
    /**
     * Date de création pour garder l'historique et prendre le plus récent
     */
    @Column(name = "date_creation")
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();
}
