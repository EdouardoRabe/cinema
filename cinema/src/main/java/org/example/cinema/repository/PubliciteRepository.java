package org.example.cinema.repository;

import org.example.cinema.model.Publicite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PubliciteRepository extends JpaRepository<Publicite, Long> {
    
    /**
     * Trouve une publicité par société et date de diffusion (même mois/année)
     */
    @Query("SELECT p FROM Publicite p WHERE p.societe.id = :societeId " +
           "AND YEAR(p.dateDiffusion) = :annee AND MONTH(p.dateDiffusion) = :mois")
    Optional<Publicite> findBySocieteIdAndYearMonth(@Param("societeId") Long societeId, 
                                                     @Param("annee") Integer annee, 
                                                     @Param("mois") Integer mois);
    
    /**
     * Trouve toutes les publicités avec leur société (EAGER fetch)
     */
    @Query("SELECT p FROM Publicite p JOIN FETCH p.societe ORDER BY p.dateDiffusion DESC, p.societe.libelle")
    List<Publicite> findAllWithSociete();
    
    /**
     * Trouve toutes les publicités pour un mois/année donné
     */
    @Query("SELECT p FROM Publicite p JOIN FETCH p.societe " +
           "WHERE YEAR(p.dateDiffusion) = :annee AND MONTH(p.dateDiffusion) = :mois " +
           "ORDER BY p.societe.libelle")
    List<Publicite> findByYearMonth(@Param("annee") Integer annee, @Param("mois") Integer mois);
    
    /**
     * Trouve toutes les publicités pour une année donnée
     */
    @Query("SELECT p FROM Publicite p JOIN FETCH p.societe WHERE YEAR(p.dateDiffusion) = :annee " +
           "ORDER BY MONTH(p.dateDiffusion), p.societe.libelle")
    List<Publicite> findByYear(@Param("annee") Integer annee);
    
    /**
     * Trouve toutes les publicités d'une société
     */
    @Query("SELECT p FROM Publicite p JOIN FETCH p.societe WHERE p.societe.id = :societeId " +
           "ORDER BY p.dateDiffusion DESC")
    List<Publicite> findBySocieteId(@Param("societeId") Long societeId);
    
    /**
     * Somme du nombre de diffusions pour un mois/année
     */
    @Query("SELECT COALESCE(SUM(p.nbFois), 0) FROM Publicite p " +
           "WHERE YEAR(p.dateDiffusion) = :annee AND MONTH(p.dateDiffusion) = :mois")
    Integer sumNbFoisByYearMonth(@Param("annee") Integer annee, @Param("mois") Integer mois);
    
    /**
     * Liste des années distinctes ayant des publicités
     */
    @Query("SELECT DISTINCT YEAR(p.dateDiffusion) FROM Publicite p ORDER BY YEAR(p.dateDiffusion) DESC")
    List<Integer> findDistinctYears();
    
    /**
     * Liste des mois distincts pour une année (retourne les numéros de mois)
     */
    @Query("SELECT DISTINCT MONTH(p.dateDiffusion) FROM Publicite p " +
           "WHERE YEAR(p.dateDiffusion) = :annee ORDER BY MONTH(p.dateDiffusion)")
    List<Integer> findDistinctMonthsByYear(@Param("annee") Integer annee);
}
