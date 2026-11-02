package org.akropia.model.campaign.po;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.model.AkropiaException;

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

        assertEquals(home.resolve(".akropia").toFile(),
                tool.getRootSaveDirectory());
        assertTrue(home.resolve(".akropia").toFile().isDirectory());
    }

    @Test
    void saveDirectoryReplacedByFileIsAnAkropiaFailureAndKeepsItsBytes()
            throws Exception {
        final Path blocker = Files.writeString(home.resolve(".akropia"),
                "not a directory");
        final PersistingTool tool = new PersistingTool();

        assertThrows(AkropiaException.class, tool::getRootSaveDirectory);
        assertThrows(AkropiaException.class, tool::getAutoSaveFile);
        assertEquals("not a directory", Files.readString(blocker));
    }

    @Test
    void saveRootLeavesOtherHomeContentIncludingOlderSavesUntouched()
            throws Exception {
        // Stands for saves and settings left under a former product name.
        final Path older = Files.createDirectories(home.resolve(".older-name"));
        Files.writeString(older.resolve("autosave.older-name"), "older save");
        Files.writeString(older.resolve("hall-of-fame.json"), "[]");
        Files.writeString(home.resolve("notes.txt"), "player notes");
        final Map<Path, String> before = contentOutsideSaveRoot();
        final PersistingTool tool = new PersistingTool();

        tool.getRootSaveDirectory();
        tool.getAutoSaveFile();
        tool.getHallOfFameFile();

        assertEquals(before, contentOutsideSaveRoot());
        assertTrue(home.resolve(".akropia").toFile().isDirectory());
    }

    private Map<Path, String> contentOutsideSaveRoot() throws Exception {
        final Map<Path, String> out = new TreeMap<>();
        try (Stream<Path> paths = Files.walk(home)) {
            for (final Path path : (Iterable<Path>) paths::iterator) {
                if (!path.startsWith(home.resolve(".akropia"))) {
                    out.put(home.relativize(path),
                            Files.isRegularFile(path) ? Files.readString(path)
                                    : "<directory>");
                }
            }
        }
        return out;
    }

}
