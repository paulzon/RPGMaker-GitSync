package com.rpgmaker.gitsync.git;

import com.rpgmaker.gitsync.model.Config;
import com.rpgmaker.gitsync.model.Credentials;
import com.rpgmaker.gitsync.model.Project;
import com.rpgmaker.gitsync.util.MessageTranslator;
import com.rpgmaker.gitsync.util.NetworkUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestriert den Sync-Ablauf laut Spezifikation.
 */
public class AutoSyncHandler {

    private static final Logger LOGGER = Logger.getLogger(AutoSyncHandler.class.getName());

    private final GitManager gitManager;

    public AutoSyncHandler(GitManager gitManager) {
        this.gitManager = gitManager;
    }

    public SyncResult run(Project project,
                          Config config,
                          Credentials credentials,
                          Consumer<String> statusConsumer) {
        Consumer<String> safeStatus = statusConsumer == null ? msg -> { } : statusConsumer;
        MessageTranslator translator = gitManager.getMessageTranslator();

        if (project == null) {
            return SyncResult.error("Kein Projekt ausgewählt.", null);
        }
        if (!credentials.isValid()) {
            return SyncResult.error("GitHub-Zugangsdaten fehlen. Bitte in den Einstellungen speichern.", null);
        }
        if (!NetworkUtil.hasInternetConnection()) {
            return SyncResult.error("Keine Verbindung zu GitHub. Internet prüfen.", null);
        }

        safeStatus.accept("Synchronisiere mit GitHub...");

        try (Git git = gitManager.ensureRepository(project, credentials)) {
            Path projectDir = project.getDirectory();
            gitManager.backupCriticalFiles(projectDir);

            PullResult pullResult = gitManager.pull(git, credentials);
            if (pullResult != null && !pullResult.isSuccessful()) {
                LOGGER.warning("Pull war nicht erfolgreich: " + pullResult.getFetchResult());
            }

            Set<String> conflicts = gitManager.detectConflicts(git);
            if (!conflicts.isEmpty()) {
                LOGGER.warning("Konflikte erkannt: " + conflicts);
                gitManager.getConflictResolver().resolveKeepingLocal(git, projectDir, conflicts);
                safeStatus.accept("Konflikt erkannt. Deine lokale Version wurde behalten.");
                if (gitManager.getConflictResolver().hasCriticalConflict(conflicts)) {
                    safeStatus.accept("Bitte kritische Dateien manuell prüfen (System/Map).");
                }
            }

            gitManager.stageContent(git, projectDir);

            gitManager.commitIfNeeded(git);

            gitManager.push(git, credentials);

            gitManager.rememberAndPersist(config, project);

            if (!conflicts.isEmpty()) {
                return SyncResult.conflict("Konflikt erkannt. Deine lokale Version wurde behalten.");
            }
            safeStatus.accept("Erfolgreich synchronisiert!");
            return SyncResult.success("Erfolgreich synchronisiert!");

        } catch (GitAPIException | IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Sync fehlgeschlagen", ex);
            String userMessage = translator.translate(ex.getMessage());
            return SyncResult.error(userMessage, ex);
        }
    }
}
