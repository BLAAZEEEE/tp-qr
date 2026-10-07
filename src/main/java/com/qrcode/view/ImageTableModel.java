package com.qrcode.view;

import com.qrcode.model.ImagePdf;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle du tableau des images : une ligne par image, avec sa position et sa taille
 * en millimètres. Les colonnes numériques sont modifiables directement dans le tableau.
 */
public class ImageTableModel extends AbstractTableModel {

    private static final String[] COLONNES = {"Fichier", "X (mm)", "Y (mm)", "Largeur (mm)", "Hauteur (mm)"};

    private final List<ImagePdf> images = new ArrayList<>();

    public void ajouter(ImagePdf image) {
        images.add(image);
        fireTableRowsInserted(images.size() - 1, images.size() - 1);
    }

    public void supprimer(int ligne) {
        images.remove(ligne);
        fireTableRowsDeleted(ligne, ligne);
    }

    public void remplacer(List<ImagePdf> nouvellesImages) {
        images.clear();
        for (ImagePdf image : nouvellesImages) {
            images.add(new ImagePdf(image.getChemin(), image.getX(), image.getY(), image.getLargeur(), image.getHauteur()));
        }
        fireTableDataChanged();
    }

    /** @return une copie des images, pour que le modèle de la vue reste indépendant du projet. */
    public List<ImagePdf> getImages() {
        List<ImagePdf> copie = new ArrayList<>();
        for (ImagePdf i : images) {
            copie.add(new ImagePdf(i.getChemin(), i.getX(), i.getY(), i.getLargeur(), i.getHauteur()));
        }
        return copie;
    }

    @Override
    public int getRowCount() {
        return images.size();
    }

    @Override
    public int getColumnCount() {
        return COLONNES.length;
    }

    @Override
    public String getColumnName(int colonne) {
        return COLONNES[colonne];
    }

    @Override
    public Class<?> getColumnClass(int colonne) {
        return colonne == 0 ? String.class : Float.class;
    }

    @Override
    public boolean isCellEditable(int ligne, int colonne) {
        return colonne > 0;
    }

    @Override
    public Object getValueAt(int ligne, int colonne) {
        ImagePdf image = images.get(ligne);
        switch (colonne) {
            case 0:
                return new File(image.getChemin()).getName();
            case 1:
                return image.getX();
            case 2:
                return image.getY();
            case 3:
                return image.getLargeur();
            default:
                return image.getHauteur();
        }
    }

    @Override
    public void setValueAt(Object valeur, int ligne, int colonne) {
        if (!(valeur instanceof Float)) {
            return;
        }
        float v = (Float) valeur;
        ImagePdf image = images.get(ligne);
        switch (colonne) {
            case 1:
                image.setX(v);
                break;
            case 2:
                image.setY(v);
                break;
            case 3:
                image.setLargeur(v);
                break;
            case 4:
                image.setHauteur(v);
                break;
            default:
                return;
        }
        fireTableCellUpdated(ligne, colonne);
    }
}
