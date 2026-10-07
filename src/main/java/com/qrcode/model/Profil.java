package com.qrcode.model;

import com.qrcode.exception.DonneesInvalidesException;

import java.io.File;

/**
 * Profil de style réutilisable : police, tailles, couleurs et styles du PDF.
 * Un profil peut être sauvegardé puis rechargé pour d'autres projets.
 */
public class Profil {

    /** Polices standard intégrées au format PDF (aucun fichier nécessaire). */
    public static final String[] POLICES_STANDARD = {"Helvetica", "Times-Roman", "Courier"};

    private String nom = "Profil par défaut";
    /** Nom d'une police standard OU chemin vers un fichier .ttf / .otf. */
    private String police = "Helvetica";
    private float tailleTitre = 24;
    private float tailleTexte = 12;
    private boolean gras = false;
    private boolean italique = false;
    private boolean souligne = false;
    private String couleurTitre = "#1F3A93";
    private String couleurTexte = "#222222";
    private String couleurFondPage = "#FFFFFF";
    private String couleurQr = "#000000";
    private String couleurFondQr = "#FFFFFF";

    public Profil() {
    }

    /** @return true si la police est un fichier de police personnalisé (.ttf / .otf). */
    public boolean estPolicePersonnalisee() {
        if (police == null) {
            return false;
        }
        String p = police.toLowerCase();
        return p.endsWith(".ttf") || p.endsWith(".otf");
    }

    /**
     * Vérifie que le profil est utilisable pour générer un PDF.
     *
     * @throws DonneesInvalidesException avec un message expliquant le problème
     */
    public void valider() throws DonneesInvalidesException {
        if (police == null || police.isBlank()) {
            throw new DonneesInvalidesException("Aucune police n'est sélectionnée.");
        }
        if (estPolicePersonnalisee() && !new File(police).isFile()) {
            throw new DonneesInvalidesException("Le fichier de police est introuvable : " + police);
        }
        if (tailleTitre < 8 || tailleTitre > 72) {
            throw new DonneesInvalidesException("La taille du titre doit être comprise entre 8 et 72.");
        }
        if (tailleTexte < 6 || tailleTexte > 48) {
            throw new DonneesInvalidesException("La taille du texte doit être comprise entre 6 et 48.");
        }
        String[][] couleurs = {
                {couleurTitre, "du titre"}, {couleurTexte, "du texte"}, {couleurFondPage, "du fond de page"},
                {couleurQr, "du QR code"}, {couleurFondQr, "du fond du QR code"}
        };
        for (String[] c : couleurs) {
            if (!Couleurs.estValide(c[0])) {
                throw new DonneesInvalidesException("La couleur " + c[1] + " est invalide : " + c[0]);
            }
        }
    }

    // ----- Getters / Setters -----

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPolice() {
        return police;
    }

    public void setPolice(String police) {
        this.police = police;
    }

    public float getTailleTitre() {
        return tailleTitre;
    }

    public void setTailleTitre(float tailleTitre) {
        this.tailleTitre = tailleTitre;
    }

    public float getTailleTexte() {
        return tailleTexte;
    }

    public void setTailleTexte(float tailleTexte) {
        this.tailleTexte = tailleTexte;
    }

    public boolean isGras() {
        return gras;
    }

    public void setGras(boolean gras) {
        this.gras = gras;
    }

    public boolean isItalique() {
        return italique;
    }

    public void setItalique(boolean italique) {
        this.italique = italique;
    }

    public boolean isSouligne() {
        return souligne;
    }

    public void setSouligne(boolean souligne) {
        this.souligne = souligne;
    }

    public String getCouleurTitre() {
        return couleurTitre;
    }

    public void setCouleurTitre(String couleurTitre) {
        this.couleurTitre = couleurTitre;
    }

    public String getCouleurTexte() {
        return couleurTexte;
    }

    public void setCouleurTexte(String couleurTexte) {
        this.couleurTexte = couleurTexte;
    }

    public String getCouleurFondPage() {
        return couleurFondPage;
    }

    public void setCouleurFondPage(String couleurFondPage) {
        this.couleurFondPage = couleurFondPage;
    }

    public String getCouleurQr() {
        return couleurQr;
    }

    public void setCouleurQr(String couleurQr) {
        this.couleurQr = couleurQr;
    }

    public String getCouleurFondQr() {
        return couleurFondQr;
    }

    public void setCouleurFondQr(String couleurFondQr) {
        this.couleurFondQr = couleurFondQr;
    }
}
