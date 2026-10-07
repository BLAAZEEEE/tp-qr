package com.qrcode.model;

import com.qrcode.exception.DonneesInvalidesException;

import java.util.ArrayList;
import java.util.List;

/**
 * Projet complet de l'utilisateur : contenu du QR code, texte du PDF,
 * images ajoutées et profil de style. C'est cet objet qui est sauvegardé
 * dans les fichiers ".qrproj".
 */
public class Projet {

    public static final float LARGEUR_PAGE_MM = 210;
    public static final float HAUTEUR_PAGE_MM = 297;
    public static final float TAILLE_QR_MIN_MM = 20;
    public static final float TAILLE_QR_MAX_MM = 180;

    private String titre = "Mon QR code";
    private TypeContenu typeContenu = TypeContenu.LIEN;
    private String donneesQr = "";
    private String description = "";
    private float tailleQrMm = 60;
    private AlignementQr alignementQr = AlignementQr.CENTRE;
    private NiveauCorrection niveauCorrection = NiveauCorrection.M;
    private boolean afficherDonnees = true;
    private Profil profil = new Profil();
    private List<ImagePdf> images = new ArrayList<>();

    public Projet() {
    }

    /**
     * @return le texte réellement encodé dans le QR code (ex : "mailto:...").
     * @throws DonneesInvalidesException si la saisie ne correspond pas au type choisi
     */
    public String getContenuFormate() throws DonneesInvalidesException {
        if (typeContenu == null) {
            throw new DonneesInvalidesException("Le type de contenu n'est pas défini.");
        }
        return typeContenu.formater(donneesQr);
    }

    /**
     * Vérifie l'ensemble du projet avant la génération du PDF.
     *
     * @throws DonneesInvalidesException au premier problème rencontré
     */
    public void valider() throws DonneesInvalidesException {
        getContenuFormate();
        if (tailleQrMm < TAILLE_QR_MIN_MM || tailleQrMm > TAILLE_QR_MAX_MM) {
            throw new DonneesInvalidesException("La taille du QR code doit être comprise entre "
                    + (int) TAILLE_QR_MIN_MM + " et " + (int) TAILLE_QR_MAX_MM + " mm.");
        }
        if (alignementQr == null || niveauCorrection == null) {
            throw new DonneesInvalidesException("L'alignement ou le niveau de correction n'est pas défini.");
        }
        if (profil == null) {
            throw new DonneesInvalidesException("Aucun profil de style n'est défini.");
        }
        profil.valider();
        for (ImagePdf image : images) {
            image.valider();
        }
    }

    // ----- Getters / Setters -----

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public TypeContenu getTypeContenu() {
        return typeContenu;
    }

    public void setTypeContenu(TypeContenu typeContenu) {
        this.typeContenu = typeContenu;
    }

    public String getDonneesQr() {
        return donneesQr;
    }

    public void setDonneesQr(String donneesQr) {
        this.donneesQr = donneesQr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getTailleQrMm() {
        return tailleQrMm;
    }

    public void setTailleQrMm(float tailleQrMm) {
        this.tailleQrMm = tailleQrMm;
    }

    public AlignementQr getAlignementQr() {
        return alignementQr;
    }

    public void setAlignementQr(AlignementQr alignementQr) {
        this.alignementQr = alignementQr;
    }

    public NiveauCorrection getNiveauCorrection() {
        return niveauCorrection;
    }

    public void setNiveauCorrection(NiveauCorrection niveauCorrection) {
        this.niveauCorrection = niveauCorrection;
    }

    public boolean isAfficherDonnees() {
        return afficherDonnees;
    }

    public void setAfficherDonnees(boolean afficherDonnees) {
        this.afficherDonnees = afficherDonnees;
    }

    public Profil getProfil() {
        return profil;
    }

    public void setProfil(Profil profil) {
        this.profil = profil;
    }

    public List<ImagePdf> getImages() {
        return images;
    }

    public void setImages(List<ImagePdf> images) {
        this.images = images == null ? new ArrayList<>() : images;
    }
}
