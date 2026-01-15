package org.example.cinema.service;

import org.example.cinema.model.Place;
import org.example.cinema.model.TypePlace;
import org.example.cinema.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    
    public Optional<Place> findById(Long id) {
        return repository.findById(id);
    }

    public Map<String, List<Place>> groupByRow(List<Place> places) {
        return places.stream()
                .collect(Collectors.groupingBy(
                        Place::getRangee,
                        java.util.TreeMap::new,
                        Collectors.toList()
                ));
    }
    
    public Place save(Place place) {
        return repository.save(place);
    }
    
    @Transactional
    public void updateTypePlace(Long placeId, TypePlace typePlace) {
        repository.findById(placeId).ifPresent(place -> {
            place.setTypePlace(typePlace);
            repository.save(place);
        });
    }
    
    @Transactional
    public void updateTypePlaces(List<Long> placeIds, TypePlace typePlace) {
        for (Long id : placeIds) {
            updateTypePlace(id, typePlace);
        }
    }
}
