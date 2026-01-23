package org.example.cinema.repository;

import org.example.cinema.model.Publicite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PubliciteRepository extends JpaRepository<Publicite, Long> {
    
    /**
     * Trouve toutes les publicités avec leur société (EAGER fetch)
     */
    @Query("SELECT DISTINCT p FROM Publicite p " +
           "JOIN FETCH p.societe " +
           "LEFT JOIN FETCH p.details " +
           "ORDER BY p.creeLe DESC, p.societe.libelle")
    List<Publicite> findAllWithSocieteAndDetails();
    
    /**
     * Trouve toutes les publicités d'une société
     */
    @Query("SELECT DISTINCT p FROM Publicite p " +
           "JOIN FETCH p.societe " +
           "LEFT JOIN FETCH p.details " +
           "WHERE p.societe.id = :societeId " +
           "ORDER BY p.creeLe DESC")
    List<Publicite> findBySocieteId(@Param("societeId") Long societeId);
    
    /**
     * Trouve une publicité avec ses détails
     */
    @Query("SELECT p FROM Publicite p " +
           "JOIN FETCH p.societe " +
           "LEFT JOIN FETCH p.details d " +
           "LEFT JOIN FETCH d.seance s " +
           "LEFT JOIN FETCH s.film " +
           "LEFT JOIN FETCH s.salle " +
           "WHERE p.id = :id")
    Publicite findByIdWithDetails(@Param("id") Long id);
}
