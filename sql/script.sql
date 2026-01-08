\c postgres;
DROP DATABASE IF EXISTS cinema;
CREATE DATABASE cinema;
\c cinema;

-- ------------------------------
-- TYPE DE PLACE
-- ------------------------------
CREATE TABLE type_place (
    id SERIAL PRIMARY KEY,
    libelle TEXT NOT NULL -- STANDARD, VIP, PMR
);

-- ------------------------------
-- CATEGORIE PERSONNE
-- ------------------------------
CREATE TABLE categorie_personne (
    id SERIAL PRIMARY KEY,
    libelle TEXT NOT NULL -- ADULTE, ENFANT, SENIOR...
);

-- ------------------------------
-- FILMS & GENRES
-- ------------------------------
CREATE TABLE film (
    id SERIAL PRIMARY KEY,
    titre TEXT NOT NULL,
    description TEXT,
    duree_minutes INT,
    date_sortie DATE,
    age_min INT DEFAULT 0, -- age minimum conseille
    langue_originale TEXT, -- langue du film
    cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE genre (
    id SERIAL PRIMARY KEY,
    libelle TEXT UNIQUE NOT NULL
);

CREATE TABLE film_genre (
    id_film INT REFERENCES film(id) ON DELETE CASCADE,
    id_genre INT REFERENCES genre(id) ON DELETE CASCADE,
    PRIMARY KEY (id_film, id_genre)
);

-- ------------------------------
-- SALLES & PLACES
-- ------------------------------
CREATE TABLE salle (
    id SERIAL PRIMARY KEY,
    nom TEXT NOT NULL,
    capacite INT NOT NULL CHECK (capacite > 0),
    cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE place (
    id SERIAL PRIMARY KEY,
    id_salle INT REFERENCES salle(id) ON DELETE CASCADE,
    rangee TEXT,
    numero INT,
    code_place TEXT,
    id_type_place INT REFERENCES type_place(id),
    cree_le TIMESTAMPTZ DEFAULT now(),
    UNIQUE (id_salle, rangee, numero)
);

-- ------------------------------
-- SEANCES
-- ------------------------------
CREATE TABLE seance (
    id SERIAL PRIMARY KEY,
    id_film INT REFERENCES film(id),
    id_salle INT REFERENCES salle(id),
    debut TIMESTAMPTZ NOT NULL,
    fin TIMESTAMPTZ,
    langue TEXT,
    cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_seance_salle_debut
ON seance(id_salle, debut);

-- ------------------------------
-- PERSONNES
-- ------------------------------
CREATE TABLE personne (
    id SERIAL PRIMARY KEY,
    nom_complet TEXT,
    email TEXT UNIQUE,
    telephone TEXT,
    cree_le TIMESTAMPTZ DEFAULT now()
);

-- ------------------------------
-- STATUTS RESERVATION
-- ------------------------------
CREATE TABLE statut_reservation (
    id SERIAL PRIMARY KEY,
    code TEXT UNIQUE NOT NULL, -- CREEE, EN_ATTENTE, PAYEE, CONFIRMEE, ANNULEE, EXPIREE
    libelle TEXT NOT NULL,
    est_final BOOLEAN DEFAULT false
);

-- ------------------------------
-- RESERVATIONS
-- ------------------------------
CREATE TABLE reservation (
    id SERIAL PRIMARY KEY,
    id_personne INT REFERENCES personne(id) NULL, -- nullable pour vente sur place
    id_seance INT REFERENCES seance(id),
    id_statut INT REFERENCES statut_reservation(id),
    montant_total NUMERIC(6,2) DEFAULT 0,
    cree_le TIMESTAMPTZ DEFAULT now()
);

-- ------------------------------
-- HISTORIQUE STATUT RESERVATION
-- ------------------------------
CREATE TABLE historique_statut_reservation (
    id SERIAL PRIMARY KEY,
    id_reservation INT REFERENCES reservation(id) ON DELETE CASCADE,
    id_statut INT REFERENCES statut_reservation(id),
    date_changement TIMESTAMPTZ DEFAULT now(),
    change_par INT REFERENCES personne(id),
    commentaire TEXT
);

-- ------------------------------
-- STATUTS TICKET
-- ------------------------------
CREATE TABLE statut_ticket (
    id SERIAL PRIMARY KEY,
    code TEXT UNIQUE NOT NULL, -- RESERVE, PAYE, ANNULE, UTILISE, REMBOURSE
    libelle TEXT NOT NULL,
    est_final BOOLEAN DEFAULT false
);

-- ------------------------------
-- TICKETS
-- ------------------------------
CREATE TABLE ticket (
    id SERIAL PRIMARY KEY,
    id_reservation INT REFERENCES reservation(id) NULL, -- nullable pour ticket sans reservation
    id_seance INT REFERENCES seance(id),
    id_place INT REFERENCES place(id),
    id_statut INT REFERENCES statut_ticket(id),
    id_categorie_personne INT REFERENCES categorie_personne(id), -- adulte/enfant
    prix NUMERIC(6,2) NOT NULL,
    cree_le TIMESTAMPTZ DEFAULT now(),
    UNIQUE (id_seance, id_place)
);

-- ------------------------------
-- HISTORIQUE STATUT TICKET
-- ------------------------------
CREATE TABLE historique_statut_ticket (
    id SERIAL PRIMARY KEY,
    id_ticket INT REFERENCES ticket(id) ON DELETE CASCADE,
    id_statut INT REFERENCES statut_ticket(id),
    date_changement TIMESTAMPTZ DEFAULT now(),
    change_par INT REFERENCES personne(id),
    commentaire TEXT
);

-- ------------------------------
-- TARIF PAR DEFAUT
-- ------------------------------
CREATE TABLE tarif_defaut (
    id SERIAL PRIMARY KEY,
    id_type_place INT REFERENCES type_place(id),
    id_categorie_personne INT REFERENCES categorie_personne(id),
    prix NUMERIC(6,2) NOT NULL
);

-- ------------------------------
-- TARIF SPECIFIQUE PAR SEANCE (OPTIONNEL)
-- ------------------------------
CREATE TABLE tarif_seance (
    id SERIAL PRIMARY KEY,
    id_seance INT REFERENCES seance(id),
    id_type_place INT REFERENCES type_place(id),
    id_categorie_personne INT REFERENCES categorie_personne(id),
    prix NUMERIC(6,2) NOT NULL
);

-- ------------------------------
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
