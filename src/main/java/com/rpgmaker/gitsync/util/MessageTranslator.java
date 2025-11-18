package com.rpgmaker.gitsync.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps technische Git-Fehler zu verständlichen Meldungen.
 */
public class MessageTranslator {

    private static final Map<String, String> TEMPLATES = new LinkedHashMap<>();

    static {
        TEMPLATES.put("Authentication failed", "GitHub-Zugangsdaten ungültig. Bitte Token überprüfen.");
        TEMPLATES.put("Remote repository not found", "GitHub-Projekt nicht gefunden. URL korrekt?");
        TEMPLATES.put("Connection timeout", "Keine Verbindung zu GitHub. Internet-Verbindung prüfen.");
        TEMPLATES.put("Merge conflict in data/System.json", "Zwei Personen haben gleichzeitig am Projekt gearbeitet. Kontaktiere Projektleiter.");
        TEMPLATES.put("Could not push - non-fast-forward", "Jemand hat zwischenzeitlich Änderungen hochgeladen. Erneut synchronisieren.");
    }

    public String translate(String technicalMessage) {
        if (technicalMessage == null || technicalMessage.isBlank()) {
            return "Unbekannter Fehler ist aufgetreten.";
        }
        for (Map.Entry<String, String> entry : TEMPLATES.entrySet()) {
            if (technicalMessage.toLowerCase(Locale.ROOT).contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                return entry.getValue();
            }
        }
        return "Fehler beim Synchronisieren. Details im Log ansehen.";
    }
}
