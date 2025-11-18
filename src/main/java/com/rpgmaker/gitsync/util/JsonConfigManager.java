package com.rpgmaker.gitsync.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rpgmaker.gitsync.model.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles loading/writing of config.json mittels Gson.
 */
public class JsonConfigManager {

    private static final Logger LOGGER = Logger.getLogger(JsonConfigManager.class.getName());
    public static final String CONFIG_FILE = "config.json";

    private final Path configPath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonConfigManager() {
        this(Paths.get(CONFIG_FILE));
    }

    public JsonConfigManager(Path configPath) {
        this.configPath = configPath;
    }

    public Config loadConfig() {
        ensureConfigExists();
        try (BufferedReader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            Config config = gson.fromJson(reader, Config.class);
            if (config == null) {
                config = new Config();
            }
            if (config.getProjects() == null) {
                config.setProjects(null);
            }
            return config;
        } catch (IOException ex) {
            throw new ConfigException("Konnte config.json nicht lesen.", ex);
        }
    }

    public void saveConfig(Config config) {
        if (config == null) {
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);
        } catch (IOException ex) {
            throw new ConfigException("Konnte config.json nicht speichern.", ex);
        }
    }

    private void ensureConfigExists() {
        if (Files.exists(configPath)) {
            return;
        }
        try {
            Path parent = configPath.getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                gson.toJson(new Config(), writer);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Config konnte nicht angelegt werden.", ex);
            throw new ConfigException("Config konnte nicht angelegt werden.", ex);
        }
    }
}
