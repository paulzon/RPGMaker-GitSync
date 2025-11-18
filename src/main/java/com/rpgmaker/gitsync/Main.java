package com.rpgmaker.gitsync;

import com.rpgmaker.gitsync.git.AutoSyncHandler;
import com.rpgmaker.gitsync.git.GitManager;
import com.rpgmaker.gitsync.model.Config;
import com.rpgmaker.gitsync.ui.MainWindow;
import com.rpgmaker.gitsync.util.JsonConfigManager;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Entry-Point f�r Desktop-App.
 */
public class Main {

    public static void main(String[] args) {
        configureLogging();
        SwingUtilities.invokeLater(() -> {
            JsonConfigManager configManager = new JsonConfigManager();
            Config config = configManager.loadConfig();
            GitManager gitManager = new GitManager(configManager);
            AutoSyncHandler autoSyncHandler = new AutoSyncHandler(gitManager);
            MainWindow mainWindow = new MainWindow(config, gitManager, autoSyncHandler);
            mainWindow.setVisible(true);
        });
    }

    private static void configureLogging() {
        try (InputStream in = Main.class.getResourceAsStream("/logging.properties")) {
            if (in != null) {
                LogManager.getLogManager().readConfiguration(in);
            }
        } catch (IOException ex) {
            // LogManager already uses default config; nichts weiter noetig.
        }
    }
}
