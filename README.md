# RPGMaker GitSync

Desktop-Anwendung zum einfachen Synchronisieren von RPG Maker-Projekten mit GitHub.  
Die App übernimmt Klonen/Initialisieren von Repositories, Staging relevanter Ressourcen
und zeigt den aktuellen Sync-Status in einer kompakten Oberfläche an.

## Funktionen
- Verwaltung beliebig vieler Projekte über eine Liste
- Ein-Klick-Sync (Pull, lokale Sicherung wichtiger Dateien, Commit, Push)
- Konfliktbehandlung mit Vorrang für lokale Dateien sowie Hinweis auf kritische Konflikte
- Speicherung von GitHub-Zugangsdaten (Nutzername + Token) in verschlüsselter Form
- Anzeige des letzten erfolgreichen Sync-Zeitpunkts pro Projekt

## Voraussetzungen
- Java 11 (wird über Gradle Toolchains automatisch geladen, falls installiert)
- Zugriff auf GitHub mit Personal Access Token
- Internetverbindung (wird vor jedem Sync geprüft)

## Einrichtung
1. Projekt herunterladen/klonen.
2. `config.sample.json` kopieren und bei Bedarf anpassen oder die App starten und im Menü
   **Optionen → Einstellungen** Benutzername und Token hinterlegen.
3. Projekte über den Button **Projekt hinzufügen** registrieren (Pfad, Remote-URL, Branch).

## Bedienung
1. Projekt in der Liste auswählen.
2. **Synchronisieren** klicken.
3. Statusbereich unten zeigt Fortschritt oder Fehlermeldungen an.

## Build
Das Projekt nutzt Gradle 7.6.1 (liegt im Repo).  
Zum Erstellen des ausführbaren JAR:

```powershell
.\gradle-7.6.1\bin\gradle.bat clean shadowJar
```

Das Ergebnis befindet sich unter `build/libs/RPGMaker-GitSync-1.0.jar`.

## Hinweise
- Token werden lokal verschlüsselt gespeichert. Für maximale Sicherheit empfiehlt sich
  dennoch die Verwaltung über das Betriebssystem (Windows-Anmeldebenutzer, macOS Keychain etc.).
- Vor dem Sync wird ein Backup sensibler Dateien (`System.json`, `Map*.json`) als `.bak`
  abgelegt, falls Konflikte manuell gelöst werden müssen.
