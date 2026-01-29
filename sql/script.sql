\c postgres;
DROP DATABASE IF EXISTS cinema;
CREATE DATABASE cinema;
\c cinema;

CREATE TABLE type_place (
    id SERIAL PRIMARY KEY,
    libelle TEXT NOT NULL UNIQUE,
    couleur TEXT DEFAULT '#6c757d'
);

CREATE TABLE categorie_personne (
    id SERIAL PRIMARY KEY,
    libelle TEXT NOT NULL 
);


CREATE TABLE film (
    id SERIAL PRIMARY KEY,
    titre TEXT NOT NULL,
    description TEXT,
    duree_minutes INT,
    date_sortie DATE,
    age_min INT DEFAULT 0, 
    langue_originale TEXT, 
    cree_le TIMESTAMP DEFAULT now()
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


    CREATE TABLE salle (
        id SERIAL PRIMARY KEY,
        nom TEXT NOT NULL,
        capacite INT NOT NULL CHECK (capacite > 0),
        cree_le TIMESTAMP DEFAULT now()
    );

    CREATE TABLE place (
        id SERIAL PRIMARY KEY,
        id_salle INT REFERENCES salle(id) ON DELETE CASCADE,
        rangee TEXT,
        numero INT,
        code_place TEXT,
        id_type_place INT REFERENCES type_place(id),
        cree_le TIMESTAMP DEFAULT now(),
        UNIQUE (id_salle, rangee, numero)
    );


    CREATE TABLE seance (
        id SERIAL PRIMARY KEY,
        id_film INT REFERENCES film(id),
        id_salle INT REFERENCES salle(id),
        debut TIMESTAMP NOT NULL,
        fin TIMESTAMP,
        langue TEXT,
        cree_le TIMESTAMP DEFAULT now()
    );

    CREATE INDEX idx_seance_salle_debut
    ON seance(id_salle, debut);


    CREATE TABLE personne (
        id SERIAL PRIMARY KEY,
        nom_complet TEXT,
        email TEXT UNIQUE,
        telephone TEXT,
        mot_de_passe TEXT,
        cree_le TIMESTAMP DEFAULT now()
    );

    CREATE TABLE statut_reservation (
        id SERIAL PRIMARY KEY,
        code TEXT UNIQUE NOT NULL, 
        libelle TEXT NOT NULL,
        est_final BOOLEAN DEFAULT false
    );

    CREATE TABLE reservation (
        id SERIAL PRIMARY KEY,
        id_personne INT REFERENCES personne(id) NULL, 
        id_statut INT REFERENCES statut_reservation(id),
        montant_total NUMERIC(6,2) DEFAULT 0,
        cree_le TIMESTAMP DEFAULT now()
    );


    CREATE TABLE historique_statut_reservation (
        id SERIAL PRIMARY KEY,
        id_reservation INT REFERENCES reservation(id) ON DELETE CASCADE,
        id_statut INT REFERENCES statut_reservation(id),
        date_changement TIMESTAMP DEFAULT now(),
        change_par INT REFERENCES personne(id),
        commentaire TEXT
    );

    CREATE TABLE statut_ticket (
        id SERIAL PRIMARY KEY,
        code TEXT UNIQUE NOT NULL, 
        libelle TEXT NOT NULL,
        est_final BOOLEAN DEFAULT false
    );


    CREATE TABLE ticket (
        id SERIAL PRIMARY KEY,
        id_reservation INT REFERENCES reservation(id) NULL, -- nullable pour ticket sans reservation
        id_seance INT REFERENCES seance(id),
        id_place INT REFERENCES place(id),
        id_statut INT REFERENCES statut_ticket(id),
        id_categorie_personne INT REFERENCES categorie_personne(id), -- adulte/enfant
        prix NUMERIC(10,2) NOT NULL,
        cree_le TIMESTAMP DEFAULT now(),
        UNIQUE (id_seance, id_place)
    );

    -- ------------------------------
    -- HISTORIQUE STATUT TICKET
    -- ------------------------------
    CREATE TABLE historique_statut_ticket (
        id SERIAL PRIMARY KEY,
        id_ticket INT REFERENCES ticket(id) ON DELETE CASCADE,
        id_statut INT REFERENCES statut_ticket(id),
        date_changement TIMESTAMP DEFAULT now(),
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
        prix NUMERIC(10,2) NOT NULL
    );

    -- ------------------------------
    -- TARIF SPECIFIQUE PAR SEANCE (OPTIONNEL)
    -- Si prix est NULL, le prix est calculé via la table remise
    -- date_creation permet de garder l'historique et prendre le plus récent
    -- ------------------------------
    CREATE TABLE tarif_seance (
        id SERIAL PRIMARY KEY,
        id_seance INT REFERENCES seance(id),
        id_type_place INT REFERENCES type_place(id),
        id_categorie_personne INT REFERENCES categorie_personne(id),
        prix NUMERIC(10,2),
        date_creation TIMESTAMP DEFAULT now()
    );

    CREATE INDEX idx_tarif_seance_lookup ON tarif_seance(id_seance, id_type_place, id_categorie_personne, date_creation DESC);

    -- ------------------------------
    -- PAIEMENT
    -- ------------------------------
    CREATE TABLE paiement (
        id SERIAL PRIMARY KEY,
        id_reservation INT REFERENCES reservation(id) ON DELETE CASCADE,
        montant_paye NUMERIC(10,2) NOT NULL,
        date_paiement TIMESTAMP DEFAULT now()
    );


    -- ------------------------------
    -- REMISE (pourcentage basé sur une autre catégorie)
    -- Si pourcentage < 0, la remise est désactivée (historique)
    -- date_creation permet de garder l'historique et prendre le plus récent
    -- ------------------------------
    CREATE TABLE remise (
        id SERIAL PRIMARY KEY,
        id_seance INT REFERENCES seance(id) ON DELETE CASCADE,
        id_type_place INT REFERENCES type_place(id),
        id_categorie_personne_cible INT REFERENCES categorie_personne(id),
        id_categorie_personne_repere INT REFERENCES categorie_personne(id),
        pourcentage NUMERIC(5,2) NOT NULL,
        date_creation TIMESTAMP DEFAULT now()
    );

    CREATE INDEX idx_remise_lookup ON remise(id_seance, id_type_place, id_categorie_personne_cible, date_creation DESC);

    CREATE TABLE societe (
        id SERIAL PRIMARY KEY,
        libelle TEXT NOT NULL UNIQUE,
        cree_le TIMESTAMP DEFAULT now()
    );

    CREATE TABLE prix_publicite (
        id SERIAL PRIMARY KEY,
        prix NUMERIC(12,2) NOT NULL,
        date_creation TIMESTAMP DEFAULT now()
    );

    CREATE TABLE publicite (
        id SERIAL PRIMARY KEY,
        id_societe INT REFERENCES societe(id) ON DELETE CASCADE,
        cree_le TIMESTAMP DEFAULT now()
    );

    CREATE TABLE publicite_detail (
        id SERIAL PRIMARY KEY,
        id_publicite INT REFERENCES publicite(id) ON DELETE CASCADE,
        id_seance INT REFERENCES seance(id) ON DELETE CASCADE,
        nb_fois INT NOT NULL DEFAULT 1 CHECK (nb_fois > 0),
        UNIQUE (id_publicite, id_seance)
    );

    CREATE INDEX idx_publicite_detail_seance ON publicite_detail(id_seance);
    CREATE INDEX idx_publicite_detail_pub ON publicite_detail(id_publicite);

    CREATE TABLE paiement_publicite (
        id SERIAL PRIMARY KEY,
        id_publicite INT REFERENCES publicite(id) ON DELETE CASCADE,
        montant NUMERIC(12,2) NOT NULL CHECK (montant > 0),
        date_paiement TIMESTAMP DEFAULT now()
    );

    CREATE INDEX idx_paiement_publicite ON paiement_publicite(id_publicite);

    -- ==============================
    -- VENTE DE PRODUITS (POP-CORN, BOISSONS, etc.)
    -- ==============================
    
    CREATE TABLE produit (
        id SERIAL PRIMARY KEY,
        libelle TEXT NOT NULL UNIQUE
    );

    CREATE TABLE prix_produit (
        id SERIAL PRIMARY KEY,
        id_produit INT REFERENCES produit(id) ON DELETE CASCADE,
        prix NUMERIC(12,2) NOT NULL,
        date_prix DATE NOT NULL
    );

    CREATE INDEX idx_prix_produit_lookup ON prix_produit(id_produit, date_prix DESC);

    CREATE TABLE vente (
        id SERIAL PRIMARY KEY,
        date_vente DATE NOT NULL
    );

    CREATE TABLE vente_detail (
        id SERIAL PRIMARY KEY,
        id_vente INT REFERENCES vente(id) ON DELETE CASCADE,
        id_produit INT REFERENCES produit(id) ON DELETE CASCADE,
        quantite INT NOT NULL DEFAULT 1 CHECK (quantite > 0),
        prix_unitaire NUMERIC(12,2) NOT NULL
    );

    CREATE INDEX idx_vente_detail_vente ON vente_detail(id_vente);

    CREATE TABLE paiement_vente (
        id SERIAL PRIMARY KEY,
        id_vente INT REFERENCES vente(id) ON DELETE CASCADE,
        montant_paye NUMERIC(12,2) NOT NULL CHECK (montant_paye > 0),
        date_paiement DATE NOT NULL
    );

    CREATE INDEX idx_paiement_vente ON paiement_vente(id_vente);
