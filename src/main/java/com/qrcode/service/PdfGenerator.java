package com.qrcode.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.qrcode.exception.DonneesInvalidesException;
import com.qrcode.exception.GenerationPdfException;
import com.qrcode.exception.GenerationQrException;
import com.qrcode.model.AlignementQr;
import com.qrcode.model.Couleurs;
import com.qrcode.model.ImagePdf;
import com.qrcode.model.Profil;
import com.qrcode.model.Projet;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.function.IntConsumer;

/**
 * Crée le fichier PDF d'un projet avec la bibliothèque iText :
 * titre, description, QR code et images, en appliquant le profil de style.
 */
public class PdfGenerator {

    /** Résolution de l'image du QR code insérée dans le PDF (pour un rendu net à l'impression). */
    public static final int RESOLUTION_QR_PX = 800;

    private final QrCodeGenerator qrCodeGenerator;

    public PdfGenerator() {
        this(new QrCodeGenerator());
    }

    public PdfGenerator(QrCodeGenerator qrCodeGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
    }

    /**
     * Génère le PDF sans suivi de progression.
     */
    public void generer(Projet projet, File fichier) throws GenerationPdfException, DonneesInvalidesException {
        generer(projet, fichier, p -> { });
    }

    /**
     * Génère le PDF.
     *
     * @param projet      projet à exporter
     * @param fichier     fichier PDF de destination
     * @param progression reçoit l'avancement de 0 à 100 (pour la barre de progression)
     * @throws DonneesInvalidesException si le projet contient des erreurs de saisie
     * @throws GenerationPdfException    si le fichier ne peut pas être créé
     */
    public void generer(Projet projet, File fichier, IntConsumer progression)
            throws GenerationPdfException, DonneesInvalidesException {
        if (projet == null) {
            throw new DonneesInvalidesException("Aucun projet à générer.");
        }
        if (fichier == null) {
            throw new GenerationPdfException("Aucun fichier de destination n'a été choisi.");
        }
        projet.valider();
        progression.accept(10);

        File dossier = fichier.getAbsoluteFile().getParentFile();
        if (dossier == null || !dossier.isDirectory()) {
            throw new GenerationPdfException("Le dossier de destination n'existe pas : " + dossier);
        }

        Profil profil = projet.getProfil();

        // Le QR code est généré avant d'ouvrir le fichier pour ne pas laisser de PDF à moitié écrit.
        Image imageQr;
        try {
            BufferedImage qr = qrCodeGenerator.generer(projet, RESOLUTION_QR_PX);
            imageQr = Image.getInstance(versPng(qr));
        } catch (GenerationQrException e) {
            throw new GenerationPdfException(e.getMessage(), e);
        } catch (IOException | DocumentException e) {
            throw new GenerationPdfException("Impossible d'insérer le QR code dans le PDF.", e);
        }
        progression.accept(30);

        Rectangle page = new Rectangle(PageSize.A4);
        page.setBackgroundColor(versBaseColor(profil.getCouleurFondPage()));
        Document document = new Document(page, 50, 50, 50, 50);

        boolean succes = false;
        try (OutputStream sortie = new FileOutputStream(fichier)) {
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.addTitle(projet.getTitre());
            document.addCreator("Générateur de QR Code PDF");
            document.open();

            BaseFont policePerso = profil.estPolicePersonnalisee() ? chargerPolice(profil.getPolice()) : null;
            Font fontTitre = creerFont(profil, policePerso, profil.getTailleTitre(), profil.getCouleurTitre());
            Font fontTexte = creerFont(profil, policePerso, profil.getTailleTexte(), profil.getCouleurTexte());
            Font fontLegende = creerFont(profil, policePerso, Math.max(8, profil.getTailleTexte() - 2),
                    profil.getCouleurTexte());

            // 1. Titre
            if (projet.getTitre() != null && !projet.getTitre().isBlank()) {
                Paragraph titre = new Paragraph(projet.getTitre(), fontTitre);
                titre.setAlignment(Element.ALIGN_CENTER);
                titre.setSpacingAfter(20);
                document.add(titre);
            }
            progression.accept(45);

            // 2. Description
            if (projet.getDescription() != null && !projet.getDescription().isBlank()) {
                Paragraph description = new Paragraph(projet.getDescription(), fontTexte);
                description.setAlignment(Element.ALIGN_JUSTIFIED);
                description.setSpacingAfter(20);
                document.add(description);
            }
            progression.accept(60);

            // 3. QR code
            float tailleQrPt = Utilities.millimetersToPoints(projet.getTailleQrMm());
            imageQr.scaleAbsolute(tailleQrPt, tailleQrPt);
            imageQr.setAlignment(versAlignementImage(projet.getAlignementQr()));
            document.add(imageQr);

            if (projet.isAfficherDonnees()) {
                Paragraph legende = new Paragraph(projet.getContenuFormate(), fontLegende);
                legende.setAlignment(versAlignementTexte(projet.getAlignementQr()));
                document.add(legende);
            }
            progression.accept(75);

            // 4. Images positionnées librement sur la première page
            PdfContentByte calque = writer.getDirectContent();
            int i = 0;
            for (ImagePdf imagePdf : projet.getImages()) {
                calque.addImage(creerImagePositionnee(imagePdf, page));
                i++;
                progression.accept(75 + 20 * i / projet.getImages().size());
            }

            document.close();
            succes = true;
            progression.accept(100);
        } catch (DocumentException e) {
            throw new GenerationPdfException("Erreur lors de la construction du PDF : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new GenerationPdfException("Impossible d'écrire le fichier " + fichier.getName()
                    + ". Vérifiez qu'il n'est pas ouvert dans un autre programme.", e);
        } finally {
            if (!succes) {
                // On ne laisse pas un fichier PDF corrompu sur le disque.
                fichier.delete();
            }
        }
    }

    /**
     * Vérifie qu'un fichier de police peut être utilisé dans un PDF.
     *
     * @throws GenerationPdfException si le fichier n'est pas une police valide
     */
    public BaseFont chargerPolice(String chemin) throws GenerationPdfException {
        if (chemin == null || !new File(chemin).isFile()) {
            throw new GenerationPdfException("Le fichier de police est introuvable : " + chemin);
        }
        try {
            // Le fichier est lu en mémoire : sinon iText le garde verrouillé en cas d'erreur.
            byte[] donnees = Files.readAllBytes(new File(chemin).toPath());
            return BaseFont.createFont(chemin, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, donnees, null);
        } catch (DocumentException | IOException | RuntimeException e) {
            throw new GenerationPdfException("Le fichier \"" + new File(chemin).getName()
                    + "\" n'est pas une police TrueType/OpenType valide.", e);
        }
    }

    private Font creerFont(Profil profil, BaseFont policePerso, float taille, String couleurHex) {
        int style = Font.NORMAL;
        if (profil.isGras()) {
            style |= Font.BOLD;
        }
        if (profil.isItalique()) {
            style |= Font.ITALIC;
        }
        if (profil.isSouligne()) {
            style |= Font.UNDERLINE;
        }
        BaseColor couleur = versBaseColor(couleurHex);
        if (policePerso != null) {
            return new Font(policePerso, taille, style, couleur);
        }
        Font.FontFamily famille;
        switch (profil.getPolice()) {
            case "Times-Roman":
                famille = Font.FontFamily.TIMES_ROMAN;
                break;
            case "Courier":
                famille = Font.FontFamily.COURIER;
                break;
            default:
                famille = Font.FontFamily.HELVETICA;
        }
        return new Font(famille, taille, style, couleur);
    }

    /**
     * Convertit la position en mm (depuis le haut à gauche) en points iText
     * (depuis le bas à gauche).
     */
    private Image creerImagePositionnee(ImagePdf imagePdf, Rectangle page) throws GenerationPdfException {
        try {
            Image image = Image.getInstance(imagePdf.getChemin());
            float largeur = Utilities.millimetersToPoints(imagePdf.getLargeur());
            float hauteur = Utilities.millimetersToPoints(imagePdf.getHauteur());
            float x = Utilities.millimetersToPoints(imagePdf.getX());
            float y = page.getHeight() - Utilities.millimetersToPoints(imagePdf.getY()) - hauteur;
            image.scaleAbsolute(largeur, hauteur);
            image.setAbsolutePosition(x, y);
            return image;
        } catch (IOException | DocumentException e) {
            throw new GenerationPdfException("Le fichier \"" + new File(imagePdf.getChemin()).getName()
                    + "\" n'est pas une image lisible (formats acceptés : PNG, JPG, GIF, BMP).", e);
        }
    }

    private static byte[] versPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream flux = new ByteArrayOutputStream();
        ImageIO.write(image, "png", flux);
        return flux.toByteArray();
    }

    private static BaseColor versBaseColor(String hex) {
        Color c = Couleurs.depuisHex(hex);
        return new BaseColor(c.getRed(), c.getGreen(), c.getBlue());
    }

    private static int versAlignementImage(AlignementQr alignement) {
        switch (alignement) {
            case GAUCHE:
                return Image.ALIGN_LEFT;
            case DROITE:
                return Image.ALIGN_RIGHT;
            default:
                return Image.ALIGN_CENTER;
        }
    }

    private static int versAlignementTexte(AlignementQr alignement) {
        switch (alignement) {
            case GAUCHE:
                return Element.ALIGN_LEFT;
            case DROITE:
                return Element.ALIGN_RIGHT;
            default:
                return Element.ALIGN_CENTER;
        }
    }
}
