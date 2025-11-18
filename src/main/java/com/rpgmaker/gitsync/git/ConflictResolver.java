package com.rpgmaker.gitsync.git;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles Merge-Konflikte (Last-Write-Wins + Backup).
 */
public class ConflictResolver {

    private static final Logger LOGGER = Logger.getLogger(ConflictResolver.class.getName());

    public void resolveKeepingLocal(Git git, Path projectDir, Set<String> conflicts) throws GitAPIException {
        if (conflicts == null || conflicts.isEmpty()) {
            return;
        }
        createBackups(projectDir, conflicts);
        CheckoutCommand checkout = git.checkout();
        checkout.setStage(CheckoutCommand.Stage.OURS);
        conflicts.forEach(checkout::addPath);
        checkout.call();
    }

    public boolean hasCriticalConflict(Set<String> conflicts) {
        if (conflicts == null) {
            return false;
        }
        return conflicts.stream().anyMatch(this::isCriticalFile);
    }

    private boolean isCriticalFile(String path) {
        return "data/System.json".equals(path) || path.startsWith("data/Map");
    }

    private void createBackups(Path projectDir, Set<String> conflicts) {
        if (projectDir == null || conflicts == null) {
            return;
        }
        conflicts.forEach(conflict -> {
            Path file = projectDir.resolve(conflict);
            if (Files.exists(file)) {
                Path backup = file.resolveSibling(file.getFileName() + ".bak");
                try {
                    Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Backup konnte nicht erstellt werden: " + conflict, ex);
                }
            }
        });
    }
}
