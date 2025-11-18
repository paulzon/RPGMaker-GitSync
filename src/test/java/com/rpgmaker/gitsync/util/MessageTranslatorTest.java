package com.rpgmaker.gitsync.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MessageTranslatorTest {

    private final MessageTranslator translator = new MessageTranslator();

    @Test
    public void translatesKnownMessage() {
        assertEquals("GitHub-Zugangsdaten ungültig. Bitte Token überprüfen.",
                translator.translate("Authentication failed"));
    }

    @Test
    public void fallsBackForUnknownMessage() {
        String text = translator.translate("strange internal bug");
        assertEquals("Fehler beim Synchronisieren. Details im Log ansehen.", text);
    }
}
