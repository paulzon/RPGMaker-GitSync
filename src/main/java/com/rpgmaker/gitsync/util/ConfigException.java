package com.rpgmaker.gitsync.util;

/**
 * Runtime-Ausnahme f�r Config-Handling (vereinfacht GUI-Fehlerbehandlung).
 */
public class ConfigException extends RuntimeException {
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
