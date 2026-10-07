package com.qrcode.service;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.qrcode.exception.DonneesInvalidesException;
import com.qrcode.exception.GenerationQrException;
import com.qrcode.model.Couleurs;
import com.qrcode.model.NiveauCorrection;
import com.qrcode.model.Projet;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Génère l'image d'un QR code à partir d'un texte, grâce à la bibliothèque ZXing.
 */
public class QrCodeGenerator {

    public static final int TAILLE_MIN_PX = 50;
    public static final int TAILLE_MAX_PX = 2000;
    /** Écart de luminance minimum entre le QR code et son fond pour rester lisible. */
    public static final double CONTRASTE_MIN = 0.4;

    /**
     * Génère un QR code.
     *
     * @param donnees     texte à encoder
     * @param taillePx    taille de l'image en pixels (carrée)
     * @param couleurQr   couleur des modules (carrés) du QR code
     * @param couleurFond couleur du fond
     * @param niveau      niveau de correction d'erreur
     * @return l'image du QR code
     * @throws GenerationQrException si les paramètres sont incorrects ou si le texte est trop long
     */
    public BufferedImage generer(String donnees, int taillePx, Color couleurQr, Color couleurFond,
                                 NiveauCorrection niveau) throws GenerationQrException {
        if (donnees == null || donnees.isEmpty()) {
            throw new GenerationQrException("Impossible de générer un QR code vide.");
        }
        if (taillePx < TAILLE_MIN_PX || taillePx > TAILLE_MAX_PX) {
            throw new GenerationQrException("La taille du QR code doit être comprise entre "
                    + TAILLE_MIN_PX + " et " + TAILLE_MAX_PX + " pixels.");
        }
        verifierContraste(couleurQr, couleurFond);

        Map<EncodeHintType, Object> options = new EnumMap<>(EncodeHintType.class);
        options.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        options.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.valueOf(niveau.name()));
        options.put(EncodeHintType.MARGIN, 2);

        try {
            BitMatrix matrice = new QRCodeWriter().encode(donnees,
                    com.google.zxing.BarcodeFormat.QR_CODE, taillePx, taillePx, options);
            MatrixToImageConfig config = new MatrixToImageConfig(couleurQr.getRGB(), couleurFond.getRGB());
            return MatrixToImageWriter.toBufferedImage(matrice, config);
        } catch (WriterException | IllegalArgumentException e) {
            throw new GenerationQrException("Le texte est trop long pour tenir dans un QR code ("
                    + donnees.length() + " caractères). Réduisez-le ou baissez le niveau de correction.", e);
        }
    }

    /**
     * Génère le QR code d'un projet en utilisant ses couleurs et son niveau de correction.
     */
    public BufferedImage generer(Projet projet, int taillePx) throws GenerationQrException, DonneesInvalidesException {
        String contenu = projet.getContenuFormate();
        Color couleurQr;
        Color couleurFond;
        try {
            couleurQr = Couleurs.depuisHex(projet.getProfil().getCouleurQr());
            couleurFond = Couleurs.depuisHex(projet.getProfil().getCouleurFondQr());
        } catch (IllegalArgumentException e) {
            throw new DonneesInvalidesException("Les couleurs du QR code sont invalides.");
        }
        return generer(contenu, taillePx, couleurQr, couleurFond, projet.getNiveauCorrection());
    }

    /**
     * Les lecteurs de QR code ont besoin de modules foncés sur un fond clair
     * et d'un contraste suffisant.
     */
    private void verifierContraste(Color couleurQr, Color couleurFond) throws GenerationQrException {
        if (couleurQr == null || couleurFond == null) {
            throw new GenerationQrException("Les couleurs du QR code ne sont pas définies.");
        }
        double lumQr = Couleurs.luminance(couleurQr);
        double lumFond = Couleurs.luminance(couleurFond);
        if (lumFond - lumQr < CONTRASTE_MIN) {
            throw new GenerationQrException("Contraste insuffisant : le QR code doit être nettement plus foncé "
                    + "que son fond, sinon il ne pourra pas être scanné.");
        }
    }
}
