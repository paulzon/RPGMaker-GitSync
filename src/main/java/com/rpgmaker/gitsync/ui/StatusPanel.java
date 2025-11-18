package com.rpgmaker.gitsync.ui;

import com.rpgmaker.gitsync.git.SyncResult;
import com.rpgmaker.gitsync.model.SyncStatus;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * Zeigt Status-Text f�r User an.
 */
public class StatusPanel extends JPanel {

    private final JLabel statusLabel = new JLabel("Bereit");
    private final JLabel detailLabel = new JLabel("");

    public StatusPanel() {
        setLayout(new BorderLayout());
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        detailLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(statusLabel, BorderLayout.WEST);
        add(detailLabel, BorderLayout.EAST);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    public void showMessage(String message, SyncStatus status) {
        statusLabel.setText(message);
        statusLabel.setForeground(colorForStatus(status));
    }

    public void updateLastSync(String text) {
        detailLabel.setText(text == null ? "" : text);
    }

    public void applyResult(SyncResult result) {
        showMessage(result.getMessage(), result.getStatus());
    }

    private Color colorForStatus(SyncStatus status) {
        if (status == null) {
            return Color.BLACK;
        }
        switch (status) {
            case SUCCESS:
                return new Color(0, 128, 0);
            case ERROR:
                return Color.RED;
            case CONFLICT:
                return new Color(255, 140, 0);
            case RUNNING:
                return new Color(0, 102, 204);
            default:
                return Color.DARK_GRAY;
        }
    }
}
