package org.microcol.model.campaign.po;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.MicroColException;

class PersistingToolSaveRootTest {

    @TempDir
    Path home;

    private String originalHome;

    @BeforeEach
    void useTemporaryHome() {
        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
    }

    @AfterEach
    void restoreHome() {
        System.setProperty("user.home", originalHome);
    }

    @Test
    void missingSaveDirectoryIsCreatedInsideHome() {
        final PersistingTool tool = new PersistingTool();

        assertEquals(home.resolve(".microcol").toFile(),
                tool.getRootSaveDirectory());
        assertTrue(home.resolve(".microcol").toFile().isDirectory());
    }

    @Test
    void saveDirectoryReplacedByFileIsAMicroColFailureAndKeepsItsBytes()
            throws Exception {
        final Path blocker = Files.writeString(home.resolve(".microcol"),
                "not a directory");
        final PersistingTool tool = new PersistingTool();

        assertThrows(MicroColException.class, tool::getRootSaveDirectory);
        assertThrows(MicroColException.class, tool::getAutoSaveFile);
        assertEquals("not a directory", Files.readString(blocker));
    }

}
