package org.example.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.example.cinema.model.TarifSeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifSeanceRepository extends JpaRepository<TarifSeance, Long> {
    
    /**
     * Trouve le tarif le plus récent pour une combinaison donnée
     */
    @Query("SELECT ts FROM TarifSeance ts WHERE ts.seance.id = :seanceId " +
           "AND ts.typePlace.id = :typePlaceId " +
           "AND ts.categoriePersonne.id = :categoriePersonneId " +
           "ORDER BY ts.dateCreation DESC LIMIT 1")
    Optional<TarifSeance> findLatestBySeanceIdAndTypePlaceIdAndCategoriePersonneId(
            @Param("seanceId") Long seanceId, 
            @Param("typePlaceId") Long typePlaceId,
            @Param("categoriePersonneId") Long categoriePersonneId);

    /**
     * Trouve tous les tarifs les plus récents pour une séance
     */
    @Query("SELECT ts FROM TarifSeance ts WHERE ts.seance.id = :seanceId " +
           "AND ts.dateCreation = (SELECT MAX(ts2.dateCreation) FROM TarifSeance ts2 " +
           "WHERE ts2.seance.id = ts.seance.id " +
           "AND ts2.typePlace.id = ts.typePlace.id " +
           "AND ts2.categoriePersonne.id = ts.categoriePersonne.id)")
    List<TarifSeance> findLatestBySeanceId(@Param("seanceId") Long seanceId);

    List<TarifSeance> findBySeanceId(Long seanceId);
}
