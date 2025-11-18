# AGENTS.md

## Projektübersicht

RPGMaker-GitSync ist eine Java Desktop-Anwendung für die RPG Maker Community. Das Tool vereinfacht Git-Kollaboration für Hobby-Entwickler ohne Programmierkenntnisse durch eine einfache GUI, die alle Git-Operationen im Hintergrund abwickelt.

**Zielgruppe**: Nicht-Programmierer, die RPG Maker nutzen
**Hauptziel**: Git-Komplexität vollständig verbergen und "One-Click" Synchronisation bieten

## Technologie-Stack

- **Java**: Version 17+ (LTS)
- **GUI**: Swing (keine JavaFX - minimale Dependencies)
- **Git**: JGit Library (org.eclipse.jgit:6.10.0+)
- **Build**: Gradle mit Shadow Plugin für Fat JAR
- **Konfiguration**: JSON für Project Storage
- **Ziel-Artefakt**: Einzelnes ausführbares Fat JAR (~10-15 MB)

## Build und Test Kommandos

Build Fat JAR

./gradlew shadowJar
Build Output

build/libs/rpgmaker-gitsync-1.0-all.jar
Run application

java -jar build/libs/rpgmaker-gitsync-1.0-all.jar
Clean build

./gradlew clean
Run tests

./gradlew test

text

## Projektstruktur

src/main/java/com/rpgmaker/gitsync/
├── Main.java # Entry point mit main()
├── ui/
│ ├── MainWindow.java # Haupt-GUI (JFrame)
│ ├── ConfigDialog.java # GitHub Credentials Dialog
│ ├── StatusPanel.java # Sync Status Anzeige
│ └── ProjectListModel.java # JList Model für Projekte
├── git/
│ ├── GitManager.java # Wrapper für JGit Operationen
│ ├── AutoSyncHandler.java # Implementiert AUTO-Sync Logik
│ └── ConflictResolver.java # Merge-Konflikt Behandlung
├── model/
│ ├── Project.java # Projekt Datenklasse
│ ├── Config.java # App Konfiguration
│ └── SyncStatus.java # Enum für Sync-Zustände
└── util/
├── JsonConfigManager.java # Lesen/Schreiben config.json
├── GitignoreGenerator.java # Generiert RPG Maker .gitignore
└── MessageTranslator.java # Git-Fehler → benutzerfreundlich

text

## Do's ✓

- **Einfachheit first**: GUI so simpel wie möglich, nur 3 Hauptbuttons
- **Deutsche Kommentare**: Code auf Englisch, Kommentare auf Deutsch für Community
- **Benutzerfreundliche Fehler**: Keine technischen Git-Begriffe in UI-Messages
- **Defensive Programmierung**: Alle Git-Operationen in try-catch
- **Last-Write-Wins**: Bei Merge-Konflikten lokale Version bevorzugen
- **Auto-Generate**: .gitignore automatisch erstellen beim Projekt-Add
- **Config Backup**: Vor jedem Sync lokale Kopie kritischer Dateien
- **Logging**: Technische Details ins Log, benutzerfreundliche Messages in GUI

## Don'ts ✗

- **Keine externen Git-Calls**: Nur JGit verwenden, kein `Runtime.exec("git ...")`
- **Keine JavaFX**: Nur Swing für maximale Kompatibilität
- **Keine komplexe Merge-Logik**: Keep it simple - Last-Write-Wins oder Warnung
- **Keine unsicheren Credentials**: Token nie im Klartext loggen oder committen
- **Keine manuellen Git-Operationen**: User soll nie direkt mit Git arbeiten müssen
- **Kein Bloat**: Dependencies minimal halten (nur JGit + JSON Parser)
- **Keine technischen Begriffe**: UI-Text muss für Nicht-Programmierer verständlich sein

## Code Style

- **Naming**: Camel Case für Methoden, Pascal Case für Klassen
- **Error Messages**: Immer zweischichtig: User-Message + Technical Log
- **Constants**: Alle Magic Strings als Konstanten definieren
- **Dependency Injection**: GitManager als Singleton oder über Constructor
- **GUI Threading**: SwingUtilities.invokeLater für UI-Updates aus Threads

## RPG Maker Spezifika

### Zu synchronisierende Ordner/Dateien:

✓ audio/ # Alle Sound-Dateien
✓ img/ # Alle Grafiken
✓ movies/ # Videos
✓ data/ # JSON Projektdaten (KRITISCH)
✓ js/ # JavaScript/Plugins
✓ css/ # Stylesheets
✓ fonts/ # Schriftarten
✓ package.json # Projekt-Metadaten

text

### Zu IGNORIERENDE Dateien (.gitignore):

✗ save/ # Savegames NIE syncen
✗ *.rpgsave # Save-Dateien
✗ node_modules/ # Node Dependencies
✗ .DS_Store # macOS
✗ Thumbs.db # Windows
✗ desktop.ini # Windows

text

### Kritische Konflikt-Dateien:
- `data/System.json` - Projekt-Einstellungen
- `data/MapXXX.json` - Map-Dateien
- Bei Konflikten in diesen Dateien: **Warnung + manuelle Prüfung verlangen**

## AUTO-Sync Ablauf

Implementiere diese exakte Reihenfolge in `AutoSyncHandler.java`:

    Internet-Check (ping github.com oder DNS lookup)

    Status-Update: "Synchronisiere mit GitHub..."

    Git Pull mit Rebase-Strategie

    Bei Konflikten:

        Backup erstellen (.bak Dateien)

        "Ours" Strategie anwenden

        User warnen: "Konflikt erkannt. Deine lokale Version wurde behalten."

    Git Add alle Änderungen (außer .gitignore)

    Git Commit mit Message: "Auto-Sync: [DD.MM.YYYY HH:MM:SS]"

    Git Push mit Credentials

    Status-Update: "Erfolgreich synchronisiert!" oder detaillierte Fehlermeldung

    Timestamp in config.json aktualisieren

text

## Sicherheit und Berechtigungen

### Erlaubt ohne Bestätigung:
- Projekt-Liste anzeigen
- Config.json lesen
- .gitignore generieren
- Status-Abfragen (git status, git log)

### Erfordert User-Bestätigung:
- Projekt hinzufügen (erstellt Git-Repo)
- AUTO-Sync (pusht zu GitHub)
- Projekt entfernen (löscht aus Liste, nicht vom Disk)

### Credentials Handling:
- GitHub Token in config.json verschlüsselt speichern oder Keychain nutzen
- config.json MUSS in .gitignore
- Username + Token für JGit UsernamePasswordCredentialsProvider

## Fehlerbehandlung - Message Mapping

Übersetze technische Git-Fehler in verständliche Nachrichten:

// GitManager.java oder MessageTranslator.java
"Authentication failed"
→ "GitHub-Zugangsdaten ungültig. Bitte Token überprüfen."

"Remote repository not found"
→ "GitHub-Projekt nicht gefunden. URL korrekt?"

"Connection timeout"
→ "Keine Verbindung zu GitHub. Internet-Verbindung prüfen."

"Merge conflict in data/System.json"
→ "Zwei Personen haben gleichzeitig am Projekt gearbeitet. Kontaktiere Projektleiter."

"Could not push - non-fast-forward"
→ "Jemand hat zwischenzeitlich Änderungen hochgeladen. Erneut synchronisieren."

text

## Gradle Build Configuration

// build.gradle
plugins {
id 'java'
id 'com.github.johnrengelman.shadow' version '8.1.1'
}

group = 'com.rpgmaker'
version = '1.0'
sourceCompatibility = '17'

repositories {
mavenCentral()
}

dependencies {
implementation 'org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r'
implementation 'com.google.code.gson:gson:2.10.1' // Für JSON
testImplementation 'junit:junit:4.13.2'
}

shadowJar {
archiveBaseName.set('rpgmaker-gitsync')
archiveClassifier.set('all')
archiveVersion.set('1.0')

text
manifest {
    attributes 'Main-Class': 'com.rpgmaker.gitsync.Main'
}

}

text

## Testing Strategie

- **Unit Tests**: Für GitignoreGenerator, MessageTranslator
- **Integration Tests**: Mock JGit Repository für GitManager
- **Manual Testing Checklist**:
  - [ ] JAR startet per Doppelklick
  - [ ] Projekt hinzufügen funktioniert
  - [ ] AUTO-Sync bei sauberem Repo
  - [ ] AUTO-Sync mit lokalen Änderungen
  - [ ] AUTO-Sync mit Remote-Änderungen
  - [ ] Konflikt-Handling
  - [ ] Ungültige Credentials
  - [ ] Keine Internet-Verbindung

## API Beispiele (JGit)

// Init Repository
Git git = Git.init().setDirectory(projectDir).call();

// Add Files
git.add().addFilepattern(".").call();

// Commit
git.commit().setMessage("Auto-Sync: " + timestamp).call();

// Pull with Rebase
git.pull()
.setRebase(true)
.setCredentialsProvider(credentialsProvider)
.call();

// Push
git.push()
.setCredentialsProvider(credentialsProvider)
.call();

// Credentials
UsernamePasswordCredentialsProvider credentialsProvider =
new UsernamePasswordCredentialsProvider(username, token);

text

## Wenn du feststeckst

- **Unklare Anforderung**: Frage nach, bevor du implementierst
- **JGit-Problem**: Konsultiere offizielle JGit Dokumentation
- **GUI-Layout**: Orientiere dich an GitHub Desktop's Simplizität
- **Merge-Konflikt**: Im Zweifel "Ours" Strategy + User-Warnung

## Deployment Checklist

- [ ] Fat JAR generiert mit `./gradlew shadowJar`
- [ ] JAR-Größe < 20 MB
- [ ] Main-Class Manifest korrekt
- [ ] Auf Windows/macOS/Linux getestet
- [ ] README.md mit deutscher Anleitung erstellt
- [ ] GitHub Release mit JAR-Attachment
- [ ] Community-Forum Post mit Download-Link

## Prioritäten

1. **Funktionalität** > Features
2. **Benutzerfreundlichkeit** > Flexibilität  
3. **Stabilität** > Performance
4. **Einfachheit** > Optimierung

Das Tool ist für Nicht-Programmierer. Eine funktionierende, verständliche Lösung ist besser als eine perfekte, komplexe Lösung.