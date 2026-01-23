-- ======================================
-- DATA.SQL - Données de test CinéMax
-- Exécuter après script.sql
-- ======================================

  -- EXEMPLES STATUTS INITIAUX
    -- ------------------------------
    INSERT INTO statut_reservation (code, libelle, est_final) VALUES
    ('CREEE', 'Creee', false),
    ('EN_ATTENTE', 'En attente de paiement', false),
    ('PAYEE', 'Payee', false),
    ('CONFIRMEE', 'Confirmee', false),
    ('ANNULEE', 'Annulee', true),
    ('EXPIREE', 'Expiree', true);

    INSERT INTO statut_ticket (code, libelle, est_final) VALUES
    ('RESERVE', 'Reserve', false),
    ('PAYE', 'Paye', false),
    ('ANNULE', 'Annule', true),
    ('UTILISE', 'Utilise', true),
    ('REMBOURSE', 'Rembourse', true);    
    -- ------------------------------
    -- TYPES DE PLACES AVEC COULEURS
    -- ------------------------------
    INSERT INTO type_place (libelle, couleur) VALUES
    ('STANDARD', '#6c757d'),
    ('VIP', '#FFD700'),
    ('PREMIUM', '#9B59B6');


-- Clients de test
INSERT INTO personne (nom_complet, email, telephone, mot_de_passe) VALUES
('Jean Dupont', 'jean@test.com', '+261 34 00 000 01', 'test123'),
('Marie Martin', 'marie@test.com', '+261 34 00 000 02', 'test123'),
('Admin CinéMax', 'admin@cinemax.mg', '+261 34 00 000 00', 'admin123');

-- Catégories de personne
INSERT INTO categorie_personne (libelle) VALUES ('ADULTE');
INSERT INTO categorie_personne (libelle) VALUES ('ENFANT');
INSERT INTO categorie_personne (libelle) VALUES ('ADO');

-- Genres
INSERT INTO genre (libelle) VALUES ('ACTION');
INSERT INTO genre (libelle) VALUES ('COMEDIE');
INSERT INTO genre (libelle) VALUES ('DRAME');
INSERT INTO genre (libelle) VALUES ('SCIENCE-FICTION');
INSERT INTO genre (libelle) VALUES ('THRILLER');
INSERT INTO genre (libelle) VALUES ('ANIMATION');
INSERT INTO genre (libelle) VALUES ('HORREUR');
INSERT INTO genre (libelle) VALUES ('FAMILIAL');
INSERT INTO genre (libelle) VALUES ('ROMANCE');

-- Film Titanic
INSERT INTO film (titre, description, duree_minutes, date_sortie, age_min, langue_originale) VALUES
('Titanic', 'Un artiste pauvre et une jeune femme de la haute société tombent amoureux à bord du paquebot Titanic lors de son voyage inaugural en 1912.', 195, '1997-12-19', 12, 'EN');

-- Association Titanic - Romance + Drame
INSERT INTO film_genre (id_film, id_genre) VALUES (1, 3); -- Titanic - Drame
INSERT INTO film_genre (id_film, id_genre) VALUES (1, 9); -- Titanic - Romance

-- Salle de 100 places standard
INSERT INTO salle (nom, capacite) VALUES ('Salle 1', 100);

-- 100 places standard (10 rangées x 10 places)
INSERT INTO place (id_salle, rangee, numero, code_place, id_type_place) 
SELECT 1, r.rangee, n.numero, r.rangee || n.numero, 1
FROM (VALUES ('A'), ('B'), ('C'), ('D'), ('E'), ('F'), ('G'), ('H'), ('I'), ('J')) AS r(rangee)
CROSS JOIN (VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)) AS n(numero);

-- ------------------------------
-- TARIFS PAR DEFAUT
-- ------------------------------
INSERT INTO tarif_defaut (id_type_place, id_categorie_personne, prix) VALUES
(1, 1, 30000),  -- Standard - Adulte
(1, 2, 30000),  -- Standard - Enfant
(1, 3, 30000);  -- Standard - Ado

-- ------------------------------
-- SEANCES TITANIC
-- ------------------------------
-- Titanic a une durée de 195 minutes = 3h15
INSERT INTO seance (id_film, id_salle, debut, fin, langue) VALUES
(1, 1, '2026-01-20 10:00:00', '2026-01-20 13:15:00', 'VF'),  -- Séance 1: 20 janvier 2026 à 10h
(1, 1, '2026-01-21 10:00:00', '2026-01-21 13:15:00', 'VF'),  -- Séance 2: 21 janvier 2026 à 10h
(1, 1, '2026-01-21 15:00:00', '2026-01-21 18:15:00', 'VF');  -- Séance 3: 21 janvier 2026 à 15h

-- ------------------------------
-- TARIFS PAR SEANCE (30000 Ar pour tous)
-- ------------------------------
INSERT INTO tarif_seance (id_seance, id_type_place, id_categorie_personne, prix) VALUES
-- Séance 1 (20 janvier 10h)
(1, 1, 1, 30000),  -- Standard - Adulte
(1, 1, 2, 30000),  -- Standard - Enfant
(1, 1, 3, 30000),  -- Standard - Ado
-- Séance 2 (21 janvier 10h)
(2, 1, 1, 30000),  -- Standard - Adulte
(2, 1, 2, 30000),  -- Standard - Enfant
(2, 1, 3, 30000),  -- Standard - Ado
-- Séance 3 (21 janvier 15h)
(3, 1, 1, 30000),  -- Standard - Adulte
(3, 1, 2, 30000),  -- Standard - Enfant
(3, 1, 3, 30000);  -- Standard - Ado

-- ------------------------------
-- SOCIETES PUBLICITAIRES
-- ------------------------------
INSERT INTO societe (libelle) VALUES
('Vaniala'),
('Lewis'),
('Socobis');

-- ------------------------------
-- PRIX PUBLICITE (100 Ar par diffusion par exemple)
-- ------------------------------
INSERT INTO prix_publicite (prix, date_creation) VALUES
(200000, '2026-01-01 00:00:00');

-- ------------------------------
-- PUBLICITES
-- ------------------------------
-- Publicité Vaniala (pour séance 1 et séance 2)
INSERT INTO publicite (id_societe, cree_le) VALUES
(1, '2026-01-15 10:00:00');  -- Publicité 1 - Vaniala

-- Publicité Lewis (pour séance 1)
INSERT INTO publicite (id_societe, cree_le) VALUES
(2, '2026-01-15 11:00:00');  -- Publicité 2 - Lewis

-- Publicité Socobis (pour séance 2)
INSERT INTO publicite (id_societe, cree_le) VALUES
(3, '2026-01-15 12:00:00');  -- Publicité 3 - Socobis

-- ------------------------------
-- DETAILS PUBLICITES (diffusions par séance)
-- ------------------------------
-- Séance 1 (20 janvier 10h): Pub Vaniala 1, Pub Lewis 1
INSERT INTO publicite_detail (id_publicite, id_seance, nb_fois) VALUES
(1, 1, 1),  -- Vaniala - Séance 1 - 1 diffusion
(2, 1, 1);  -- Lewis - Séance 1 - 1 diffusion

-- Séance 2 (21 janvier 10h): Pub Vaniala 2, Pub Socobis 1
INSERT INTO publicite_detail (id_publicite, id_seance, nb_fois) VALUES
(1, 2, 2),  -- Vaniala - Séance 2 - 2 diffusions
(3, 2, 1);  -- Socobis - Séance 2 - 1 diffusion

-- Séance 3 (21 janvier 15h): 0 pub (aucune insertion)
