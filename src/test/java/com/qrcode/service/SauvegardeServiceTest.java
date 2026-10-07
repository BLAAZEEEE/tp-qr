package com.qrcode.service;

import com.qrcode.exception.SauvegardeException;
import com.qrcode.model.AlignementQr;
import com.qrcode.model.ImagePdf;
import com.qrcode.model.NiveauCorrection;
import com.qrcode.model.Profil;
import com.qrcode.model.Projet;
import com.qrcode.model.TypeContenu;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SauvegardeServiceTest {

    @TempDir
    Path dossier;

    private final SauvegardeService service = new SauvegardeService();

    @Test
    void sauvegardeEtChargementDUnProjet() throws SauvegardeException {
        Projet projet = new Projet();
        projet.setTitre("Mon événement");
        projet.setTypeContenu(TypeContenu.TELEPHONE);
        projet.setDonneesQr("06 12 34 56 78");
        projet.setDescription("Ligne 1\nLigne 2");
        projet.setTailleQrMm(80);
        projet.setAlignementQr(AlignementQr.GAUCHE);
        projet.setNiveauCorrection(NiveauCorrection.H);
        projet.setAfficherDonnees(false);
        projet.getProfil().setCouleurTitre("#FF0000");
        projet.getImages().add(new ImagePdf("C:/images/logo.png", 10, 20, 30, 40));

        File fichier = dossier.resolve("projet.qrproj").toFile();
        service.sauvegarderProjet(projet, fichier);
        Projet charge = service.chargerProjet(fichier);

        assertEquals("Mon événement", charge.getTitre());
        assertEquals(TypeContenu.TELEPHONE, charge.getTypeContenu());
        assertEquals("06 12 34 56 78", charge.getDonneesQr());
        assertEquals("Ligne 1\nLigne 2", charge.getDescription());
        assertEquals(80, charge.getTailleQrMm());
        assertEquals(AlignementQr.GAUCHE, charge.getAlignementQr());
        assertEquals(NiveauCorrection.H, charge.getNiveauCorrection());
        assertEquals(false, charge.isAfficherDonnees());
        assertEquals("#FF0000", charge.getProfil().getCouleurTitre());
        assertEquals(1, charge.getImages().size());
        ImagePdf image = charge.getImages().get(0);
        assertEquals("C:/images/logo.png", image.getChemin());
        assertEquals(10, image.getX());
        assertEquals(40, image.getHauteur());
    }

    @Test
    void sauvegardeEtChargementDUnProfil() throws SauvegardeException {
        Profil profil = new Profil();
        profil.setNom("Entreprise");
        profil.setPolice("Courier");
        profil.setTailleTitre(30);
        profil.setGras(true);
        profil.setSouligne(true);
        profil.setCouleurQr("#0D47A1");

        File fichier = dossier.resolve("profil.qrprofil").toFile();
        service.sauvegarderProfil(profil, fichier);
        Profil charge = service.chargerProfil(fichier);

        assertEquals("Entreprise", charge.getNom());
        assertEquals("Courier", charge.getPolice());
        assertEquals(30, charge.getTailleTitre());
        assertTrue(charge.isGras());
        assertTrue(charge.isSouligne());
        assertEquals("#0D47A1", charge.getCouleurQr());
    }

    @Test
    void leFichierEstUnJsonLisible() throws Exception {
        File fichier = dossier.resolve("profil.qrprofil").toFile();
        service.sauvegarderProfil(new Profil(), fichier);
        String contenu = Files.readString(fichier.toPath());
        assertTrue(contenu.contains("\"police\": \"Helvetica\""));
    }

    @Test
    void champsManquantsRemplacesParDesValeursParDefaut() throws Exception {
        File fichier = dossier.resolve("ancien.qrproj").toFile();
        Files.writeString(fichier.toPath(), "{ \"titre\": \"Ancien projet\", \"donneesQr\": \"abc\" }");
        Projet projet = service.chargerProjet(fichier);
        assertEquals("Ancien projet", projet.getTitre());
        assertNotNull(projet.getProfil());
        assertNotNull(projet.getImages());
    }

    @Test
    void fichierIntrouvable() {
        File fichier = dossier.resolve("absent.qrproj").toFile();
        assertThrows(SauvegardeException.class, () -> service.chargerProjet(fichier));
        assertThrows(SauvegardeException.class, () -> service.chargerProfil(fichier));
    }

    @Test
    void fichierCorrompu() throws IOException {
        File fichier = dossier.resolve("corrompu.qrproj").toFile();
        Files.writeString(fichier.toPath(), "{ ceci n'est pas du JSON valide");
        SauvegardeException e = assertThrows(SauvegardeException.class, () -> service.chargerProjet(fichier));
        assertTrue(e.getMessage().contains("corrompu"));
    }

    @Test
    void fichierVide() throws IOException {
        File fichier = dossier.resolve("vide.qrprofil").toFile();
        Files.writeString(fichier.toPath(), "");
        assertThrows(SauvegardeException.class, () -> service.chargerProfil(fichier));
    }

    @Test
    void valeurInconnueDansLeFichier() throws IOException {
        File fichier = dossier.resolve("inconnu.qrproj").toFile();
        Files.writeString(fichier.toPath(), "{ \"typeContenu\": \"VIDEO\" }");
        assertThrows(SauvegardeException.class, () -> service.chargerProjet(fichier));
    }

    @Test
    void dossierDeDestinationInexistant() {
        File fichier = dossier.resolve("pas/de/dossier/projet.qrproj").toFile();
        assertThrows(SauvegardeException.class, () -> service.sauvegarderProjet(new Projet(), fichier));
    }

    @Test
    void objetsNullRefuses() {
        File fichier = dossier.resolve("x.qrproj").toFile();
        assertThrows(SauvegardeException.class, () -> service.sauvegarderProjet(null, fichier));
        assertThrows(SauvegardeException.class, () -> service.sauvegarderProfil(null, fichier));
        assertThrows(SauvegardeException.class, () -> service.sauvegarderProjet(new Projet(), null));
    }

    @Test
    void ajoutAutomatiqueDeLExtension() {
        File sans = new File("dossier/mon-projet");
        File avec = new File("dossier/mon-projet.qrproj");
        assertEquals(new File("dossier/mon-projet.qrproj"), SauvegardeService.avecExtension(sans, ".qrproj"));
        assertSame(avec, SauvegardeService.avecExtension(avec, ".qrproj"));
    }
}
