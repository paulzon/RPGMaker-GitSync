package com.rpgmaker.gitsync.model;

import com.rpgmaker.gitsync.util.CredentialStore;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Holds config.json content (Benutzerkonfiguration).
 */
public class Config {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.GERMANY)
                    .withZone(ZoneId.systemDefault());

    private String username = "";
    private String encryptedToken = "";
    private List<Project> projects = new ArrayList<>();
    private Map<String, String> lastSyncByProject = new HashMap<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? "" : username;
    }

    public String getEncryptedToken() {
        return encryptedToken;
    }

    public void setEncryptedToken(String encryptedToken) {
        this.encryptedToken = encryptedToken == null ? "" : encryptedToken;
    }

    public void setPlainToken(String token) {
        this.encryptedToken = CredentialStore.encrypt(token == null ? "" : token);
    }

    public String getPlainToken() {
        return CredentialStore.decrypt(encryptedToken);
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects == null ? new ArrayList<>() : projects;
    }

    public Map<String, String> getLastSyncByProject() {
        return lastSyncByProject;
    }

    public void setLastSyncByProject(Map<String, String> lastSyncByProject) {
        this.lastSyncByProject = lastSyncByProject == null ? new HashMap<>() : lastSyncByProject;
    }

    public void rememberSync(Project project) {
        if (project == null || project.getDirectory() == null) {
            return;
        }
        String timestamp = DATE_TIME_FORMATTER.format(Instant.now());
        lastSyncByProject.put(project.getPath(), timestamp);
        project.setLastSync(timestamp);
    }

    public Optional<String> getLastSync(Project project) {
        if (project == null || project.getPath() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(lastSyncByProject.get(project.getPath()));
    }

    public Credentials toCredentials() {
        return new Credentials(username, getPlainToken());
    }
}
