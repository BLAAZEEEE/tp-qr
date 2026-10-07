package com.qrcode.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.qrcode.exception.SauvegardeException;
import com.qrcode.model.Profil;
import com.qrcode.model.Projet;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Sauvegarde et chargement des projets (.qrproj) et des profils (.qrprofil)
 * au format JSON grâce à la bibliothèque Gson.
 */
public class SauvegardeService {

    public static final String EXTENSION_PROJET = ".qrproj";
    public static final String EXTENSION_PROFIL = ".qrprofil";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void sauvegarderProjet(Projet projet, File fichier) throws SauvegardeException {
        if (projet == null) {
            throw new SauvegardeException("Aucun projet à sauvegarder.");
        }
        ecrire(projet, fichier);
    }

    public Projet chargerProjet(File fichier) throws SauvegardeException {
        Projet projet = lire(fichier, Projet.class);
        // Un ancien fichier ou un fichier modifié à la main peut avoir des champs manquants.
        if (projet.getProfil() == null) {
            projet.setProfil(new Profil());
        }
        projet.setImages(projet.getImages());
        if (projet.getTypeContenu() == null || projet.getAlignementQr() == null
                || projet.getNiveauCorrection() == null) {
            throw new SauvegardeException("Le fichier \"" + fichier.getName() + "\" contient des valeurs inconnues.");
        }
        return projet;
    }

    public void sauvegarderProfil(Profil profil, File fichier) throws SauvegardeException {
        if (profil == null) {
            throw new SauvegardeException("Aucun profil à sauvegarder.");
        }
        ecrire(profil, fichier);
    }

    public Profil chargerProfil(File fichier) throws SauvegardeException {
        return lire(fichier, Profil.class);
    }

    /**
     * Ajoute l'extension au nom du fichier si l'utilisateur ne l'a pas tapée.
     */
    public static File avecExtension(File fichier, String extension) {
        if (fichier.getName().toLowerCase().endsWith(extension)) {
            return fichier;
        }
        return new File(fichier.getParentFile(), fichier.getName() + extension);
    }

    private void ecrire(Object objet, File fichier) throws SauvegardeException {
        if (fichier == null) {
            throw new SauvegardeException("Aucun fichier de destination n'a été choisi.");
        }
        try (Writer writer = Files.newBufferedWriter(fichier.toPath(), StandardCharsets.UTF_8)) {
            gson.toJson(objet, writer);
        } catch (IOException e) {
            throw new SauvegardeException("Impossible d'enregistrer le fichier \"" + fichier.getName()
                    + "\" : " + e.getMessage(), e);
        }
    }

    private <T> T lire(File fichier, Class<T> type) throws SauvegardeException {
        if (fichier == null || !fichier.isFile()) {
            throw new SauvegardeException("Le fichier est introuvable : " + fichier);
        }
        try (Reader reader = Files.newBufferedReader(fichier.toPath(), StandardCharsets.UTF_8)) {
            T objet = gson.fromJson(reader, type);
            if (objet == null) {
                throw new SauvegardeException("Le fichier \"" + fichier.getName() + "\" est vide.");
            }
            return objet;
        } catch (JsonParseException e) {
            throw new SauvegardeException("Le fichier \"" + fichier.getName()
                    + "\" est corrompu ou n'est pas au bon format.", e);
        } catch (IOException e) {
            throw new SauvegardeException("Impossible de lire le fichier \"" + fichier.getName()
                    + "\" : " + e.getMessage(), e);
        }
    }
}
