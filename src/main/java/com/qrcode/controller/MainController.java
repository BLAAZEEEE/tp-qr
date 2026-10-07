package com.qrcode.controller;

import com.qrcode.exception.QrAppException;
import com.qrcode.model.ImagePdf;
import com.qrcode.model.Profil;
import com.qrcode.model.Projet;
import com.qrcode.service.PdfGenerator;
import com.qrcode.service.QrCodeGenerator;
import com.qrcode.service.SauvegardeService;
import com.qrcode.view.MainView;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * CONTRÔLEUR : fait le lien entre la vue (fenêtre Swing) et le modèle
 * (projet, profil et services de génération / sauvegarde).
 * Chaque action de l'utilisateur est traitée ici et toutes les erreurs
 * sont transformées en messages compréhensibles.
 */
public class MainController {

    /** Largeur par défaut d'une image ajoutée (mm). */
    private static final float LARGEUR_IMAGE_DEFAUT = 50;

    private final MainView vue;
    private final QrCodeGenerator qrCodeGenerator;
    private final PdfGenerator pdfGenerator;
    private final SauvegardeService sauvegardeService;

    /** Attend 300 ms après la dernière frappe avant de recalculer l'aperçu. */
    private final Timer minuterieApercu;

    public MainController(MainView vue, QrCodeGenerator qrCodeGenerator, PdfGenerator pdfGenerator,
                          SauvegardeService sauvegardeService) {
        this.vue = vue;
        this.qrCodeGenerator = qrCodeGenerator;
        this.pdfGenerator = pdfGenerator;
        this.sauvegardeService = sauvegardeService;

        minuterieApercu = new Timer(300, e -> actualiserApercu(false));
        minuterieApercu.setRepeats(false);

        vue.onGenererPdf(e -> genererPdf());
        vue.onApercu(e -> actualiserApercu(true));
        vue.onNouveauProjet(e -> nouveauProjet());
        vue.onOuvrirProjet(e -> ouvrirProjet());
        vue.onEnregistrerProjet(e -> enregistrerProjet());
        vue.onChargerProfil(e -> chargerProfil());
        vue.onSauverProfil(e -> sauverProfil());
        vue.onPolicePersonnalisee(e -> choisirPolicePersonnalisee());
        vue.onAjouterImage(e -> ajouterImage());
        vue.onSupprimerImage(e -> supprimerImage());
        vue.onModificationQr(minuterieApercu::restart);

        actualiserApercu(false);
    }

    // ----- Aperçu -----

    /**
     * Recalcule l'aperçu du QR code.
     *
     * @param afficherPopup true pour afficher les erreurs dans une boîte de dialogue
     *                      (clic sur le bouton), false pour les afficher dans l'aperçu (frappe au clavier)
     */
    void actualiserApercu(boolean afficherPopup) {
        Projet projet = vue.lireProjet();
        if (!afficherPopup && (projet.getDonneesQr() == null || projet.getDonneesQr().isBlank())) {
            vue.afficherErreurApercu("Saisissez le contenu du QR code dans l'onglet \"Contenu\".");
            return;
        }
        try {
            BufferedImage image = qrCodeGenerator.generer(projet, MainView.TAILLE_APERCU_PX);
            vue.afficherApercu(image);
            vue.setStatut("Contenu encodé : " + projet.getContenuFormate());
        } catch (QrAppException e) {
            vue.afficherErreurApercu(e.getMessage());
            if (afficherPopup) {
                vue.afficherErreur("Aperçu impossible", e.getMessage());
            }
        }
    }

    // ----- Génération du PDF -----

    private void genererPdf() {
        vue.terminerEdition();
        Projet projet = vue.lireProjet();
        try {
            projet.valider();
        } catch (QrAppException e) {
            vue.afficherErreur("Données invalides", e.getMessage());
            return;
        }

        File fichier = demanderFichierAEnregistrer("Enregistrer le PDF", "Document PDF", ".pdf");
        if (fichier == null) {
            return;
        }

        vue.setOccupe(true);
        vue.setProgression(0);
        vue.setStatut("Génération du PDF en cours...");

        // La génération se fait dans un thread séparé pour ne pas figer la fenêtre.
        SwingWorker<Void, Void> tache = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                pdfGenerator.generer(projet, fichier, this::setProgress);
                return null;
            }

            @Override
            protected void done() {
                vue.setOccupe(false);
                try {
                    get();
                    vue.setProgression(100);
                    vue.setStatut("PDF créé : " + fichier.getAbsolutePath());
                    if (vue.confirmer("PDF généré", "Le PDF a été créé avec succès :\n"
                            + fichier.getAbsolutePath() + "\n\nVoulez-vous l'ouvrir ?")) {
                        ouvrirFichier(fichier);
                    }
                } catch (ExecutionException e) {
                    vue.setProgression(0);
                    vue.setStatut("Échec de la génération.");
                    Throwable cause = e.getCause();
                    String message = cause instanceof QrAppException
                            ? cause.getMessage()
                            : "Erreur inattendue : " + cause;
                    vue.afficherErreur("Erreur de génération", message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        tache.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                vue.setProgression((Integer) evt.getNewValue());
            }
        });
        tache.execute();
    }

    private void ouvrirFichier(File fichier) {
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new IOException("Ouverture automatique non supportée sur ce système.");
            }
            Desktop.getDesktop().open(fichier);
        } catch (IOException | UnsupportedOperationException e) {
            vue.afficherErreur("Ouverture impossible", "Impossible d'ouvrir le PDF : " + e.getMessage());
        }
    }

    // ----- Projets -----

    private void nouveauProjet() {
        if (vue.confirmer("Nouveau projet", "Les modifications non enregistrées seront perdues. Continuer ?")) {
            vue.afficherProjet(new Projet());
            vue.setProgression(0);
            vue.setStatut("Nouveau projet.");
            actualiserApercu(false);
        }
    }

    private void enregistrerProjet() {
        vue.terminerEdition();
        File fichier = demanderFichierAEnregistrer("Enregistrer le projet", "Projet QR Code",
                SauvegardeService.EXTENSION_PROJET);
        if (fichier == null) {
            return;
        }
        try {
            sauvegardeService.sauvegarderProjet(vue.lireProjet(), fichier);
            vue.setStatut("Projet enregistré : " + fichier.getName());
        } catch (QrAppException e) {
            vue.afficherErreur("Erreur d'enregistrement", e.getMessage());
        }
    }

    private void ouvrirProjet() {
        File fichier = vue.choisirFichierAOuvrir("Ouvrir un projet", "Projet QR Code", "qrproj");
        if (fichier == null) {
            return;
        }
        try {
            Projet projet = sauvegardeService.chargerProjet(fichier);
            vue.afficherProjet(projet);
            vue.setStatut("Projet chargé : " + fichier.getName());
            actualiserApercu(false);
            signalerFichiersManquants(projet);
        } catch (QrAppException e) {
            vue.afficherErreur("Erreur de chargement", e.getMessage());
        }
    }

    /** Prévient l'utilisateur si des images ou une police du projet ont été déplacées. */
    private void signalerFichiersManquants(Projet projet) {
        StringBuilder manquants = new StringBuilder();
        for (ImagePdf image : projet.getImages()) {
            if (!new File(image.getChemin()).isFile()) {
                manquants.append("\n - ").append(image.getChemin());
            }
        }
        if (projet.getProfil().estPolicePersonnalisee() && !new File(projet.getProfil().getPolice()).isFile()) {
            manquants.append("\n - ").append(projet.getProfil().getPolice());
        }
        if (manquants.length() > 0) {
            vue.afficherErreur("Fichiers manquants",
                    "Certains fichiers utilisés par ce projet sont introuvables :" + manquants
                            + "\n\nRemplacez-les avant de générer le PDF.");
        }
    }

    // ----- Profils -----

    private void sauverProfil() {
        File fichier = demanderFichierAEnregistrer("Sauvegarder le profil", "Profil de style",
                SauvegardeService.EXTENSION_PROFIL);
        if (fichier == null) {
            return;
        }
        try {
            sauvegardeService.sauvegarderProfil(vue.lireProfil(), fichier);
            vue.setStatut("Profil sauvegardé : " + fichier.getName());
        } catch (QrAppException e) {
            vue.afficherErreur("Erreur d'enregistrement", e.getMessage());
        }
    }

    private void chargerProfil() {
        File fichier = vue.choisirFichierAOuvrir("Charger un profil", "Profil de style", "qrprofil");
        if (fichier == null) {
            return;
        }
        try {
            Profil profil = sauvegardeService.chargerProfil(fichier);
            profil.valider();
            vue.afficherProfil(profil);
            vue.setStatut("Profil chargé : " + profil.getNom());
            actualiserApercu(false);
        } catch (QrAppException e) {
            vue.afficherErreur("Profil invalide", e.getMessage());
        }
    }

    private void choisirPolicePersonnalisee() {
        File fichier = vue.choisirFichierAOuvrir("Choisir une police", "Polices TrueType / OpenType", "ttf", "otf");
        if (fichier == null) {
            return;
        }
        try {
            pdfGenerator.chargerPolice(fichier.getAbsolutePath());
            vue.selectionnerPolice(fichier.getAbsolutePath());
            vue.setStatut("Police ajoutée : " + fichier.getName());
        } catch (QrAppException e) {
            vue.afficherErreur("Police invalide", e.getMessage());
        }
    }

    // ----- Images -----

    private void ajouterImage() {
        File fichier = vue.choisirFichierAOuvrir("Ajouter une image", "Images", "png", "jpg", "jpeg", "gif", "bmp");
        if (fichier == null) {
            return;
        }
        try {
            BufferedImage image = ImageIO.read(fichier);
            if (image == null) {
                vue.afficherErreur("Image invalide", "Le fichier \"" + fichier.getName() + "\" n'est pas une image lisible.");
                return;
            }
            // On garde les proportions de l'image d'origine.
            float largeur = LARGEUR_IMAGE_DEFAUT;
            float hauteur = largeur * image.getHeight() / image.getWidth();
            if (hauteur > Projet.HAUTEUR_PAGE_MM - 20) {
                hauteur = Projet.HAUTEUR_PAGE_MM - 20;
                largeur = hauteur * image.getWidth() / image.getHeight();
            }
            float y = Math.max(10, Projet.HAUTEUR_PAGE_MM - hauteur - 20);
            vue.ajouterImage(new ImagePdf(fichier.getAbsolutePath(), 20, arrondir(y),
                    arrondir(largeur), arrondir(hauteur)));
            vue.setStatut("Image ajoutée : " + fichier.getName() + " (modifiable dans le tableau)");
        } catch (IOException e) {
            vue.afficherErreur("Image invalide", "Impossible de lire \"" + fichier.getName() + "\" : " + e.getMessage());
        }
    }

    private void supprimerImage() {
        int ligne = vue.getImageSelectionnee();
        if (ligne < 0) {
            vue.afficherErreur("Aucune sélection", "Sélectionnez d'abord une image dans le tableau.");
            return;
        }
        vue.supprimerImage(ligne);
    }

    // ----- Utilitaires -----

    /** Demande un fichier de destination, ajoute l'extension et confirme l'écrasement. */
    private File demanderFichierAEnregistrer(String titre, String description, String extension) {
        File fichier = vue.choisirFichierAEnregistrer(titre, description, extension);
        if (fichier == null) {
            return null;
        }
        fichier = SauvegardeService.avecExtension(fichier, extension);
        if (fichier.exists() && !vue.confirmer("Fichier existant",
                "Le fichier \"" + fichier.getName() + "\" existe déjà. Le remplacer ?")) {
            return null;
        }
        return fichier;
    }

    private static float arrondir(float valeur) {
        return Math.round(valeur * 10) / 10f;
    }
}
