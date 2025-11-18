package com.rpgmaker.gitsync.git;

import com.rpgmaker.gitsync.model.Config;
import com.rpgmaker.gitsync.model.Credentials;
import com.rpgmaker.gitsync.model.Project;
import com.rpgmaker.gitsync.util.GitignoreGenerator;
import com.rpgmaker.gitsync.util.JsonConfigManager;
import com.rpgmaker.gitsync.util.MessageTranslator;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstraktion um JGit laut Anforderungen.
 */
public class GitManager {

    private static final Logger LOGGER = Logger.getLogger(GitManager.class.getName());
    private static final List<String> SYNC_PATTERNS = List.of("audio", "img", "movies", "data", "js", "css", "fonts");
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.GERMANY).withZone(ZoneId.systemDefault());

    private final JsonConfigManager configManager;
    private final GitignoreGenerator gitignoreGenerator = new GitignoreGenerator();
    private final MessageTranslator translator = new MessageTranslator();
    private final ConflictResolver conflictResolver = new ConflictResolver();

    public GitManager(JsonConfigManager configManager) {
        this.configManager = configManager;
    }

    public JsonConfigManager getConfigManager() {
        return configManager;
    }

    public MessageTranslator getTranslator() {
        return translator;
    }

    public Git ensureRepository(Project project, Credentials credentials) throws IOException, GitAPIException {
        Path directory = project.getDirectory();
        if (directory == null) {
            throw new IllegalArgumentException("Projektordner fehlt.");
        }
        Files.createDirectories(directory);
        gitignoreGenerator.ensureGitignore(directory);
        if (project.hasGitRepository()) {
            return Git.open(directory.toFile());
        }
        if (project.getRemoteUrl() != null && !project.getRemoteUrl().isBlank() && isDirectoryEmpty(directory)) {
            LOGGER.info("Clone Repository: " + project.getRemoteUrl());
            return Git.cloneRepository()
                    .setURI(project.getRemoteUrl())
                    .setBranch(project.getBranch())
                    .setDirectory(directory.toFile())
                    .setCredentialsProvider(credentials.toProvider())
                    .call();
        }
        LOGGER.info("Init Repository: " + directory);
        Git git = Git.init().setDirectory(directory.toFile()).call();
        configureRemote(git, project);
        return git;
    }

    public PullResult pull(Git git, Credentials credentials) throws GitAPIException {
        CredentialsProvider provider = credentials.toProvider();
        return git.pull()
                .setRebase(true)
                .setCredentialsProvider(provider)
                .call();
    }

    public void push(Git git, Credentials credentials) throws GitAPIException {
        git.push()
                .setCredentialsProvider(credentials.toProvider())
                .call();
    }

    public boolean stageContent(Git git, Path projectDir) throws GitAPIException {
        AddCommand add = git.add();
        boolean added = false;
        for (String pattern : SYNC_PATTERNS) {
            if (Files.exists(projectDir.resolve(pattern))) {
                add.addFilepattern(pattern);
                added = true;
            }
        }
        if (Files.exists(projectDir.resolve("package.json"))) {
            add.addFilepattern("package.json");
            added = true;
        }
        if (added) {
            add.call();
        }
        return added;
    }

    public boolean commitIfNeeded(Git git) throws GitAPIException, IOException {
        Status status = git.status().call();
        if (status.isClean()) {
            return false;
        }
        String message = "Sync: " + TIMESTAMP.format(Instant.now());
        git.commit().setMessage(message).call();
        return true;
    }

    public Set<String> detectConflicts(Git git) throws GitAPIException {
        Status status = git.status().call();
        return new HashSet<>(status.getConflicting());
    }

    public ConflictResolver getConflictResolver() {
        return conflictResolver;
    }

    public void backupCriticalFiles(Path projectDir) {
        Path dataDir = projectDir.resolve("data");
        copyIfExists(dataDir.resolve("System.json"));
        try (DirectoryStream<Path> stream = Files.isDirectory(dataDir)
                ? Files.newDirectoryStream(dataDir, "Map*.json") : null) {
            if (stream != null) {
                for (Path path : stream) {
                    copyIfExists(path);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Backup der Map-Dateien fehlgeschlagen.", ex);
        }
    }

    public void rememberAndPersist(Config config, Project project) {
        config.rememberSync(project);
        configManager.saveConfig(config);
    }

    private void copyIfExists(Path file) {
        if (file == null || Files.notExists(file)) {
            return;
        }
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        try {
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Backup fehlgeschlagen: " + file, ex);
        }
    }

    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var stream = Files.list(directory)) {
            return !stream.findFirst().isPresent();
        }
    }

    private void configureRemote(Git git, Project project) {
        if (project.getRemoteUrl() == null || project.getRemoteUrl().isBlank()) {
            return;
        }
        try {
            StoredConfig config = git.getRepository().getConfig();
            config.setString("remote", "origin", "url", project.getRemoteUrl());
            config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
            config.save();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Remote konnte nicht gesetzt werden.", ex);
        }
    }

    public MessageTranslator getMessageTranslator() {
        return translator;
    }
}
