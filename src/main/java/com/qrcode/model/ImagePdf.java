package com.qrcode.model;

import com.qrcode.exception.DonneesInvalidesException;

import java.io.File;

/**
 * Image ajoutée par l'utilisateur dans le PDF.
 * La position (x, y) est mesurée en millimètres depuis le coin
 * <b>en haut à gauche</b> de la page, comme sur une feuille papier.
 */
public class ImagePdf {

    private String chemin;
    private float x;
    private float y;
    private float largeur;
    private float hauteur;

    public ImagePdf() {
    }

    public ImagePdf(String chemin, float x, float y, float largeur, float hauteur) {
        this.chemin = chemin;
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.hauteur = hauteur;
    }

    /**
     * Vérifie que l'image existe et qu'elle tient entièrement dans une page A4.
     *
     * @throws DonneesInvalidesException avec un message expliquant le problème
     */
    public void valider() throws DonneesInvalidesException {
        if (chemin == null || chemin.isBlank()) {
            throw new DonneesInvalidesException("Une image n'a pas de fichier associé.");
        }
        String nom = new File(chemin).getName();
        if (!new File(chemin).isFile()) {
            throw new DonneesInvalidesException("L'image est introuvable : " + chemin);
        }
        if (largeur <= 0 || hauteur <= 0) {
            throw new DonneesInvalidesException("La largeur et la hauteur de l'image \"" + nom + "\" doivent être positives.");
        }
        if (x < 0 || y < 0) {
            throw new DonneesInvalidesException("La position de l'image \"" + nom + "\" ne peut pas être négative.");
        }
        if (x + largeur > Projet.LARGEUR_PAGE_MM || y + hauteur > Projet.HAUTEUR_PAGE_MM) {
            throw new DonneesInvalidesException("L'image \"" + nom + "\" dépasse de la page A4 ("
                    + (int) Projet.LARGEUR_PAGE_MM + " x " + (int) Projet.HAUTEUR_PAGE_MM + " mm).");
        }
    }

    // ----- Getters / Setters -----

    public String getChemin() {
        return chemin;
    }

    public void setChemin(String chemin) {
        this.chemin = chemin;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getLargeur() {
        return largeur;
    }

    public void setLargeur(float largeur) {
        this.largeur = largeur;
    }

    public float getHauteur() {
        return hauteur;
    }

    public void setHauteur(float hauteur) {
        this.hauteur = hauteur;
    }
}
