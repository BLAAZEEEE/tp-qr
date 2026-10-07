package com.qrcode.exception;

/**
 * Levée quand les informations saisies par l'utilisateur sont incorrectes
 * (champ vide, URL invalide, image hors de la page...)..
 */
public class DonneesInvalidesException extends QrAppException {

    public DonneesInvalidesException(String message) {
        super(message);
    }
}
