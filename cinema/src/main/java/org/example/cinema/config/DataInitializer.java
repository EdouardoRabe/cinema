package org.example.cinema.config;

import org.example.cinema.model.Film;
import org.example.cinema.repository.FilmRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(FilmRepository filmRepository) {
        return args -> {
            long count = filmRepository.count();
            System.out.println("Films in DB before init: " + count);
            if (count == 0) {
                Film f = Film.builder()
                        .titre("Exemple")
                        .description("Film initial")
                        .dureeMinutes(90)
                        .dateSortie(LocalDate.now())
                        .ageMin(0)
                        .langueOriginale("FR")
                        .build();
                filmRepository.save(f);
                System.out.println("Saved example film: " + f.getId());
            }
            System.out.println("Films after init: " + filmRepository.count());
        };
    }

    @Bean
    CommandLineRunner initStatuts(org.example.cinema.repository.StatutReservationRepository statutRepository) {
        return args -> {
            long c = statutRepository.count();
            System.out.println("StatutReservation in DB before init: " + c);
            if (c == 0) {
                statutRepository.save(org.example.cinema.model.StatutReservation.builder().code("CREEE").libelle("Creee").estFinal(false).build());
                statutRepository.save(org.example.cinema.model.StatutReservation.builder().code("EN_ATTENTE").libelle("En attente de paiement").estFinal(false).build());
                statutRepository.save(org.example.cinema.model.StatutReservation.builder().code("PAYEE").libelle("Payee").estFinal(false).build());
                statutRepository.save(org.example.cinema.model.StatutReservation.builder().code("CONFIRMEE").libelle("Confirmee").estFinal(false).build());
                statutRepository.save(org.example.cinema.model.StatutReservation.builder().code("ANNULEE").libelle("Annulee").estFinal(true).build());
                statutRepository.save(org.example.cinema.model.StatutReservation.builder().code("EXPIREE").libelle("Expiree").estFinal(true).build());
                System.out.println("Inserted default statut_reservation entries");
            }
            System.out.println("StatutReservation after init: " + statutRepository.count());
        };
    }
}
