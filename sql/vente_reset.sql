-- ======================================
-- RESET DES TABLES DE VENTES
-- ======================================
-- Ce script supprime toutes les données des tables liées aux ventes
-- Ordre de suppression important pour respecter les contraintes FK

-- Suppression des paiements de vente
DELETE FROM paiement_vente;

-- Suppression des détails de vente
DELETE FROM vente_detail;

-- Suppression des ventes
DELETE FROM vente;

-- Reset des séquences
ALTER SEQUENCE paiement_vente_id_seq RESTART WITH 1;
ALTER SEQUENCE vente_detail_id_seq RESTART WITH 1;
ALTER SEQUENCE vente_id_seq RESTART WITH 1;

-- ======================================
-- FIN DU RESET VENTES
-- ======================================
