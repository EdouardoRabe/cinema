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
}
