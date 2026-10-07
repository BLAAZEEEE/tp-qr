package com.qrcode.model;

/**
 * Position horizontale du QR code dans la page PDF.
 */
public enum AlignementQr {

    GAUCHE("Gauche"),
    CENTRE("Centre"),
    DROITE("Droite");

    private final String libelle;

    AlignementQr(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
