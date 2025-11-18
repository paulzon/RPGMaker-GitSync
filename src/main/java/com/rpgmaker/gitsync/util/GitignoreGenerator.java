package com.rpgmaker.gitsync.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates/updates RPG Maker typische .gitignore (von Spezifikation).
 */
public class GitignoreGenerator {

    private static final Logger LOGGER = Logger.getLogger(GitignoreGenerator.class.getName());

    private static final List<String> REQUIRED_ENTRIES = List.of(
            "# RPG Maker Auto-Generated",
            "save/",
            "*.rpgsave",
            "node_modules/",
            ".DS_Store",
            "Thumbs.db",
            "desktop.ini"
    );

    public void ensureGitignore(Path projectDirectory) {
        Path gitignore = projectDirectory.resolve(".gitignore");
        try {
            Set<String> lines = new LinkedHashSet<>();
            if (Files.exists(gitignore)) {
                lines.addAll(Files.readAllLines(gitignore, StandardCharsets.UTF_8));
            }
            lines.addAll(REQUIRED_ENTRIES);
            Files.write(gitignore, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ".gitignore konnte nicht aktualisiert werden.", ex);
            throw new ConfigException(".gitignore konnte nicht aktualisiert werden.", ex);
        }
    }
}
