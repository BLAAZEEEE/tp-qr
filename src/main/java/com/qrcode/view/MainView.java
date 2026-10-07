package com.qrcode.view;

import com.qrcode.model.AlignementQr;
import com.qrcode.model.Couleurs;
import com.qrcode.model.ImagePdf;
import com.qrcode.model.NiveauCorrection;
import com.qrcode.model.Profil;
import com.qrcode.model.Projet;
import com.qrcode.model.TypeContenu;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * VUE : fenêtre principale de l'application.
 * Elle affiche le formulaire et transmet les actions de l'utilisateur au contrôleur.
 * Elle ne contient aucune logique métier (pas de génération de QR code ni de PDF).
 */
public class MainView extends JFrame {

    public static final int TAILLE_APERCU_PX = 260;

    // ----- Onglet "Contenu" -----
    private final JTextField champTitre = new JTextField(25);
    private final JComboBox<TypeContenu> comboType = new JComboBox<>(TypeContenu.values());
    private final JTextArea zoneDonnees = new JTextArea(3, 25);
    private final JTextArea zoneDescription = new JTextArea(7, 25);
    private final JSpinner spinnerTailleQr = new JSpinner(new SpinnerNumberModel(60, 20, 180, 5));
    private final JComboBox<AlignementQr> comboAlignement = new JComboBox<>(AlignementQr.values());
    private final JComboBox<NiveauCorrection> comboNiveau = new JComboBox<>(NiveauCorrection.values());
    private final JCheckBox caseAfficherDonnees = new JCheckBox("Afficher le contenu sous le QR code");

    // ----- Onglet "Style" -----
    private final JTextField champNomProfil = new JTextField(20);
    private final JComboBox<String> comboPolice = new JComboBox<>(Profil.POLICES_STANDARD);
    private final JButton boutonPolicePerso = new JButton("Police personnalisée (.ttf / .otf)...");
    private final JSpinner spinnerTailleTitre = new JSpinner(new SpinnerNumberModel(24, 8, 72, 1));
    private final JSpinner spinnerTailleTexte = new JSpinner(new SpinnerNumberModel(12, 6, 48, 1));
    private final JCheckBox caseGras = new JCheckBox("Gras");
    private final JCheckBox caseItalique = new JCheckBox("Italique");
    private final JCheckBox caseSouligne = new JCheckBox("Souligné");
    private final BoutonCouleur boutonCouleurTitre = new BoutonCouleur("Couleur du titre", Color.BLUE);
    private final BoutonCouleur boutonCouleurTexte = new BoutonCouleur("Couleur du texte", Color.BLACK);
    private final BoutonCouleur boutonCouleurFondPage = new BoutonCouleur("Couleur du fond de page", Color.WHITE);
    private final BoutonCouleur boutonCouleurQr = new BoutonCouleur("Couleur du QR code", Color.BLACK);
    private final BoutonCouleur boutonCouleurFondQr = new BoutonCouleur("Couleur du fond du QR code", Color.WHITE);
    private final JButton boutonSauverProfil = new JButton("Sauvegarder le profil...");
    private final JButton boutonChargerProfil = new JButton("Charger un profil...");

    // ----- Onglet "Images" -----
    private final ImageTableModel modeleImages = new ImageTableModel();
    private final JTable tableImages = new JTable(modeleImages);
    private final JButton boutonAjouterImage = new JButton("Ajouter une image...");
    private final JButton boutonSupprimerImage = new JButton("Supprimer");

    // ----- Aperçu -----
    private final JLabel labelApercu = new JLabel("", SwingConstants.CENTER);
    private final JButton boutonApercu = new JButton("Actualiser l'aperçu");

    // ----- Barre du bas -----
    private final JButton boutonGenererPdf = new JButton("Générer le PDF");
    private final JProgressBar barreProgression = new JProgressBar(0, 100);
    private final JLabel labelStatut = new JLabel("Prêt.");

    // ----- Menus -----
    private final JMenuItem menuNouveau = new JMenuItem("Nouveau projet");
    private final JMenuItem menuOuvrir = new JMenuItem("Ouvrir un projet...");
    private final JMenuItem menuEnregistrer = new JMenuItem("Enregistrer le projet...");
    private final JMenuItem menuGenerer = new JMenuItem("Générer le PDF...");
    private final JMenuItem menuQuitter = new JMenuItem("Quitter");
    private final JMenuItem menuChargerProfil = new JMenuItem("Charger un profil...");
    private final JMenuItem menuSauverProfil = new JMenuItem("Sauvegarder le profil...");
    private final JMenuItem menuAPropos = new JMenuItem("À propos");

    private File dernierDossier = new File(System.getProperty("user.home"));

    public MainView() {
        super("Générateur de QR Code PDF");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(creerMenus());

        JTabbedPane onglets = new JTabbedPane();
        onglets.addTab("Contenu", creerOngletContenu());
        onglets.addTab("Style", creerOngletStyle());
        onglets.addTab("Images", creerOngletImages());

        JPanel centre = new JPanel(new BorderLayout(10, 0));
        centre.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        centre.add(onglets, BorderLayout.CENTER);
        centre.add(creerPanneauApercu(), BorderLayout.EAST);

        add(centre, BorderLayout.CENTER);
        add(creerBarreBas(), BorderLayout.SOUTH);

        afficherProjet(new Projet());
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    // =====================================================================
    //  Construction de l'interface
    // =====================================================================

    private JMenuBar creerMenus() {
        JMenuBar barre = new JMenuBar();

        JMenu fichier = new JMenu("Fichier");
        menuNouveau.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        menuOuvrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        menuEnregistrer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        menuGenerer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        fichier.add(menuNouveau);
        fichier.add(menuOuvrir);
        fichier.add(menuEnregistrer);
        fichier.addSeparator();
        fichier.add(menuGenerer);
        fichier.addSeparator();
        fichier.add(menuQuitter);
        menuQuitter.addActionListener(e -> dispose());

        JMenu profil = new JMenu("Profil");
        profil.add(menuChargerProfil);
        profil.add(menuSauverProfil);

        JMenu aide = new JMenu("Aide");
        aide.add(menuAPropos);
        menuAPropos.addActionListener(e -> afficherInfo("À propos",
                "Générateur de QR Code PDF - version 2.0\n\n"
                        + "Projet Java Swing (architecture MVC)\n"
                        + "Bibliothèques : iText (PDF), ZXing (QR code), Gson (sauvegarde)."));

        barre.add(fichier);
        barre.add(profil);
        barre.add(aide);
        return barre;
    }

    private JPanel creerOngletContenu() {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        zoneDonnees.setLineWrap(true);
        zoneDonnees.setWrapStyleWord(true);
        zoneDescription.setLineWrap(true);
        zoneDescription.setWrapStyleWord(true);
        zoneDonnees.setFont(champTitre.getFont());
        zoneDescription.setFont(champTitre.getFont());

        int ligne = 0;
        ajouterLigne(panneau, ligne++, "Titre du PDF :", champTitre);
        ajouterLigne(panneau, ligne++, "Type de contenu :", comboType);
        ajouterLigne(panneau, ligne++, "Contenu du QR code :", new JScrollPane(zoneDonnees));
        ajouterLigne(panneau, ligne++, "Description (texte du PDF) :", new JScrollPane(zoneDescription));
        ajouterLigne(panneau, ligne++, "Taille du QR code (mm) :", spinnerTailleQr);
        ajouterLigne(panneau, ligne++, "Position du QR code :", comboAlignement);
        ajouterLigne(panneau, ligne++, "Correction d'erreur :", comboNiveau);
        ajouterLigne(panneau, ligne++, "", caseAfficherDonnees);
        ajouterEspace(panneau, ligne);
        return panneau;
    }

    private JPanel creerOngletStyle() {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Affiche seulement le nom du fichier pour les polices personnalisées
        comboPolice.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> liste, Object valeur, int index,
                                                          boolean selection, boolean focus) {
                String texte = valeur == null ? "" : valeur.toString();
                if (texte.toLowerCase().endsWith(".ttf") || texte.toLowerCase().endsWith(".otf")) {
                    texte = new File(texte).getName() + " (personnalisée)";
                }
                return super.getListCellRendererComponent(liste, texte, index, selection, focus);
            }
        });

        JPanel styles = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        styles.add(caseGras);
        styles.add(caseItalique);
        styles.add(caseSouligne);

        JPanel boutonsProfil = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        boutonsProfil.add(boutonChargerProfil);
        boutonsProfil.add(javax.swing.Box.createHorizontalStrut(8));
        boutonsProfil.add(boutonSauverProfil);

        int ligne = 0;
        ajouterLigne(panneau, ligne++, "Nom du profil :", champNomProfil);
        ajouterLigne(panneau, ligne++, "Police :", comboPolice);
        ajouterLigne(panneau, ligne++, "", boutonPolicePerso);
        ajouterLigne(panneau, ligne++, "Taille du titre :", spinnerTailleTitre);
        ajouterLigne(panneau, ligne++, "Taille du texte :", spinnerTailleTexte);
        ajouterLigne(panneau, ligne++, "Style :", styles);
        ajouterLigne(panneau, ligne++, "Couleur du titre :", boutonCouleurTitre);
        ajouterLigne(panneau, ligne++, "Couleur du texte :", boutonCouleurTexte);
        ajouterLigne(panneau, ligne++, "Fond de la page :", boutonCouleurFondPage);
        ajouterLigne(panneau, ligne++, "Couleur du QR code :", boutonCouleurQr);
        ajouterLigne(panneau, ligne++, "Fond du QR code :", boutonCouleurFondQr);
        ajouterLigne(panneau, ligne++, "", boutonsProfil);
        ajouterEspace(panneau, ligne);
        return panneau;
    }

    private JPanel creerOngletImages() {
        JPanel panneau = new JPanel(new BorderLayout(0, 8));
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel aide = new JLabel("<html>Les images sont placées sur la première page. La position est mesurée "
                + "en millimètres depuis le coin <b>en haut à gauche</b> (page A4 : 210 x 297 mm).<br>"
                + "Double-cliquez sur une valeur pour la modifier.</html>");
        tableImages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableImages.setFillsViewportHeight(true);
        tableImages.getColumnModel().getColumn(0).setPreferredWidth(160);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        boutons.add(boutonAjouterImage);
        boutons.add(javax.swing.Box.createHorizontalStrut(8));
        boutons.add(boutonSupprimerImage);

        panneau.add(aide, BorderLayout.NORTH);
        panneau.add(new JScrollPane(tableImages), BorderLayout.CENTER);
        panneau.add(boutons, BorderLayout.SOUTH);
        return panneau;
    }

    private JPanel creerPanneauApercu() {
        JPanel panneau = new JPanel(new BorderLayout(0, 8));
        panneau.setBorder(BorderFactory.createTitledBorder("Aperçu du QR code"));
        labelApercu.setPreferredSize(new Dimension(TAILLE_APERCU_PX + 20, TAILLE_APERCU_PX + 20));
        labelApercu.setVerticalTextPosition(SwingConstants.BOTTOM);
        labelApercu.setHorizontalTextPosition(SwingConstants.CENTER);
        panneau.add(labelApercu, BorderLayout.CENTER);
        panneau.add(boutonApercu, BorderLayout.SOUTH);
        return panneau;
    }

    private JPanel creerBarreBas() {
        JPanel barre = new JPanel(new BorderLayout(10, 0));
        barre.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boutonGenererPdf.setFont(boutonGenererPdf.getFont().deriveFont(Font.BOLD, 14f));
        barreProgression.setStringPainted(true);
        barreProgression.setPreferredSize(new Dimension(200, 24));

        JPanel gauche = new JPanel(new BorderLayout(10, 0));
        gauche.add(barreProgression, BorderLayout.WEST);
        gauche.add(labelStatut, BorderLayout.CENTER);

        barre.add(gauche, BorderLayout.CENTER);
        barre.add(boutonGenererPdf, BorderLayout.EAST);
        return barre;
    }

    private void ajouterLigne(JPanel panneau, int ligne, String libelle, JComponent composant) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.gridy = ligne;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        panneau.add(new JLabel(libelle), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panneau.add(composant, c);
    }

    private void ajouterEspace(JPanel panneau, int ligne) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = ligne;
        c.weighty = 1;
        panneau.add(new JPanel(), c);
    }

    // =====================================================================
    //  Échanges avec le modèle (utilisés par le contrôleur)
    // =====================================================================

    /** Construit un projet à partir de ce qui est saisi dans le formulaire. */
    public Projet lireProjet() {
        Projet projet = new Projet();
        projet.setTitre(champTitre.getText());
        projet.setTypeContenu((TypeContenu) comboType.getSelectedItem());
        projet.setDonneesQr(zoneDonnees.getText());
        projet.setDescription(zoneDescription.getText());
        projet.setTailleQrMm(((Number) spinnerTailleQr.getValue()).floatValue());
        projet.setAlignementQr((AlignementQr) comboAlignement.getSelectedItem());
        projet.setNiveauCorrection((NiveauCorrection) comboNiveau.getSelectedItem());
        projet.setAfficherDonnees(caseAfficherDonnees.isSelected());
        projet.setProfil(lireProfil());
        projet.setImages(modeleImages.getImages());
        return projet;
    }

    /** Remplit le formulaire avec les valeurs d'un projet. */
    public void afficherProjet(Projet projet) {
        champTitre.setText(projet.getTitre());
        comboType.setSelectedItem(projet.getTypeContenu());
        zoneDonnees.setText(projet.getDonneesQr());
        zoneDescription.setText(projet.getDescription());
        spinnerTailleQr.setValue(Math.round(projet.getTailleQrMm()));
        comboAlignement.setSelectedItem(projet.getAlignementQr());
        comboNiveau.setSelectedItem(projet.getNiveauCorrection());
        caseAfficherDonnees.setSelected(projet.isAfficherDonnees());
        afficherProfil(projet.getProfil());
        modeleImages.remplacer(projet.getImages());
    }

    /** Construit un profil à partir de l'onglet "Style". */
    public Profil lireProfil() {
        Profil profil = new Profil();
        profil.setNom(champNomProfil.getText());
        profil.setPolice((String) comboPolice.getSelectedItem());
        profil.setTailleTitre(((Number) spinnerTailleTitre.getValue()).floatValue());
        profil.setTailleTexte(((Number) spinnerTailleTexte.getValue()).floatValue());
        profil.setGras(caseGras.isSelected());
        profil.setItalique(caseItalique.isSelected());
        profil.setSouligne(caseSouligne.isSelected());
        profil.setCouleurTitre(boutonCouleurTitre.getHex());
        profil.setCouleurTexte(boutonCouleurTexte.getHex());
        profil.setCouleurFondPage(boutonCouleurFondPage.getHex());
        profil.setCouleurQr(boutonCouleurQr.getHex());
        profil.setCouleurFondQr(boutonCouleurFondQr.getHex());
        return profil;
    }

    /** Remplit l'onglet "Style" avec un profil. Les couleurs invalides sont ignorées. */
    public void afficherProfil(Profil profil) {
        champNomProfil.setText(profil.getNom());
        selectionnerPolice(profil.getPolice());
        spinnerTailleTitre.setValue(Math.round(profil.getTailleTitre()));
        spinnerTailleTexte.setValue(Math.round(profil.getTailleTexte()));
        caseGras.setSelected(profil.isGras());
        caseItalique.setSelected(profil.isItalique());
        caseSouligne.setSelected(profil.isSouligne());
        appliquerCouleur(boutonCouleurTitre, profil.getCouleurTitre());
        appliquerCouleur(boutonCouleurTexte, profil.getCouleurTexte());
        appliquerCouleur(boutonCouleurFondPage, profil.getCouleurFondPage());
        appliquerCouleur(boutonCouleurQr, profil.getCouleurQr());
        appliquerCouleur(boutonCouleurFondQr, profil.getCouleurFondQr());
    }

    private void appliquerCouleur(BoutonCouleur bouton, String hex) {
        if (Couleurs.estValide(hex)) {
            bouton.setHex(hex);
        }
    }

    /** Sélectionne une police ; une police personnalisée est ajoutée à la liste si besoin. */
    public void selectionnerPolice(String police) {
        if (police == null) {
            return;
        }
        for (int i = 0; i < comboPolice.getItemCount(); i++) {
            if (comboPolice.getItemAt(i).equals(police)) {
                comboPolice.setSelectedIndex(i);
                return;
            }
        }
        comboPolice.addItem(police);
        comboPolice.setSelectedItem(police);
    }

    public void ajouterImage(ImagePdf image) {
        modeleImages.ajouter(image);
    }

    /** @return l'index de l'image sélectionnée dans le tableau, ou -1. */
    public int getImageSelectionnee() {
        if (tableImages.isEditing()) {
            tableImages.getCellEditor().stopCellEditing();
        }
        return tableImages.getSelectedRow();
    }

    public void supprimerImage(int ligne) {
        modeleImages.supprimer(ligne);
    }

    /** Valide la cellule en cours de modification avant de lire le tableau. */
    public void terminerEdition() {
        if (tableImages.isEditing()) {
            tableImages.getCellEditor().stopCellEditing();
        }
    }

    // =====================================================================
    //  Affichage
    // =====================================================================

    public void afficherApercu(BufferedImage image) {
        labelApercu.setIcon(new ImageIcon(image));
        labelApercu.setText("");
    }

    public void afficherErreurApercu(String message) {
        labelApercu.setIcon(null);
        labelApercu.setText("<html><div style='text-align:center;width:200px;color:#B00020'>" + message + "</div></html>");
    }

    public void setProgression(int valeur) {
        barreProgression.setValue(valeur);
    }

    public void setStatut(String texte) {
        labelStatut.setText(texte);
    }

    /** Désactive les boutons pendant la génération du PDF. */
    public void setOccupe(boolean occupe) {
        boutonGenererPdf.setEnabled(!occupe);
        menuGenerer.setEnabled(!occupe);
        menuOuvrir.setEnabled(!occupe);
        menuNouveau.setEnabled(!occupe);
        setCursor(occupe ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR) : null);
    }

    public void afficherErreur(String titre, String message) {
        JOptionPane.showMessageDialog(this, message, titre, JOptionPane.ERROR_MESSAGE);
    }

    public void afficherInfo(String titre, String message) {
        JOptionPane.showMessageDialog(this, message, titre, JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean confirmer(String titre, String message) {
        return JOptionPane.showConfirmDialog(this, message, titre, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /**
     * Ouvre une boîte de dialogue "Enregistrer sous".
     *
     * @return le fichier choisi, ou null si l'utilisateur annule
     */
    public File choisirFichierAEnregistrer(String titre, String description, String extension) {
        JFileChooser selecteur = new JFileChooser(dernierDossier);
        selecteur.setDialogTitle(titre);
        selecteur.setFileFilter(new FileNameExtensionFilter(description, extension.replace(".", "")));
        if (selecteur.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        dernierDossier = selecteur.getCurrentDirectory();
        return selecteur.getSelectedFile();
    }

    /**
     * Ouvre une boîte de dialogue "Ouvrir".
     *
     * @return le fichier choisi, ou null si l'utilisateur annule
     */
    public File choisirFichierAOuvrir(String titre, String description, String... extensions) {
        JFileChooser selecteur = new JFileChooser(dernierDossier);
        selecteur.setDialogTitle(titre);
        selecteur.setFileFilter(new FileNameExtensionFilter(description, extensions));
        if (selecteur.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        dernierDossier = selecteur.getCurrentDirectory();
        return selecteur.getSelectedFile();
    }

    // =====================================================================
    //  Abonnement du contrôleur aux actions de l'utilisateur
    // =====================================================================

    public void onGenererPdf(ActionListener l) {
        boutonGenererPdf.addActionListener(l);
        menuGenerer.addActionListener(l);
    }

    public void onApercu(ActionListener l) {
        boutonApercu.addActionListener(l);
    }

    public void onNouveauProjet(ActionListener l) {
        menuNouveau.addActionListener(l);
    }

    public void onOuvrirProjet(ActionListener l) {
        menuOuvrir.addActionListener(l);
    }

    public void onEnregistrerProjet(ActionListener l) {
        menuEnregistrer.addActionListener(l);
    }

    public void onChargerProfil(ActionListener l) {
        boutonChargerProfil.addActionListener(l);
        menuChargerProfil.addActionListener(l);
    }

    public void onSauverProfil(ActionListener l) {
        boutonSauverProfil.addActionListener(l);
        menuSauverProfil.addActionListener(l);
    }

    public void onPolicePersonnalisee(ActionListener l) {
        boutonPolicePerso.addActionListener(l);
    }

    public void onAjouterImage(ActionListener l) {
        boutonAjouterImage.addActionListener(l);
    }

    public void onSupprimerImage(ActionListener l) {
        boutonSupprimerImage.addActionListener(l);
    }

    /** Appelé à chaque modification qui change l'apparence du QR code (pour l'aperçu en direct). */
    public void onModificationQr(Runnable r) {
        zoneDonnees.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                r.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                r.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                r.run();
            }
        });
        comboType.addActionListener(e -> r.run());
        comboNiveau.addActionListener(e -> r.run());
        boutonCouleurQr.onChangement(r);
        boutonCouleurFondQr.onChangement(r);
    }
}
