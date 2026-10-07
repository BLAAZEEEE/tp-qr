package com.qrcode.exception;

/**
 * Levée quand la création du fichier PDF échoue (fichier verrouillé, police illisible...).
 */
public class GenerationPdfException extends QrAppException {

    public GenerationPdfException(String message) {
        super(message);
    }

    public GenerationPdfException(String message, Throwable cause) {
        super(message, cause);
    }
}
