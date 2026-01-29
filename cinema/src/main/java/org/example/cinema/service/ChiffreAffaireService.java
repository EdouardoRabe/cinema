package org.example.cinema.service;

import org.example.cinema.model.PubliciteDetail;
import org.example.cinema.model.Ticket;
import org.example.cinema.model.VenteDetail;
import org.example.cinema.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ChiffreAffaireService {

    private final PaiementRepository paiementRepository;
    private final PaiementPubliciteRepository paiementPubliciteRepository;
    private final PaiementVenteRepository paiementVenteRepository;
    private final TicketRepository ticketRepository;
    private final SeanceRepository seanceRepository;
    private final PubliciteDetailRepository publiciteDetailRepository;
    private final PrixPubliciteRepository prixPubliciteRepository;
    private final VenteRepository venteRepository;
    private final VenteDetailRepository venteDetailRepository;
    private final PubliciteService publiciteService;

    public ChiffreAffaireService(PaiementRepository paiementRepository,
                                  PaiementPubliciteRepository paiementPubliciteRepository,
                                  PaiementVenteRepository paiementVenteRepository,
                                  TicketRepository ticketRepository,
                                  SeanceRepository seanceRepository,
                                  PubliciteDetailRepository publiciteDetailRepository,
                                  PrixPubliciteRepository prixPubliciteRepository,
                                  VenteRepository venteRepository,
                                  VenteDetailRepository venteDetailRepository,
                                  PubliciteService publiciteService) {
        this.paiementRepository = paiementRepository;
        this.paiementPubliciteRepository = paiementPubliciteRepository;
        this.paiementVenteRepository = paiementVenteRepository;
        this.ticketRepository = ticketRepository;
        this.seanceRepository = seanceRepository;
        this.publiciteDetailRepository = publiciteDetailRepository;
        this.prixPubliciteRepository = prixPubliciteRepository;
        this.venteRepository = venteRepository;
        this.venteDetailRepository = venteDetailRepository;
        this.publiciteService = publiciteService;
    }

    // ========================
    // CA THEORIQUE (basé sur réservations/pub/ventes, pas sur paiements)
    // ========================

    /**
     * CA Tickets théorique = SUM(ticket.prix) pour les séances du mois
     * (toutes les places réservées × leur prix, peu importe le paiement)
     */
    public BigDecimal getCATicketsTheoriqueByMoisAnnee(int mois, int annee) {
        List<Long> seanceIds = seanceRepository.findIdsByMoisAnnee(mois, annee);
        BigDecimal total = BigDecimal.ZERO;
        for (Long seanceId : seanceIds) {
            List<Ticket> tickets = ticketRepository.findBySeanceId(seanceId);
            for (Ticket t : tickets) {
                if (t.getPrix() != null) {
                    total = total.add(t.getPrix());
                }
            }
        }
        return total;
    }

    /**
     * CA Publicités théorique = SUM(nb_fois × prix_publicite) pour les séances du mois
     */
    public BigDecimal getCAPublicitesTheoriqueByMoisAnnee(int mois, int annee) {
        List<PubliciteDetail> details = publiciteDetailRepository.findBySeanceMoisAnnee(annee, mois);
        BigDecimal total = BigDecimal.ZERO;
        for (PubliciteDetail detail : details) {
            BigDecimal montantDetail = publiciteService.calculerMontantDetail(detail);
            total = total.add(montantDetail);
        }
        return total;
    }

    /**
     * CA Ventes théorique = SUM(quantite × prix_unitaire) pour les ventes du mois
     */
    public BigDecimal getCAVentesTheoriqueByMoisAnnee(int mois, int annee) {
        var ventes = venteRepository.findByMoisAnnee(mois, annee);
        BigDecimal total = BigDecimal.ZERO;
        for (var vente : ventes) {
            List<VenteDetail> details = venteDetailRepository.findByVenteId(vente.getId());
            for (VenteDetail vd : details) {
                BigDecimal montant = vd.getPrixUnitaire().multiply(BigDecimal.valueOf(vd.getQuantite()));
                total = total.add(montant);
            }
        }
        return total;
    }

    /**
     * CA Total théorique
     */
    public BigDecimal getCATotalTheoriqueByMoisAnnee(int mois, int annee) {
        BigDecimal caTickets = getCATicketsTheoriqueByMoisAnnee(mois, annee);
        BigDecimal caPub = getCAPublicitesTheoriqueByMoisAnnee(mois, annee);
        BigDecimal caVentes = getCAVentesTheoriqueByMoisAnnee(mois, annee);
        return caTickets.add(caPub).add(caVentes);
    }

    // ========================
    // TOTAL PAYE (basé sur les paiements des réservations/pub/ventes du mois)
    // ========================

    /**
     * Total payé tickets = paiements des réservations dont les tickets sont sur des séances du mois
     */
    public BigDecimal getTotalPayeTicketsByMoisAnnee(int mois, int annee) {
        return paiementRepository.getTotalPayeBySeanceMoisAnnee(mois, annee);
    }

    /**
     * Total payé publicités = montant payé au prorata pour les séances du mois
     */
    public BigDecimal getTotalPayePublicitesByMoisAnnee(int mois, int annee) {
        return publiciteService.calculerCAPubPourMois(annee, mois, 
            pubId -> paiementPubliciteRepository.getTotalPayeParPublicite(pubId));
    }

    /**
     * Total payé ventes = paiements des ventes du mois
     */
    public BigDecimal getTotalPayeVentesByMoisAnnee(int mois, int annee) {
        var ventes = venteRepository.findByMoisAnnee(mois, annee);
        BigDecimal total = BigDecimal.ZERO;
        for (var vente : ventes) {
            BigDecimal paye = paiementVenteRepository.getTotalPayeByVenteId(vente.getId());
            total = total.add(paye);
        }
        return total;
    }

    /**
     * Total payé global
     */
    public BigDecimal getTotalPayeByMoisAnnee(int mois, int annee) {
        BigDecimal tickets = getTotalPayeTicketsByMoisAnnee(mois, annee);
        BigDecimal pubs = getTotalPayePublicitesByMoisAnnee(mois, annee);
        BigDecimal ventes = getTotalPayeVentesByMoisAnnee(mois, annee);
        return tickets.add(pubs).add(ventes);
    }

    // ========================
    // RESTE A PAYER
    // ========================

    public BigDecimal getResteAPayerTicketsByMoisAnnee(int mois, int annee) {
        return getCATicketsTheoriqueByMoisAnnee(mois, annee)
                .subtract(getTotalPayeTicketsByMoisAnnee(mois, annee));
    }

    public BigDecimal getResteAPayerPublicitesByMoisAnnee(int mois, int annee) {
        return getCAPublicitesTheoriqueByMoisAnnee(mois, annee)
                .subtract(getTotalPayePublicitesByMoisAnnee(mois, annee));
    }

    public BigDecimal getResteAPayerVentesByMoisAnnee(int mois, int annee) {
        return getCAVentesTheoriqueByMoisAnnee(mois, annee)
                .subtract(getTotalPayeVentesByMoisAnnee(mois, annee));
    }

    public BigDecimal getResteAPayerTotalByMoisAnnee(int mois, int annee) {
        return getCATotalTheoriqueByMoisAnnee(mois, annee)
                .subtract(getTotalPayeByMoisAnnee(mois, annee));
    }

    // ========================
    // Anciennes méthodes (pour compatibilité)
    // ========================

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
