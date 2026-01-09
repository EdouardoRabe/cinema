package org.example.cinema.repository;

import org.example.cinema.model.TarifDefaut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifDefautRepository extends JpaRepository<TarifDefaut, Long> {
    
    List<TarifDefaut> findByTypePlaceId(Long typePlaceId);
    
    List<TarifDefaut> findByCategoriePersonneId(Long categoriePersonneId);
    
    Optional<TarifDefaut> findByTypePlaceIdAndCategoriePersonneId(Long typePlaceId, Long categoriePersonneId);
    
    @Query("SELECT t FROM TarifDefaut t JOIN FETCH t.typePlace JOIN FETCH t.categoriePersonne")
    List<TarifDefaut> findAllWithDetails();
}
