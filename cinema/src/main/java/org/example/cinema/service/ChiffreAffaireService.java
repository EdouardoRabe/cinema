package org.example.cinema.service;

import org.example.cinema.repository.PaiementPubliciteRepository;
import org.example.cinema.repository.PaiementRepository;
import org.example.cinema.repository.PaiementVenteRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ChiffreAffaireService {

    private final PaiementRepository paiementRepository;
    private final PaiementPubliciteRepository paiementPubliciteRepository;
    private final PaiementVenteRepository paiementVenteRepository;

    public ChiffreAffaireService(PaiementRepository paiementRepository,
                                  PaiementPubliciteRepository paiementPubliciteRepository,
                                  PaiementVenteRepository paiementVenteRepository) {
        this.paiementRepository = paiementRepository;
        this.paiementPubliciteRepository = paiementPubliciteRepository;
        this.paiementVenteRepository = paiementVenteRepository;
    }

    public BigDecimal getCATicketsByMoisAnnee(int mois, int annee) {
        return paiementRepository.getTotalPayeByMoisAnnee(mois, annee);
    }

    public BigDecimal getCAPublicitesByMoisAnnee(int mois, int annee) {
        return paiementPubliciteRepository.getTotalPayeByMoisAnnee(mois, annee);
    }

    public BigDecimal getCAVentesByMoisAnnee(int mois, int annee) {
        return paiementVenteRepository.getTotalPayeByMoisAnnee(mois, annee);
    }

    public BigDecimal getCATotalByMoisAnnee(int mois, int annee) {
        BigDecimal caTickets = getCATicketsByMoisAnnee(mois, annee);
        BigDecimal caPublicites = getCAPublicitesByMoisAnnee(mois, annee);
        BigDecimal caVentes = getCAVentesByMoisAnnee(mois, annee);
        return caTickets.add(caPublicites).add(caVentes);
    }
}
