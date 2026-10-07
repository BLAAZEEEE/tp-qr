package com.qrcode.service;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.qrcode.exception.DonneesInvalidesException;
import com.qrcode.exception.GenerationPdfException;
import com.qrcode.model.AlignementQr;
import com.qrcode.model.ImagePdf;
import com.qrcode.model.Projet;
import com.qrcode.model.TypeContenu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class PdfGeneratorTest {

    @TempDir
    Path dossier;

    private final PdfGenerator generateur = new PdfGenerator();
    private Projet projet;

    @BeforeEach
    void preparer() {
        projet = new Projet();
        projet.setTitre("Fiche de test");
        projet.setDescription("Scannez ce QR code pour accéder au site.");
        projet.setTypeContenu(TypeContenu.LIEN);
        projet.setDonneesQr("www.exemple.fr");
    }

    @Test
    void generationDUnPdfSimple() throws Exception {
        File pdf = dossier.resolve("simple.pdf").toFile();
        generateur.generer(projet, pdf);

        assertTrue(pdf.isFile());
        assertTrue(new String(Files.readAllBytes(pdf.toPath()), 0, 5).startsWith("%PDF"));

        PdfReader lecteur = new PdfReader(Files.readAllBytes(pdf.toPath()));
        try {
            assertEquals(1, lecteur.getNumberOfPages());
            String texte = PdfTextExtractor.getTextFromPage(lecteur, 1);
            assertTrue(texte.contains("Fiche de test"));
            assertTrue(texte.contains("accéder au site"));
            assertTrue(texte.contains("https://www.exemple.fr"));
        } finally {
            lecteur.close();
        }
    }

    @Test
    void contenuMasqueSousLeQrCode() throws Exception {
        projet.setAfficherDonnees(false);
        File pdf = dossier.resolve("sans-legende.pdf").toFile();
        generateur.generer(projet, pdf);
        assertFalse(extraireTexte(pdf).contains("https://www.exemple.fr"));
    }

    @Test
    void stylesEtCouleursPersonnalises() throws Exception {
        projet.getProfil().setPolice("Times-Roman");
        projet.getProfil().setGras(true);
        projet.getProfil().setItalique(true);
        projet.getProfil().setSouligne(true);
        projet.getProfil().setCouleurTitre("#C0392B");
        projet.getProfil().setCouleurFondPage("#FFF8E1");
        projet.getProfil().setCouleurQr("#1A237E");
        projet.setAlignementQr(AlignementQr.DROITE);

        File pdf = dossier.resolve("style.pdf").toFile();
        generateur.generer(projet, pdf);
        assertTrue(extraireTexte(pdf).contains("Fiche de test"));
    }

    @Test
    void policePersonnaliseeTtf() throws Exception {
        File arial = new File("C:/Windows/Fonts/arial.ttf");
        assumeTrue(arial.isFile(), "Police Arial absente : test ignoré");
        projet.getProfil().setPolice(arial.getAbsolutePath());

        File pdf = dossier.resolve("police.pdf").toFile();
        generateur.generer(projet, pdf);
        assertTrue(extraireTexte(pdf).contains("Fiche de test"));
    }

    @Test
    void fichierQuiNEstPasUnePoliceEstRefuse() throws IOException {
        File faussePolice = dossier.resolve("fausse.ttf").toFile();
        Files.writeString(faussePolice.toPath(), "ceci n'est pas une police");
        assertThrows(GenerationPdfException.class, () -> generateur.chargerPolice(faussePolice.getPath()));

        projet.getProfil().setPolice(faussePolice.getPath());
        File pdf = dossier.resolve("police-ko.pdf").toFile();
        assertThrows(GenerationPdfException.class, () -> generateur.generer(projet, pdf));
        assertFalse(pdf.exists(), "Aucun PDF corrompu ne doit rester sur le disque");
    }

    @Test
    void pdfAvecImages() throws Exception {
        File logo = creerImage("logo.png", Color.RED);
        File photo = creerImage("photo.jpg", Color.BLUE);
        projet.getImages().add(new ImagePdf(logo.getPath(), 10, 10, 30, 30));
        projet.getImages().add(new ImagePdf(photo.getPath(), 120, 220, 60, 40));

        File pdf = dossier.resolve("images.pdf").toFile();
        generateur.generer(projet, pdf);

        PdfReader lecteur = new PdfReader(Files.readAllBytes(pdf.toPath()));
        try {
            // QR code + 2 images = 3 images dans les ressources de la page
            int nbImages = lecteur.getPageN(1).getAsDict(com.itextpdf.text.pdf.PdfName.RESOURCES)
                    .getAsDict(com.itextpdf.text.pdf.PdfName.XOBJECT).size();
            assertEquals(3, nbImages);
        } finally {
            lecteur.close();
        }
    }

    @Test
    void fichierQuiNEstPasUneImageEstRefuse() throws IOException {
        File fausseImage = dossier.resolve("image.png").toFile();
        Files.writeString(fausseImage.toPath(), "texte");
        projet.getImages().add(new ImagePdf(fausseImage.getPath(), 10, 10, 30, 30));

        File pdf = dossier.resolve("image-ko.pdf").toFile();
        assertThrows(GenerationPdfException.class, () -> generateur.generer(projet, pdf));
        assertFalse(pdf.exists());
    }

    @Test
    void projetInvalideEstRefuseAvantLaCreationDuFichier() {
        projet.setDonneesQr("");
        File pdf = dossier.resolve("vide.pdf").toFile();
        assertThrows(DonneesInvalidesException.class, () -> generateur.generer(projet, pdf));
        assertFalse(pdf.exists());
    }

    @Test
    void dossierInexistantEstRefuse() {
        File pdf = dossier.resolve("inexistant/sous-dossier/test.pdf").toFile();
        assertThrows(GenerationPdfException.class, () -> generateur.generer(projet, pdf));
    }

    @Test
    void fichierNullEstRefuse() {
        assertThrows(GenerationPdfException.class, () -> generateur.generer(projet, null));
        assertThrows(DonneesInvalidesException.class, () -> generateur.generer(null, new File("x.pdf")));
    }

    @Test
    void contrasteInsuffisantEstSignale() {
        projet.getProfil().setCouleurQr("#FFFFFF");
        File pdf = dossier.resolve("contraste.pdf").toFile();
        GenerationPdfException e = assertThrows(GenerationPdfException.class, () -> generateur.generer(projet, pdf));
        assertTrue(e.getMessage().contains("Contraste"));
    }

    @Test
    void laProgressionVaDe10A100() throws Exception {
        List<Integer> etapes = new ArrayList<>();
        generateur.generer(projet, dossier.resolve("progression.pdf").toFile(), etapes::add);
        assertEquals(10, etapes.get(0));
        assertEquals(100, etapes.get(etapes.size() - 1));
        for (int i = 1; i < etapes.size(); i++) {
            assertTrue(etapes.get(i) >= etapes.get(i - 1), "La progression ne doit jamais reculer");
        }
    }

    private String extraireTexte(File pdf) throws IOException {
        PdfReader lecteur = new PdfReader(Files.readAllBytes(pdf.toPath()));
        try {
            return PdfTextExtractor.getTextFromPage(lecteur, 1);
        } finally {
            lecteur.close();
        }
    }

    private File creerImage(String nom, Color couleur) throws IOException {
        BufferedImage image = new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(couleur);
        g.fillRect(0, 0, 100, 80);
        g.dispose();
        File fichier = dossier.resolve(nom).toFile();
        ImageIO.write(image, nom.endsWith(".png") ? "png" : "jpg", fichier);
        return fichier;
    }
}
