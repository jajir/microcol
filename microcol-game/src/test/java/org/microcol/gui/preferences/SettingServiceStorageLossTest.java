package org.microcol.gui.preferences;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.MicroColException;
import org.microcol.model.campaign.po.PersistingTool;

/**
 * Losing the save location during play must not stop running gameplay from
 * reading its settings, and must never overwrite or move the blocked path.
 */
class SettingServiceStorageLossTest {

    @TempDir
    Path home;

    private String originalHome;

    private Path saveFolder;

    private SettingService service;

    @BeforeEach
    void useTemporaryHome() {
        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        saveFolder = home.resolve(".microcol");
        service = new SettingService(new SettingDao(), new PersistingTool());
    }

    @AfterEach
    void restoreHome() {
        System.setProperty("user.home", originalHome);
    }

    @Test
    void lostStorageKeepsLastKnownChoicesAndRetriesAfterRestore()
            throws Exception {
        service.update(setting -> {
            setting.setVolume(17);
            setting.setShowGrid(true);
        });
        assertEquals(17, service.load().getVolume());

        final Path moved = home.resolve("saved-away");
        Files.move(saveFolder, moved);
        Files.writeString(saveFolder, "not a directory");

        // Repeated reads (paint loop) keep returning the remembered choices.
        for (int read = 0; read < 3; read++) {
            final Setting duringLoss = service.load();
            assertEquals(17, duringLoss.getVolume());
            assertTrue(duringLoss.isShowGrid());
        }
        assertEquals("not a directory", Files.readString(saveFolder));

        // Restoring the folder is noticed by the next read.
        Files.delete(saveFolder);
        Files.move(moved, saveFolder);
        service.update(setting -> setting.setVolume(33));
        assertEquals(33, service.load().getVolume());
    }

    @Test
    void failedSaveIsStillReportedAndBlockedPathIsNeverTouched()
            throws Exception {
        service.update(setting -> setting.setVolume(17));
        final Path moved = home.resolve("saved-away");
        Files.move(saveFolder, moved);
        Files.writeString(saveFolder, "not a directory");

        assertThrows(MicroColException.class,
                () -> service.update(setting -> setting.setVolume(99)));
        assertThrows(MicroColException.class,
                () -> service.save(new Setting()));

        assertEquals("not a directory", Files.readString(saveFolder));
        assertEquals(17, service.load().getVolume(),
                "A failed save must not change the remembered choices");
    }

    @Test
    void fallbackIsAnIndependentCopyAndIsNeverWrittenBack() throws Exception {
        service.update(setting -> setting.setVolume(17));
        final Path moved = home.resolve("saved-away");
        Files.move(saveFolder, moved);
        Files.writeString(saveFolder, "not a directory");

        service.load().setVolume(1);
        assertEquals(17, service.load().getVolume());

        Files.delete(saveFolder);
        Files.move(moved, saveFolder);
        final byte[] before = Files
                .readAllBytes(saveFolder.resolve(".setting.json"));
        assertEquals(17, service.load().getVolume());
        assertArrayEquals(before,
                Files.readAllBytes(saveFolder.resolve(".setting.json")));
    }

    @Test
    void defaultsAreUsedWhenNothingWasKnownBeforeTheLoss() throws Exception {
        Files.writeString(saveFolder, "not a directory");

        final Setting setting = service.load();

        assertEquals(GamePreferences.DEFAULT_VOLUME, setting.getVolume());
        assertEquals("not a directory", Files.readString(saveFolder));
    }

}
