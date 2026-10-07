package com.qrcode;

import com.qrcode.controller.MainController;
import com.qrcode.service.PdfGenerator;
import com.qrcode.service.QrCodeGenerator;
import com.qrcode.service.SauvegardeService;
import com.qrcode.view.MainView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Point d'entrée de l'application : crée le modèle, la vue et le contrôleur (MVC).
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Si le thème du système n'est pas disponible, Swing garde son thème par défaut.
            }

            QrCodeGenerator qrCodeGenerator = new QrCodeGenerator();
            MainView vue = new MainView();
            new MainController(vue, qrCodeGenerator, new PdfGenerator(qrCodeGenerator), new SauvegardeService());
            vue.setVisible(true);
        });
    }
}
