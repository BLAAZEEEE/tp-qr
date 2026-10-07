package com.qrcode.model;

/**
 * Niveau de correction d'erreur du QR code : plus il est élevé, plus le QR code
 * reste lisible s'il est abîmé, mais plus il contient de petits carrés.
 */
public enum NiveauCorrection {

    L("Faible (7 %)"),
    M("Moyen (15 %)"),
    Q("Élevé (25 %)"),
    H("Maximum (30 %)");

    private final String libelle;

    NiveauCorrection(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
