package com.qrcode.exception;

/**
 * Levée quand la génération du QR code échoue (texte trop long, couleurs illisibles...).
 */
public class GenerationQrException extends QrAppException {

    public GenerationQrException(String message) {
        super(message);
    }

    public GenerationQrException(String message, Throwable cause) {
        super(message, cause);
    }
}
