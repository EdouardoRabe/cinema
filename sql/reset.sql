-- ================================================
-- SCRIPT DE RESET SIMPLE (TRUNCATE)
-- Vide toutes les tables sans les supprimer
-- ================================================

\c cinema;

-- Désactiver les contraintes de clés étrangères temporairement
SET session_replication_role = 'replica';

-- Truncate toutes les tables (ordre inverse des dépendances)
TRUNCATE TABLE remise RESTART IDENTITY CASCADE;
TRUNCATE TABLE paiement RESTART IDENTITY CASCADE;
TRUNCATE TABLE tarif_seance RESTART IDENTITY CASCADE;
TRUNCATE TABLE tarif_defaut RESTART IDENTITY CASCADE;
TRUNCATE TABLE historique_statut_ticket RESTART IDENTITY CASCADE;
TRUNCATE TABLE ticket RESTART IDENTITY CASCADE;
TRUNCATE TABLE historique_statut_reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE seance RESTART IDENTITY CASCADE;
TRUNCATE TABLE place RESTART IDENTITY CASCADE;
TRUNCATE TABLE salle RESTART IDENTITY CASCADE;
TRUNCATE TABLE film_genre RESTART IDENTITY CASCADE;
TRUNCATE TABLE genre RESTART IDENTITY CASCADE;
TRUNCATE TABLE film RESTART IDENTITY CASCADE;
TRUNCATE TABLE personne RESTART IDENTITY CASCADE;
TRUNCATE TABLE statut_ticket RESTART IDENTITY CASCADE;
TRUNCATE TABLE statut_reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE categorie_personne RESTART IDENTITY CASCADE;
TRUNCATE TABLE type_place RESTART IDENTITY CASCADE;

-- Réactiver les contraintes
SET session_replication_role = 'origin';

-- Message de confirmation
DO $$
BEGIN
    RAISE NOTICE 'Base de données vidée avec succès !';
END $$;
