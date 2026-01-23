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
(1, 2, 20000),  -- Standard - Enfant
(1, 3, 25000);  -- Standard - Senior
