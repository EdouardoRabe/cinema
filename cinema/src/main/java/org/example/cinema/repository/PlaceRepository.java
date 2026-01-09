package org.example.cinema.repository;

import org.example.cinema.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findBySalleIdOrderByRangeeAscNumeroAsc(Long salleId);
}
