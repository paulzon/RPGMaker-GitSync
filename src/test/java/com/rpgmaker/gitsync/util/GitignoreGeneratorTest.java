package com.rpgmaker.gitsync.util;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class GitignoreGeneratorTest {

    @Test
    public void ensuresRequiredEntries() throws IOException {
        Path dir = Files.createTempDirectory("gitignore-test");
        Files.write(dir.resolve(".gitignore"), List.of("custom"));
        GitignoreGenerator generator = new GitignoreGenerator();

        generator.ensureGitignore(dir);

        List<String> lines = Files.readAllLines(dir.resolve(".gitignore"));
        assertTrue(lines.contains("save/"));
        assertTrue(lines.contains("*.rpgsave"));
        assertTrue(lines.contains("node_modules/"));
    }
}
