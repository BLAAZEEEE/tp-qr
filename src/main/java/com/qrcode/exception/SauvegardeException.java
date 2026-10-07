package com.qrcode.exception;

/**
 * Levée quand la sauvegarde ou le chargement d'un projet / profil échoue.
 */
public class SauvegardeException extends QrAppException {

    public SauvegardeException(String message) {
        super(message);
    }

    public SauvegardeException(String message, Throwable cause) {
        super(message, cause);
    }
}
