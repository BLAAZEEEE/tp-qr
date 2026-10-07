package com.qrcode.view;

import com.qrcode.model.Couleurs;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * Bouton affichant une couleur. Un clic ouvre le sélecteur de couleur de Swing.
 */
public class BoutonCouleur extends JButton {

    private Color couleur;
    private final List<Runnable> ecouteurs = new ArrayList<>();

    public BoutonCouleur(String titreDialogue, Color couleurInitiale) {
        this.couleur = couleurInitiale;
        setIcon(new Pastille());
        setText(Couleurs.versHex(couleur));
        setHorizontalAlignment(LEFT);
        addActionListener(e -> {
            Color choix = JColorChooser.showDialog(this, titreDialogue, couleur);
            if (choix != null) {
                setHex(Couleurs.versHex(choix));
                ecouteurs.forEach(Runnable::run);
            }
        });
    }

    public String getHex() {
        return Couleurs.versHex(couleur);
    }

    public void setHex(String hex) {
        this.couleur = Couleurs.depuisHex(hex);
        setText(Couleurs.versHex(couleur));
        repaint();
    }

    /** Appelé quand l'utilisateur choisit une nouvelle couleur. */
    public void onChangement(Runnable ecouteur) {
        ecouteurs.add(ecouteur);
    }

    /** Petit carré coloré affiché à gauche du texte du bouton. */
    private class Pastille implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(couleur);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(Color.GRAY);
            g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
        }

        @Override
        public int getIconWidth() {
            return 28;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }
}
