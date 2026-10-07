package com.qrcode.model;

import com.qrcode.exception.DonneesInvalidesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjetTest {

    @TempDir
    Path dossier;

    private Projet projet;

    @BeforeEach
    void preparer() {
        projet = new Projet();
        projet.setTypeContenu(TypeContenu.LIEN);
        projet.setDonneesQr("www.exemple.fr");
    }

    @Test
    void projetCorrectEstValide() {
        assertDoesNotThrow(() -> projet.valider());
    }

    @Test
    void contenuVideEstRefuse() {
        projet.setDonneesQr("");
        assertThrows(DonneesInvalidesException.class, () -> projet.valider());
    }

    @Test
    void tailleQrHorsLimitesEstRefusee() {
        projet.setTailleQrMm(5);
        assertThrows(DonneesInvalidesException.class, () -> projet.valider());
        projet.setTailleQrMm(500);
        assertThrows(DonneesInvalidesException.class, () -> projet.valider());
    }

    @Test
    void profilAvecCouleurInvalideEstRefuse() {
        projet.getProfil().setCouleurTitre("bleu");
        DonneesInvalidesException e = assertThrows(DonneesInvalidesException.class, () -> projet.valider());
        assertTrue(e.getMessage().contains("titre"));
    }

    @Test
    void policePersonnaliseeIntrouvableEstRefusee() {
        projet.getProfil().setPolice("C:/n/existe/pas/police.ttf");
        assertTrue(projet.getProfil().estPolicePersonnalisee());
        assertThrows(DonneesInvalidesException.class, () -> projet.valider());
    }

    @Test
    void tailleDeTexteHorsLimitesEstRefusee() {
        projet.getProfil().setTailleTexte(100);
        assertThrows(DonneesInvalidesException.class, () -> projet.valider());
    }

    @Test
    void imageIntrouvableEstRefusee() {
        projet.getImages().add(new ImagePdf("introuvable.png", 10, 10, 50, 50));
        assertThrows(DonneesInvalidesException.class, () -> projet.valider());
    }

    @Test
    void imageQuiDepasseDeLaPageEstRefusee() throws IOException {
        File image = creerFichier("logo.png");
        projet.getImages().add(new ImagePdf(image.getPath(), 180, 10, 50, 50));
        DonneesInvalidesException e = assertThrows(DonneesInvalidesException.class, () -> projet.valider());
        assertTrue(e.getMessage().contains("dépasse"));
    }

    @Test
    void imageAvecTailleNulleOuPositionNegativeEstRefusee() throws IOException {
        File image = creerFichier("logo.png");
        ImagePdf imagePdf = new ImagePdf(image.getPath(), 10, 10, 0, 50);
        projet.getImages().add(imagePdf);
        assertThrows(DonneesInvalidesException.class, () -> projet.valider());

        imagePdf.setLargeur(50);
        imagePdf.setX(-5);
        assertThrows(DonneesInvalidesException.class, () -> projet.valider());
    }

    @Test
    void imageBienPlaceeEstAcceptee() throws IOException {
        File image = creerFichier("logo.png");
        projet.getImages().add(new ImagePdf(image.getPath(), 10, 200, 50, 50));
        assertDoesNotThrow(() -> projet.valider());
    }

    private File creerFichier(String nom) throws IOException {
        return Files.createFile(dossier.resolve(nom)).toFile();
    }
}
