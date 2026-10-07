package com.qrcode.model;

import com.qrcode.exception.DonneesInvalidesException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeContenuTest {

    @Test
    void texteEstConserveTelQuel() throws DonneesInvalidesException {
        assertEquals("Bonjour à tous", TypeContenu.TEXTE.formater("  Bonjour à tous "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void saisieVideEstRefusee(String saisie) {
        for (TypeContenu type : TypeContenu.values()) {
            assertThrows(DonneesInvalidesException.class, () -> type.formater(saisie));
        }
    }

    @Test
    void saisieNullEstRefusee() {
        assertThrows(DonneesInvalidesException.class, () -> TypeContenu.TEXTE.formater(null));
    }

    @Test
    void lienSansProtocoleRecoitHttps() throws DonneesInvalidesException {
        assertEquals("https://www.google.fr", TypeContenu.LIEN.formater("www.google.fr"));
    }

    @Test
    void lienAvecProtocoleEstConserve() throws DonneesInvalidesException {
        assertEquals("http://exemple.com/page?id=3", TypeContenu.LIEN.formater("http://exemple.com/page?id=3"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"pas un lien", "http://", "localhost"})
    void lienInvalideEstRefuse(String saisie) {
        assertThrows(DonneesInvalidesException.class, () -> TypeContenu.LIEN.formater(saisie));
    }

    @Test
    void emailValideDevientMailto() throws DonneesInvalidesException {
        assertEquals("mailto:jean.dupont@exemple.fr", TypeContenu.EMAIL.formater("jean.dupont@exemple.fr"));
        assertEquals("mailto:a@b.com", TypeContenu.EMAIL.formater("mailto:a@b.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jean.dupont", "jean@", "@exemple.fr", "jean@exemple"})
    void emailInvalideEstRefuse(String saisie) {
        assertThrows(DonneesInvalidesException.class, () -> TypeContenu.EMAIL.formater(saisie));
    }

    @Test
    void telephoneEstNettoye() throws DonneesInvalidesException {
        assertEquals("tel:0612345678", TypeContenu.TELEPHONE.formater("06 12 34 56 78"));
        assertEquals("tel:+33612345678", TypeContenu.TELEPHONE.formater("+33 6.12.34.56.78"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12", "06-12-ab-56-78"})
    void telephoneInvalideEstRefuse(String saisie) {
        assertThrows(DonneesInvalidesException.class, () -> TypeContenu.TELEPHONE.formater(saisie));
    }
}
