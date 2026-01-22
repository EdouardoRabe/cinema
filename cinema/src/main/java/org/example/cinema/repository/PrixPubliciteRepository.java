package org.example.cinema.repository;

import org.example.cinema.model.PrixPublicite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PrixPubliciteRepository extends JpaRepository<PrixPublicite, Long> {
    
    /**
     * Trouve le prix le plus récent
     */
    @Query("SELECT p FROM PrixPublicite p ORDER BY p.dateCreation DESC LIMIT 1")
    Optional<PrixPublicite> findLatest();
    
    /**
     * Trouve le prix valide pour une date donnée (le plus récent avant ou égal à cette date)
     */
    @Query("SELECT p FROM PrixPublicite p WHERE p.dateCreation <= :date ORDER BY p.dateCreation DESC LIMIT 1")
    Optional<PrixPublicite> findPrixValidAt(@Param("date") LocalDateTime date);
}
