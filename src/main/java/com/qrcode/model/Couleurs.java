package com.qrcode.model;

import java.awt.Color;

/**
 * Conversion entre les couleurs Java et leur code hexadécimal ("#RRGGBB").
 * Les couleurs sont stockées en hexadécimal dans le modèle pour que les
 * fichiers de sauvegarde JSON restent lisibles.
 */
public final class Couleurs {

    private Couleurs() {
    }

    /**
     * @param hex code de la forme "#RRGGBB"
     * @return la couleur correspondante
     * @throws IllegalArgumentException si le code est invalide
     */
    public static Color depuisHex(String hex) {
        if (hex == null || !hex.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Code couleur invalide : " + hex);
        }
        return Color.decode(hex);
    }

    /** @return le code "#RRGGBB" de la couleur. */
    public static String versHex(Color couleur) {
        return String.format("#%02X%02X%02X", couleur.getRed(), couleur.getGreen(), couleur.getBlue());
    }

    /** @return true si le code est un code couleur valide. */
    public static boolean estValide(String hex) {
        return hex != null && hex.matches("^#[0-9A-Fa-f]{6}$");
    }

    /**
     * Luminance relative (0 = noir, 1 = blanc), utilisée pour vérifier
     * que le QR code reste lisible.
     */
    public static double luminance(Color c) {
        return (0.2126 * canal(c.getRed()) + 0.7152 * canal(c.getGreen()) + 0.0722 * canal(c.getBlue()));
    }

    private static double canal(int valeur) {
        double v = valeur / 255.0;
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
}
