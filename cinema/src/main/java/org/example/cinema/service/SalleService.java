package org.example.cinema.service;

import org.example.cinema.model.Place;
import org.example.cinema.model.Salle;
import org.example.cinema.model.TypePlace;
import org.example.cinema.repository.PlaceRepository;
import org.example.cinema.repository.SalleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SalleService {

    private final SalleRepository repository;
    private final PlaceRepository placeRepository;
    private final TypePlaceService typePlaceService;

    public SalleService(SalleRepository repository, PlaceRepository placeRepository, TypePlaceService typePlaceService) {
        this.repository = repository;
        this.placeRepository = placeRepository;
        this.typePlaceService = typePlaceService;
    }

    public List<Salle> findAll() {
        return repository.findAll();
    }

    public Optional<Salle> findById(Long id) {
        return repository.findById(id);
    }
    
    public Salle save(Salle salle) {
        return repository.save(salle);
    }
    
    /**
     * Crée une salle et génère automatiquement les places avec le type STANDARD
     * @param nom Nom de la salle
     * @param capacite Nombre de places
     * @param colonnesParRangee Nombre de places par rangée (par défaut 10)
     * @return La salle créée
     */
    @Transactional
    public Salle createWithPlaces(String nom, int capacite, int colonnesParRangee) {
        // Créer la salle
        Salle salle = new Salle();
        salle.setNom(nom);
        salle.setCapacite(capacite);
        salle = repository.save(salle);
        
        // Récupérer le type STANDARD
        TypePlace typeStandard = typePlaceService.getStandard();
        
        // Générer les places
        int nbRangees = (int) Math.ceil((double) capacite / colonnesParRangee);
        int placesRestantes = capacite;
        
        for (int r = 0; r < nbRangees && placesRestantes > 0; r++) {
            String rangee = String.valueOf((char) ('A' + r));
            int placesRangee = Math.min(colonnesParRangee, placesRestantes);
            
            for (int n = 1; n <= placesRangee; n++) {
                Place place = new Place();
                place.setSalle(salle);
                place.setRangee(rangee);
                place.setNumero(n);
                place.setCodePlace(rangee + "-" + n);
                place.setTypePlace(typeStandard);
                placeRepository.save(place);
            }
            placesRestantes -= placesRangee;
        }
        
        return salle;
    }
    
    @Transactional
    public void deleteById(Long id) {
        // Les places sont supprimées automatiquement grâce à ON DELETE CASCADE
        repository.deleteById(id);
    }
}
