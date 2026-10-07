package com.qrcode.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.qrcode.exception.GenerationQrException;
import com.qrcode.model.NiveauCorrection;
import com.qrcode.model.Projet;
import com.qrcode.model.TypeContenu;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QrCodeGeneratorTest {

    private final QrCodeGenerator generateur = new QrCodeGenerator();

    /** Relit un QR code avec ZXing pour vérifier son contenu. */
    static String lireQrCode(BufferedImage image) throws Exception {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        Result resultat = new QRCodeReader().decode(bitmap, Map.of(DecodeHintType.CHARACTER_SET, "UTF-8"));
        return resultat.getText();
    }

    @Test
    void imageALaBonneTaille() throws GenerationQrException {
        BufferedImage image = generateur.generer("test", 300, Color.BLACK, Color.WHITE, NiveauCorrection.M);
        assertEquals(300, image.getWidth());
        assertEquals(300, image.getHeight());
    }

    @Test
    void leQrCodeContientLeTexte() throws Exception {
        String texte = "Bonjour, ceci est un test avec des accents : éàç !";
        BufferedImage image = generateur.generer(texte, 400, Color.BLACK, Color.WHITE, NiveauCorrection.H);
        assertEquals(texte, lireQrCode(image));
    }

    @Test
    void lesCouleursSontAppliquees() throws Exception {
        Color bleu = new Color(0, 0, 139);
        Color jaune = new Color(255, 255, 200);
        BufferedImage image = generateur.generer("couleurs", 200, bleu, jaune, NiveauCorrection.M);
        // Le coin supérieur gauche est dans la marge : couleur de fond
        assertEquals(jaune.getRGB(), image.getRGB(0, 0));
        // Le QR code coloré reste lisible
        assertEquals("couleurs", lireQrCode(image));
    }

    @Test
    void generationDepuisUnProjet() throws Exception {
        Projet projet = new Projet();
        projet.setTypeContenu(TypeContenu.EMAIL);
        projet.setDonneesQr("contact@exemple.fr");
        BufferedImage image = generateur.generer(projet, 300);
        assertEquals("mailto:contact@exemple.fr", lireQrCode(image));
    }

    @Test
    void texteVideEstRefuse() {
        assertThrows(GenerationQrException.class,
                () -> generateur.generer("", 200, Color.BLACK, Color.WHITE, NiveauCorrection.M));
        assertThrows(GenerationQrException.class,
                () -> generateur.generer(null, 200, Color.BLACK, Color.WHITE, NiveauCorrection.M));
    }

    @Test
    void tailleInvalideEstRefusee() {
        assertThrows(GenerationQrException.class,
                () -> generateur.generer("test", 10, Color.BLACK, Color.WHITE, NiveauCorrection.M));
        assertThrows(GenerationQrException.class,
                () -> generateur.generer("test", 5000, Color.BLACK, Color.WHITE, NiveauCorrection.M));
    }

    @Test
    void texteTropLongEstRefuse() {
        String texte = "a".repeat(5000);
        GenerationQrException e = assertThrows(GenerationQrException.class,
                () -> generateur.generer(texte, 500, Color.BLACK, Color.WHITE, NiveauCorrection.H));
        assertEquals(true, e.getMessage().contains("trop long"));
    }

    @Test
    void contrasteInsuffisantEstRefuse() {
        // Même couleur
        assertThrows(GenerationQrException.class,
                () -> generateur.generer("test", 200, Color.WHITE, Color.WHITE, NiveauCorrection.M));
        // Couleurs proches
        assertThrows(GenerationQrException.class,
                () -> generateur.generer("test", 200, new Color(220, 220, 220), Color.WHITE, NiveauCorrection.M));
        // QR code clair sur fond foncé (non lisible par la plupart des téléphones)
        assertThrows(GenerationQrException.class,
                () -> generateur.generer("test", 200, Color.WHITE, Color.BLACK, NiveauCorrection.M));
    }

    @Test
    void couleursNullSontRefusees() {
        assertThrows(GenerationQrException.class,
                () -> generateur.generer("test", 200, null, Color.WHITE, NiveauCorrection.M));
    }

    @Test
    void imageBlancheNeContientPasDeQrCode() {
        BufferedImage blanche = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        assertThrows(NotFoundException.class, () -> lireQrCode(blanche));
    }
}
