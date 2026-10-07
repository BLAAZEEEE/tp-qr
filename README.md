# Générateur de QR Code PDF

Application Java Swing (architecture MVC) qui génère des QR codes et les intègre dans des fichiers PDF personnalisables.

## Lancer dans IntelliJ IDEA

1. **File > Open...** et sélectionner le dossier `qr code` (celui qui contient `pom.xml`), puis **Trust Project**.
2. **File > Project Structure > Project > SDK** : JDK 17 ou plus récent.
3. Clic droit sur `src/main/java/com/qrcode/Main.java` > **Run 'Main.main()'**.

Tests : clic droit sur `src/test/java` > **Run 'All Tests'**.

## Structure (MVC)

```
src/main/java/com/qrcode/
├── Main.java                  Point d'entrée
├── model/                     MODÈLE : données
├── service/                   MODÈLE : traitements (QR code, PDF, sauvegarde)
├── view/                      VUE : fenêtre Swing
├── controller/                CONTRÔLEUR
└── exception/                 Exceptions de l'application
```
