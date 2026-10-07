package com.qrcode.exception;

/**
 * Exception de base de l'application.
 * Toutes les erreurs « métier » héritent de cette classe, ce qui permet au
 * contrôleur de les attraper en un seul bloc et d'afficher un message clair.
 */
public class QrAppException extends Exception {

    public QrAppException(String message) {
        super(message);
    }

    public QrAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
