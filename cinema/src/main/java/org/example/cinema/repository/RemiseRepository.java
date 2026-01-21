package org.example.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.example.cinema.model.Remise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RemiseRepository extends JpaRepository<Remise, Long> {
    
    /**
     * Trouve la remise la plus récente et active (pourcentage >= 0) pour une combinaison donnée
     */
    @Query("SELECT r FROM Remise r WHERE r.seance.id = :seanceId " +
           "AND r.typePlace.id = :typePlaceId " +
           "AND r.categoriePersonneCible.id = :categoriePersonneCibleId " +
           "AND r.pourcentage >= 0 " +
           "ORDER BY r.dateCreation DESC LIMIT 1")
    Optional<Remise> findLatestActiveBySeanceIdAndTypePlaceIdAndCategoriePersonneCibleId(
            @Param("seanceId") Long seanceId, 
            @Param("typePlaceId") Long typePlaceId, 
            @Param("categoriePersonneCibleId") Long categoriePersonneCibleId);
 
    /**
     * Trouve toutes les remises actives (pourcentage >= 0) les plus récentes pour une séance
     */
    @Query("SELECT r FROM Remise r WHERE r.seance.id = :seanceId " +
           "AND r.pourcentage >= 0 " +
           "AND r.dateCreation = (SELECT MAX(r2.dateCreation) FROM Remise r2 " +
           "WHERE r2.seance.id = r.seance.id " +
           "AND r2.typePlace.id = r.typePlace.id " +
           "AND r2.categoriePersonneCible.id = r.categoriePersonneCible.id)")
    List<Remise> findLatestActiveBySeanceId(@Param("seanceId") Long seanceId);
   
    /**
     * Désactive toutes les remises d'une séance en mettant le pourcentage en négatif
     */
    @Modifying
    @Query("UPDATE Remise r SET r.pourcentage = -ABS(r.pourcentage) WHERE r.seance.id = :seanceId AND r.pourcentage >= 0")
    void deactivateBySeanceId(@Param("seanceId") Long seanceId);
    
    /**
     * Désactive une remise spécifique en mettant le pourcentage en négatif
     */
    @Modifying
    @Query("UPDATE Remise r SET r.pourcentage = -ABS(r.pourcentage) " +
           "WHERE r.seance.id = :seanceId AND r.typePlace.id = :typePlaceId " +
           "AND r.categoriePersonneCible.id = :categorieId AND r.pourcentage >= 0")
    void deactivateBySeanceIdAndTypePlaceIdAndCategorieId(
            @Param("seanceId") Long seanceId, 
            @Param("typePlaceId") Long typePlaceId, 
            @Param("categorieId") Long categorieId);
    
    List<Remise> findBySeanceIdAndTypePlaceId(Long seanceId, Long typePlaceId);
}
