package com.rpgmaker.gitsync.ui;

import com.rpgmaker.gitsync.model.Config;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

/**
 * Dialog fuer Credentials.
 */
public class ConfigDialog extends JDialog {

    private final Config config;
    private boolean saved;

    private JTextField usernameField;
    private JPasswordField tokenField;

    public ConfigDialog(JFrame owner, Config config) {
        super(owner, "Einstellungen", true);
        this.config = config;
        initComponents();
    }

    private void initComponents() {
        usernameField = new JTextField(config.getUsername(), 20);
        tokenField = new JPasswordField(config.getPlainToken(), 20);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        form.add(new JLabel("GitHub Benutzername"), gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("GitHub Token"), gbc);
        gbc.gridx = 1;
        form.add(tokenField, gbc);

        JButton cancel = new JButton("Abbrechen");
        cancel.addActionListener(e -> dispose());
        JButton save = new JButton("Speichern");
        save.addActionListener(e -> onSave());

        JPanel buttons = new JPanel();
        buttons.add(cancel);
        buttons.add(save);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void onSave() {
        config.setUsername(usernameField.getText().trim());
        char[] token = tokenField.getPassword();
        config.setPlainToken(new String(token));
        Arrays.fill(token, '\0');
        saved = true;
        dispose();
    }

    public boolean wasSaved() {
        return saved;
    }
}
