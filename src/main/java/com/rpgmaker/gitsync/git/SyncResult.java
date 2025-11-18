package com.rpgmaker.gitsync.git;

import com.rpgmaker.gitsync.model.SyncStatus;

import java.util.Optional;

/**
 * Ergebnis eines Sync-Laufs f�r UI.
 */
public class SyncResult {
    private final SyncStatus status;
    private final String message;
    private final Exception exception;

    private SyncResult(SyncStatus status, String message, Exception exception) {
        this.status = status;
        this.message = message;
        this.exception = exception;
    }

    public static SyncResult success(String message) {
        return new SyncResult(SyncStatus.SUCCESS, message, null);
    }

    public static SyncResult running(String message) {
        return new SyncResult(SyncStatus.RUNNING, message, null);
    }

    public static SyncResult error(String message, Exception exception) {
        return new SyncResult(SyncStatus.ERROR, message, exception);
    }

    public static SyncResult conflict(String message) {
        return new SyncResult(SyncStatus.CONFLICT, message, null);
    }

    public SyncStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }
}
