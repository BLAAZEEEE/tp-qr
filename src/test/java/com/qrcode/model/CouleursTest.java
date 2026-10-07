package com.qrcode.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouleursTest {

    @Test
    void conversionAllerRetour() {
        Color c = new Color(31, 58, 147);
        assertEquals("#1F3A93", Couleurs.versHex(c));
        assertEquals(c, Couleurs.depuisHex("#1F3A93"));
        assertEquals(c, Couleurs.depuisHex("#1f3a93"));
    }

    @Test
    void codeInvalideEstRefuse() {
        assertThrows(IllegalArgumentException.class, () -> Couleurs.depuisHex("rouge"));
        assertThrows(IllegalArgumentException.class, () -> Couleurs.depuisHex("#12345"));
        assertThrows(IllegalArgumentException.class, () -> Couleurs.depuisHex(null));
        assertFalse(Couleurs.estValide("#GGGGGG"));
        assertTrue(Couleurs.estValide("#00ff00"));
    }

    @Test
    void luminanceNoirEtBlanc() {
        assertEquals(0.0, Couleurs.luminance(Color.BLACK), 0.001);
        assertEquals(1.0, Couleurs.luminance(Color.WHITE), 0.001);
    }
}
