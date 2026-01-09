package org.example.cinema.repository;

import org.example.cinema.model.StatutTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatutTicketRepository extends JpaRepository<StatutTicket, Long> {
    Optional<StatutTicket> findByCode(String code);
}
