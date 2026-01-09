package org.example.cinema.repository;

import org.example.cinema.model.CategoriePersonne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriePersonneRepository extends JpaRepository<CategoriePersonne, Long> {
}
