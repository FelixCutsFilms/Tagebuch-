# Tagebuch

Simple Tagebuch-App (PWA, offline, keine Server). Pro Tag ein Eintrag, gespeichert als `YYYY-MM-DD.md`.

## Benutzung
1. `index.html` über HTTPS hosten (z. B. GitHub Pages: Settings → Pages → Branch wählen).
2. Auf dem Handy öffnen → Browsermenü → „Zum Startbildschirm hinzufügen“.
3. Schreiben (wird automatisch im Browser zwischengespeichert), dann **Als .md speichern**
   → Datei landet im Ordner „Downloads“ (Android) bzw. über **Teilen** → „In Dateien sichern“ (iOS).
4. „Alle als .md exportieren“ erzeugt eine Datei mit allen Einträgen.

## Android-App (Ordner frei wählbar)
Im Ordner `android-app/` liegt eine Capacitor-App, die dieselbe Oberfläche nutzt, aber die
`.md`-Dateien direkt in einen frei gewählten Ordner schreibt (Android Storage Access Framework,
Plugin: `FolderStoragePlugin.java`).

1. GitHub → Tab **Actions** → „Android APK“ → **Run workflow** (läuft auch automatisch bei Änderungen auf `main`).
2. Nach dem Lauf unten bei **Artifacts** `tagebuch-apk` herunterladen, entpacken, `app-debug.apk` aufs Handy kopieren und installieren
   (Android fragt einmal nach „Installation aus unbekannten Quellen“).
3. App öffnen → **📁 Ordner wählen** → Ordner auswählen. Ab dann wird jeder Eintrag automatisch als `JJJJ-MM-TT.md` dort gespeichert.

Lokal bauen: `cd android-app && npm install && npm run sync && cd android && ./gradlew assembleDebug`
(benötigt JDK 17+ und das Android SDK).
