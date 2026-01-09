package org.example.cinema.repository.custom.impl;

import org.example.cinema.model.Film;
import org.example.cinema.repository.custom.FilmRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FilmRepositoryCustomImpl implements FilmRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Film> findWithFilters(String title, Long genreId, String langue, LocalDate dateFrom, LocalDate dateTo) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Film> cq = cb.createQuery(Film.class);
        Root<Film> film = cq.from(Film.class);
        film.fetch("genres", JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            predicates.add(cb.like(cb.lower(film.get("titre")), "%" + title.toLowerCase() + "%"));
        }
        if (langue != null && !langue.isBlank()) {
            predicates.add(cb.equal(cb.lower(film.get("langueOriginale")), langue.toLowerCase()));
        }
        if (dateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(film.get("dateSortie"), dateFrom));
        }
        if (dateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(film.get("dateSortie"), dateTo));
        }
        if (genreId != null) {
            Join<Object, Object> genres = film.join("genres", JoinType.INNER);
            predicates.add(cb.equal(genres.get("id"), genreId));
            cq.distinct(true);
        }

        cq.select(film).where(predicates.toArray(new Predicate[0])).orderBy(cb.asc(film.get("titre")));
        TypedQuery<Film> query = em.createQuery(cq);
        return query.getResultList();
    }
}
