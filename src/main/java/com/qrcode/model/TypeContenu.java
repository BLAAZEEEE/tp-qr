package com.qrcode.model;

import com.qrcode.exception.DonneesInvalidesException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Type d'information que l'utilisateur veut encoder dans le QR code.
 * Chaque type sait valider et mettre en forme la saisie
 * (ex : un e-mail devient "mailto:adresse" pour être reconnu par les téléphones).
 */
public enum TypeContenu {

    TEXTE("Texte libre"),
    LIEN("Lien (URL)"),
    EMAIL("Adresse e-mail"),
    TELEPHONE("Numéro de téléphone");

    private static final String REGEX_EMAIL = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";
    private static final String REGEX_TELEPHONE = "^\\+?[0-9]{6,15}$";

    private final String libelle;

    TypeContenu(String libelle) {
        this.libelle = libelle;
    }

    /**
     * Vérifie la saisie et la transforme en texte prêt à être encodé.
     *
     * @param saisie texte tapé par l'utilisateur
     * @return le contenu final du QR code
     * @throws DonneesInvalidesException si la saisie est vide ou ne correspond pas au type
     */
    public String formater(String saisie) throws DonneesInvalidesException {
        if (saisie == null || saisie.isBlank()) {
            throw new DonneesInvalidesException("Les données du QR code ne peuvent pas être vides.");
        }
        String s = saisie.trim();

        switch (this) {
            case LIEN:
                if (!s.toLowerCase().startsWith("http://") && !s.toLowerCase().startsWith("https://")) {
                    s = "https://" + s;
                }
                try {
                    URI uri = new URI(s);
                    if (uri.getHost() == null || !uri.getHost().contains(".")) {
                        throw new DonneesInvalidesException("Le lien \"" + saisie.trim() + "\" n'est pas une URL valide.");
                    }
                } catch (URISyntaxException e) {
                    throw new DonneesInvalidesException("Le lien \"" + saisie.trim() + "\" n'est pas une URL valide.");
                }
                return s;

            case EMAIL:
                if (s.toLowerCase().startsWith("mailto:")) {
                    s = s.substring(7);
                }
                if (!s.matches(REGEX_EMAIL)) {
                    throw new DonneesInvalidesException("L'adresse e-mail \"" + s + "\" n'est pas valide.");
                }
                return "mailto:" + s;

            case TELEPHONE:
                String numero = s.replaceAll("[\\s.\\-()]", "");
                if (!numero.matches(REGEX_TELEPHONE)) {
                    throw new DonneesInvalidesException("Le numéro \"" + s + "\" n'est pas valide (6 à 15 chiffres).");
                }
                return "tel:" + numero;

            default:
                return s;
        }
    }

    /** Texte affiché dans la liste déroulante de l'interface. */
    @Override
    public String toString() {
        return libelle;
    }
}
