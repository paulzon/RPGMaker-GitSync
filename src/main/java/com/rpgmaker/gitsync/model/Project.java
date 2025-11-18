package com.rpgmaker.gitsync.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Project meta data that ends up inside config.json (Benutzersicht).
 */
public class Project {

    private String name;
    private String path;
    private String remoteUrl;
    private String branch = "main";
    private String lastSync;

    public Project() {
        // Gson Constructor
    }

    public Project(String name, String path, String remoteUrl) {
        this.name = name;
        this.path = path;
        this.remoteUrl = remoteUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Path getDirectory() {
        return path == null ? null : Paths.get(path);
    }

    public boolean hasGitRepository() {
        Path directory = getDirectory();
        return directory != null && Files.isDirectory(directory.resolve(".git"));
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        if (branch != null && !branch.isBlank()) {
            this.branch = branch;
        }
    }

    public String getLastSync() {
        return lastSync;
    }

    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }

    @Override
    public String toString() {
        return name == null ? path : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Project)) {
            return false;
        }
        Project project = (Project) o;
        return Objects.equals(path, project.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
