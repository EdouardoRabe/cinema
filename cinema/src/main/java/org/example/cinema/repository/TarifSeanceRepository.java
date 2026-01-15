package org.example.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.example.cinema.model.TarifSeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifSeanceRepository extends JpaRepository<TarifSeance, Long> {
    Optional<TarifSeance> findBySeanceIdAndTypePlaceIdAndCategoriePersonneId(Long seanceId, Long typePlaceId,
            Long categoriePersonneId);

    List<TarifSeance> findBySeanceId(Long seanceId);

    void deleteBySeanceId(Long seanceId);
}
