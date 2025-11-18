package com.rpgmaker.gitsync.ui;

import com.rpgmaker.gitsync.git.AutoSyncHandler;
import com.rpgmaker.gitsync.git.GitManager;
import com.rpgmaker.gitsync.git.SyncResult;
import com.rpgmaker.gitsync.model.Config;
import com.rpgmaker.gitsync.model.Project;
import com.rpgmaker.gitsync.model.SyncStatus;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Hauptfenster mit Liste + Buttons.
 */
public class MainWindow extends JFrame {

    private final Config config;
    private final GitManager gitManager;
    private final AutoSyncHandler syncHandler;
    private final ProjectListModel projectListModel;
    private final JList<Project> projectJList;
    private final StatusPanel statusPanel = new StatusPanel();
    private final JButton addProjectButton = new JButton("Projekt hinzufügen");
    private final JButton syncButton = new JButton("Synchronisieren");
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MainWindow(Config config, GitManager gitManager, AutoSyncHandler autoSyncHandler) {
        super("RPGMaker GitSync");
        this.config = config;
        this.gitManager = gitManager;
        this.syncHandler = autoSyncHandler;
        this.projectListModel = new ProjectListModel(config.getProjects());
        this.projectJList = new JList<>(projectListModel);
        initUi();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(600, 400));
        setLayout(new BorderLayout());

        projectJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateLastSyncLabel();
            }
        });
        projectJList.addMouseListener(new PopupListener());
        projectJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                String text = value instanceof Project
                        ? ((Project) value).getName() + " (" + ((Project) value).getPath() + ")"
                        : "";
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        projectJList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteProject");
        projectJList.getActionMap().put("deleteProject", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                removeSelectedProject();
            }
        });
        if (projectListModel.getSize() > 0) {
            projectJList.setSelectedIndex(0);
        }

        JScrollPane listScroll = new JScrollPane(projectJList);
        listScroll.setBorder(BorderFactory.createTitledBorder("Projekte"));
        add(listScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        addProjectButton.addActionListener(e -> onAddProject());
        syncButton.addActionListener(e -> runSync());

        buttonPanel.add(addProjectButton);
        buttonPanel.add(syncButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);

        setJMenuBar(buildMenu());

        pack();
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();

        JMenu menuOptions = new JMenu("Optionen");
        JMenuItem settings = new JMenuItem("Einstellungen");
        settings.addActionListener(e -> openConfigDialog());
        menuOptions.add(settings);
        bar.add(menuOptions);

        return bar;
    }

    private void openConfigDialog() {
        ConfigDialog dialog = new ConfigDialog(this, config);
        dialog.setVisible(true);
        if (dialog.wasSaved()) {
            gitManager.getConfigManager().saveConfig(config);
            statusPanel.showMessage("Einstellungen gespeichert.", SyncStatus.SUCCESS);
        }
    }

    private void onAddProject() {
        Project project = promptForProject();
        if (project == null) {
            return;
        }
        projectListModel.addProject(project);
        gitManager.getConfigManager().saveConfig(config);
        statusPanel.showMessage("Projekt gespeichert.", SyncStatus.SUCCESS);
    }

    private Project promptForProject() {
        JTextField nameField = new JTextField(20);
        JTextField pathField = new JTextField(20);
        JTextField remoteField = new JTextField(20);
        JTextField branchField = new JTextField("main", 20);

        JButton browse = new JButton("...");
        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                pathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(new javax.swing.JLabel("Projektname"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new javax.swing.JLabel("Ordner"), gbc);
        gbc.gridx = 1;
        panel.add(pathField, gbc);
        gbc.gridx = 2;
        panel.add(browse, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new javax.swing.JLabel("Remote URL"), gbc);
        gbc.gridx = 1;
        panel.add(remoteField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new javax.swing.JLabel("Branch"), gbc);
        gbc.gridx = 1;
        panel.add(branchField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Projekt hinzufügen",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String name = nameField.getText().trim();
        String path = pathField.getText().trim();
        if (path.isBlank()) {
            statusPanel.showMessage("Ordner darf nicht leer sein.", SyncStatus.ERROR);
            return null;
        }
        Project project = new Project();
        project.setName(name.isEmpty() ? path : name);
        project.setPath(Paths.get(path).toAbsolutePath().toString());
        project.setRemoteUrl(remoteField.getText().trim());
        project.setBranch(branchField.getText().trim());
        return project;
    }

    private void runSync() {
        Project project = projectJList.getSelectedValue();
        if (project == null) {
            statusPanel.showMessage("Bitte Projekt auswählen.", SyncStatus.ERROR);
            return;
        }
        statusPanel.showMessage("Sync gestartet...", SyncStatus.RUNNING);
        setActionButtonsEnabled(false);
        Consumer<String> callback = message ->
                SwingUtilities.invokeLater(() -> statusPanel.showMessage(message, SyncStatus.RUNNING));
        executor.submit(() -> {
            SyncResult result = syncHandler.run(project, config, config.toCredentials(), callback);
            SwingUtilities.invokeLater(() -> {
                statusPanel.applyResult(result);
                projectListModel.refresh();
                updateLastSyncLabel();
                setActionButtonsEnabled(true);
            });
        });
    }

    private void removeSelectedProject() {
        Project selected = projectJList.getSelectedValue();
        if (selected == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Projekt nur aus der Liste entfernen?", "Entfernen",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            projectListModel.removeProject(selected);
            gitManager.getConfigManager().saveConfig(config);
            statusPanel.showMessage("Projekt entfernt.", SyncStatus.SUCCESS);
        }
    }

    private void updateLastSyncLabel() {
        Project project = projectJList.getSelectedValue();
        if (project == null) {
            statusPanel.updateLastSync("");
        } else {
            String text = config.getLastSync(project)
                    .map(ts -> "Zuletzt: " + ts)
                    .orElse("Noch kein Sync");
            statusPanel.updateLastSync(text);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        executor.shutdownNow();
    }

    private class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger() && projectJList.getSelectedIndex() >= 0) {
                int index = projectJList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    projectJList.setSelectedIndex(index);
                }
                javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
                javax.swing.JMenuItem remove = new javax.swing.JMenuItem("Projekt entfernen");
                remove.addActionListener(evt -> removeSelectedProject());
                menu.add(remove);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void setActionButtonsEnabled(boolean enabled) {
        addProjectButton.setEnabled(enabled);
        syncButton.setEnabled(enabled);
    }
}


