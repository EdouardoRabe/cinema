package org.example.cinema.service;

import org.example.cinema.model.Client;
import org.example.cinema.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public Optional<Client> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<Client> findById(Long id) {
        return repository.findById(id);
    }

    public boolean authenticate(String email, String motDePasse) {
        Optional<Client> clientOpt = repository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            return false;
        }
        Client client = clientOpt.get();
        // Simple password check (en production, utiliser BCrypt)
        return client.getMotDePasse() != null && client.getMotDePasse().equals(motDePasse);
    }

    public Client register(String nomComplet, String email, String telephone, String motDePasse) {
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        Client client = Client.builder()
                .nomComplet(nomComplet)
                .email(email)
                .telephone(telephone)
                .motDePasse(motDePasse)
                .creeLe(OffsetDateTime.now())
                .build();
        return repository.save(client);
    }
}
