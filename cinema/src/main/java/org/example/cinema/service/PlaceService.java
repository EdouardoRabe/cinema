package org.example.cinema.service;

import org.example.cinema.model.Place;
import org.example.cinema.repository.PlaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    private final PlaceRepository repository;

    public PlaceService(PlaceRepository repository) {
        this.repository = repository;
    }

    public List<Place> findBySalleId(Long salleId) {
        return repository.findBySalleIdOrderByRangeeAscNumeroAsc(salleId);
    }

    public Map<String, List<Place>> groupByRow(List<Place> places) {
        return places.stream()
                .collect(Collectors.groupingBy(
                        Place::getRangee,
                        java.util.TreeMap::new,
                        Collectors.toList()
                ));
    }
}
