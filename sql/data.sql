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
INSERT INTO categorie_personne (libelle) VALUES ('SENIOR');

-- Genres
INSERT INTO genre (libelle) VALUES ('ACTION');
INSERT INTO genre (libelle) VALUES ('COMEDIE');
INSERT INTO genre (libelle) VALUES ('DRAME');
INSERT INTO genre (libelle) VALUES ('SCIENCE-FICTION');
INSERT INTO genre (libelle) VALUES ('THRILLER');
INSERT INTO genre (libelle) VALUES ('ANIMATION');
INSERT INTO genre (libelle) VALUES ('HORREUR');
INSERT INTO genre (libelle) VALUES ('FAMILIAL');

-- Films
INSERT INTO film (titre, description, duree_minutes, date_sortie, age_min, langue_originale) VALUES
('Inception', 'Un voleur qui s''infiltre dans les rêves des autres pour voler leurs secrets se voit offrir une chance de rédemption.', 148, '2010-07-16', 12, 'EN'),
('Le Roi Lion', 'L''histoire épique d''un jeune lion qui doit reconquérir son royaume après la trahison de son oncle.', 88, '2019-07-19', 0, 'FR'),
('Avengers: Endgame', 'Les Avengers restants tentent d''inverser les actions de Thanos et restaurer l''équilibre de l''univers.', 181, '2019-04-26', 12, 'EN'),
('Parasite', 'Une famille pauvre s''infiltre dans une famille riche avec des conséquences inattendues et dramatiques.', 132, '2019-05-30', 16, 'KO'),
('Interstellar', 'Une équipe d''explorateurs voyage à travers un trou de ver dans l''espace pour assurer la survie de l''humanité.', 169, '2014-11-07', 10, 'EN'),
('Spider-Man: No Way Home', 'Peter Parker demande l''aide du Docteur Strange pour faire oublier son identité au monde entier.', 148, '2021-12-15', 12, 'EN'),
('Avatar: La Voie de l''Eau', 'Jake Sully et Neytiri ont formé une famille et font tout pour rester ensemble. Cependant, ils doivent quitter leur foyer et explorer les régions de Pandora.', 192, '2022-12-14', 12, 'EN');

-- Associations film-genre
INSERT INTO film_genre (id_film, id_genre) VALUES (1, 4); -- Inception - SF
INSERT INTO film_genre (id_film, id_genre) VALUES (1, 5); -- Inception - Thriller
INSERT INTO film_genre (id_film, id_genre) VALUES (2, 6); -- Roi Lion - Animation
INSERT INTO film_genre (id_film, id_genre) VALUES (2, 8); -- Roi Lion - Familial
INSERT INTO film_genre (id_film, id_genre) VALUES (3, 1); -- Avengers - Action
INSERT INTO film_genre (id_film, id_genre) VALUES (3, 4); -- Avengers - SF
INSERT INTO film_genre (id_film, id_genre) VALUES (4, 3); -- Parasite - Drame
INSERT INTO film_genre (id_film, id_genre) VALUES (4, 5); -- Parasite - Thriller
INSERT INTO film_genre (id_film, id_genre) VALUES (5, 4); -- Interstellar - SF
INSERT INTO film_genre (id_film, id_genre) VALUES (5, 3); -- Interstellar - Drame
INSERT INTO film_genre (id_film, id_genre) VALUES (6, 1); -- Spider-Man - Action
INSERT INTO film_genre (id_film, id_genre) VALUES (6, 4); -- Spider-Man - SF
INSERT INTO film_genre (id_film, id_genre) VALUES (7, 1); -- Avatar - Action
INSERT INTO film_genre (id_film, id_genre) VALUES (7, 4); -- Avatar - SF
INSERT INTO film_genre (id_film, id_genre) VALUES (7, 3); -- Avatar - Drame

-- Salles
INSERT INTO salle (nom, capacite) VALUES ('Salle 1 - IMAX', 50);
INSERT INTO salle (nom, capacite) VALUES ('Salle 2 - Standard', 30);
INSERT INTO salle (nom, capacite) VALUES ('Salle 3 - VIP', 20);
INSERT INTO salle (nom, capacite) VALUES ('Salle 4', 100);

-- Places Salle 1 (5 rangées x 10 places)
INSERT INTO place (id_salle, rangee, numero, code_place, id_type_place) 
SELECT 1, r.rangee, n.numero, r.rangee || n.numero, 
    CASE WHEN r.rangee = 'E' AND n.numero BETWEEN 4 AND 7 THEN 2 ELSE 1 END
FROM (VALUES ('A'), ('B'), ('C'), ('D'), ('E')) AS r(rangee)
CROSS JOIN (VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)) AS n(numero);

-- Places Salle 2 (3 rangées x 10 places)
INSERT INTO place (id_salle, rangee, numero, code_place, id_type_place) 
SELECT 2, r.rangee, n.numero, r.rangee || n.numero, 1
FROM (VALUES ('A'), ('B'), ('C')) AS r(rangee)
CROSS JOIN (VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)) AS n(numero);

-- Places Salle 3 VIP (2 rangées x 10 places, toutes VIP)
INSERT INTO place (id_salle, rangee, numero, code_place, id_type_place) 
SELECT 3, r.rangee, n.numero, r.rangee || n.numero, 2
FROM (VALUES ('A'), ('B')) AS r(rangee)
CROSS JOIN (VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)) AS n(numero);

-- Places Salle 4 : 70 Standard, 20 PMR, 10 VIP
WITH tp AS (
    SELECT
        (SELECT id FROM type_place WHERE libelle = 'STANDARD') AS std_id,
        (SELECT id FROM type_place WHERE libelle = 'VIP') AS vip_id,
        (SELECT id FROM type_place WHERE libelle = 'PREMIUM') AS pmr_id
)
INSERT INTO place (id_salle, rangee, numero, code_place, id_type_place)
SELECT 4, r.rangee, n.numero, r.rangee || n.numero,
       CASE
           WHEN r.rangee IN ('H','I') THEN tp.pmr_id   -- 20 PMR
           WHEN r.rangee = 'J' THEN tp.vip_id          -- 10 VIP
           ELSE tp.std_id                             -- 70 Standard
       END
FROM (VALUES ('A'), ('B'), ('C'), ('D'), ('E'), ('F'), ('G'), ('H'), ('I'), ('J')) AS r(rangee)
CROSS JOIN (VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)) AS n(numero)
CROSS JOIN tp;

-- Séances (pour les prochains jours)
INSERT INTO seance (id_film, id_salle, debut, fin, langue) VALUES
-- Aujourd'hui
(1, 1, CURRENT_TIMESTAMP + INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '4 hours 28 minutes', 'VF'),
(1, 1, CURRENT_TIMESTAMP + INTERVAL '6 hours', CURRENT_TIMESTAMP + INTERVAL '8 hours 28 minutes', 'VOST'),
(2, 2, CURRENT_TIMESTAMP + INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '3 hours 28 minutes', 'VF'),
(2, 2, CURRENT_TIMESTAMP + INTERVAL '5 hours', CURRENT_TIMESTAMP + INTERVAL '6 hours 28 minutes', 'VF'),
(3, 1, CURRENT_TIMESTAMP + INTERVAL '9 hours', CURRENT_TIMESTAMP + INTERVAL '12 hours 1 minute', 'VOST'),
(4, 3, CURRENT_TIMESTAMP + INTERVAL '3 hours', CURRENT_TIMESTAMP + INTERVAL '5 hours 12 minutes', 'VOST'),
-- Demain
(1, 1, CURRENT_TIMESTAMP + INTERVAL '1 day 2 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 4 hours 28 minutes', 'VF'),
(2, 2, CURRENT_TIMESTAMP + INTERVAL '1 day 2 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 3 hours 28 minutes', 'VF'),
(3, 1, CURRENT_TIMESTAMP + INTERVAL '1 day 5 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 8 hours 1 minute', 'VOST'),
(5, 3, CURRENT_TIMESTAMP + INTERVAL '1 day 3 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 5 hours 49 minutes', 'VOST'),
(6, 2, CURRENT_TIMESTAMP + INTERVAL '1 day 6 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 8 hours 28 minutes', 'VF'),
-- Après-demain
(1, 1, CURRENT_TIMESTAMP + INTERVAL '2 days 2 hours', CURRENT_TIMESTAMP + INTERVAL '2 days 4 hours 28 minutes', 'VOST'),
(4, 3, CURRENT_TIMESTAMP + INTERVAL '2 days 3 hours', CURRENT_TIMESTAMP + INTERVAL '2 days 5 hours 12 minutes', 'VOST'),
(5, 1, CURRENT_TIMESTAMP + INTERVAL '2 days 6 hours', CURRENT_TIMESTAMP + INTERVAL '2 days 8 hours 49 minutes', 'VF'),
(6, 2, CURRENT_TIMESTAMP + INTERVAL '2 days 2 hours', CURRENT_TIMESTAMP + INTERVAL '2 days 4 hours 28 minutes', 'VF'),
-- Dans 3 jours
(2, 2, CURRENT_TIMESTAMP + INTERVAL '3 days 2 hours', CURRENT_TIMESTAMP + INTERVAL '3 days 3 hours 28 minutes', 'VF'),
(3, 1, CURRENT_TIMESTAMP + INTERVAL '3 days 5 hours', CURRENT_TIMESTAMP + INTERVAL '3 days 8 hours 1 minute', 'VF'),
(4, 3, CURRENT_TIMESTAMP + INTERVAL '3 days 8 hours', CURRENT_TIMESTAMP + INTERVAL '3 days 10 hours 12 minutes', 'VOST'),
-- Avatar - Séances
(7, 1, '2026-01-10 10:00:00', '2026-01-10 13:12:00', 'VF'),  -- 10 janvier 2026 à 10h
(7, 1, '2026-01-10 15:00:00', '2026-01-10 18:12:00', 'VOST'),
(7, 3, '2026-01-10 20:00:00', '2026-01-10 23:12:00', 'VOST'),
(7, 1, '2026-01-11 14:00:00', '2026-01-11 17:12:00', 'VF'),
(7, 2, '2026-01-11 18:00:00', '2026-01-11 21:12:00', 'VF'),
(7, 1, '2026-01-12 10:00:00', '2026-01-12 13:12:00', 'VOST');

-- ------------------------------
-- TARIFS PAR DEFAUT
-- ------------------------------
INSERT INTO tarif_defaut (id_type_place, id_categorie_personne, prix) VALUES
(1, 1, 10000),  -- Standard - Adulte
(1, 2, 7000),   -- Standard - Enfant
(1, 3, 8000),   -- Standard - Senior
(2, 1, 30000),  -- VIP - Adulte
(2, 2, 20000),  -- VIP - Enfant
(2, 3, 25000),  -- VIP - Senior
(3, 1, 20000),  -- Premium - Adulte
(3, 2, 15000),  -- Premium - Enfant
(3, 3, 17000);  -- Premium - Senior

-- ------------------------------
-- TARIFS SPECIFIQUES PAR SEANCE (prix variés par séance)
-- Séance 1 (Inception - VF) - Tarifs normaux
-- ------------------------------
INSERT INTO tarif_seance (id_seance, id_type_place, id_categorie_personne, prix) VALUES
-- Séance 1: Inception VF - Prix standard
(1, 1, 1, 12000),  -- Standard - Adulte
(1, 1, 2, 8000),   -- Standard - Enfant
(1, 1, 3, 10000),  -- Standard - Senior
(1, 2, 1, 35000),  -- VIP - Adulte
(1, 2, 2, 25000),  -- VIP - Enfant
(1, 2, 3, 30000),  -- VIP - Senior

-- Séance 2: Inception VOST - Prix premium (soirée)
(2, 1, 1, 15000),  -- Standard - Adulte
(2, 1, 2, 10000),  -- Standard - Enfant
(2, 1, 3, 12000),  -- Standard - Senior
(2, 2, 1, 40000),  -- VIP - Adulte
(2, 2, 2, 28000),  -- VIP - Enfant
(2, 2, 3, 35000),  -- VIP - Senior

-- Séance 3: Le Roi Lion VF - Tarifs famille (réduits enfants)
(3, 1, 1, 10000),  -- Standard - Adulte
(3, 1, 2, 5000),   -- Standard - Enfant (réduit!)
(3, 1, 3, 8000),   -- Standard - Senior

-- Séance 4: Le Roi Lion VF après-midi
(4, 1, 1, 10000),  -- Standard - Adulte
(4, 1, 2, 5000),   -- Standard - Enfant (réduit!)
(4, 1, 3, 8000),   -- Standard - Senior

-- Séance 5: Avengers VOST soirée - Prix élevés
(5, 1, 1, 18000),  -- Standard - Adulte
(5, 1, 2, 12000),  -- Standard - Enfant
(5, 1, 3, 15000),  -- Standard - Senior
(5, 2, 1, 45000),  -- VIP - Adulte
(5, 2, 2, 32000),  -- VIP - Enfant
(5, 2, 3, 38000),  -- VIP - Senior

-- Séance 6: Parasite VOST en salle VIP
(6, 2, 1, 50000),  -- VIP - Adulte (exclusif)
(6, 2, 2, 35000),  -- VIP - Enfant
(6, 2, 3, 42000),  -- VIP - Senior

-- Séances Avatar (séances 19-24) - Prix spéciaux lancement
(19, 1, 1, 20000), -- Standard - Adulte
(19, 1, 2, 14000), -- Standard - Enfant
(19, 1, 3, 17000), -- Standard - Senior
(19, 2, 1, 55000), -- VIP - Adulte
(19, 2, 2, 40000), -- VIP - Enfant
(19, 2, 3, 48000), -- VIP - Senior

(20, 1, 1, 22000), -- VOST soirée - plus cher
(20, 1, 2, 15000),
(20, 1, 3, 18000),
(20, 2, 1, 58000),
(20, 2, 2, 42000),
(20, 2, 3, 50000),

(21, 2, 1, 60000), -- Séance VIP exclusive
(21, 2, 2, 45000),
(21, 2, 3, 52000);
  
update tarif_seance set prix = 80000 where id_seance=30 and id_type_place = 2 and id_categorie_personne=1;
update tarif_seance set prix = 40000 where id_seance=30 and id_type_place = 2 and id_categorie_personne=2;
