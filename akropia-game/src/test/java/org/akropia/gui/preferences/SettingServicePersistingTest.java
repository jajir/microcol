package org.akropia.gui.preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.model.campaign.po.PersistingTool;

/**
 * This test initialize {@link SettingDao} to really read and write to json
 * file.
 */
public class SettingServicePersistingTest {

    @TempDir
    File temporaryDirectory;

    private SettingDao settingDao;

    private final PersistingTool persistingTool = mock(PersistingTool.class);

    private SettingService settingService;

    @Test
    public void verify_that_default_setting_is_used() throws Exception {
        when(persistingTool.getRootSaveDirectory())
                .thenReturn(temporaryDirectory);

        assertEquals(GamePreferences.DEFAULT_ANIMATION_SPEED,
                settingService.load().getAnimationSpeed());
        assertNotNull(settingService.load().getLocale());
    }

    @Test
    public void mapScrollSpeedPersistsAndRejectsInvalidValues() {
        when(persistingTool.getRootSaveDirectory())
                .thenReturn(temporaryDirectory);
        final GamePreferences preferences = new GamePreferences(settingService);
        assertEquals(2, preferences.getMapScrollSpeed());
        for (int speed : new int[] { 1, 2, 9 }) {
            preferences.setMapScrollSpeed(speed);
            final GamePreferences reloaded = new GamePreferences(
                    new SettingService(new SettingDao(), persistingTool));
            assertEquals(speed, reloaded.getMapScrollSpeed());
        }
        assertThrows(IllegalArgumentException.class,
                () -> preferences.setMapScrollSpeed(0));
        assertThrows(IllegalArgumentException.class,
                () -> preferences.setMapScrollSpeed(10));
        assertEquals(9, preferences.getMapScrollSpeed());
        settingService.update(setting -> setting.setMapScrollSpeed(100));
        assertEquals(2, preferences.getMapScrollSpeed());
    }

    @Test
    public void shortcutOverridesPersistPerPlatformAndKeepOtherPreferences()
            throws Exception {
        when(persistingTool.getRootSaveDirectory())
                .thenReturn(temporaryDirectory);
        final GamePreferences preferences = new GamePreferences(settingService);
        preferences.setMapScrollSpeed(7);
        preferences.setShortcutOverrides("linux",
                java.util.Map.of("map.statistics", "Y", "map.help", ""));
        preferences.setShortcutOverrides("macos",
                java.util.Map.of("session.load", "Meta+L"));
        final GamePreferences reloaded = new GamePreferences(
                new SettingService(new SettingDao(), persistingTool));
        assertEquals(java.util.Map.of("map.statistics", "Y", "map.help", ""),
                reloaded.getShortcutOverrides("linux"));
        assertEquals(java.util.Map.of("session.load", "Meta+L"),
                reloaded.getShortcutOverrides("macos"));
        assertTrue(reloaded.getShortcutOverrides("windows").isEmpty());
        assertEquals(7, reloaded.getMapScrollSpeed());
        // Removing every Linux deviation keeps the macOS ones.
        reloaded.setShortcutOverrides("linux", java.util.Map.of());
        assertTrue(preferences.getShortcutOverrides("linux").isEmpty());
        assertEquals(java.util.Map.of("session.load", "Meta+L"),
                preferences.getShortcutOverrides("macos"));
    }

    @Test
    public void malformedShortcutOverridesAreSkippedWithoutRewritingTheFile()
            throws Exception {
        when(persistingTool.getRootSaveDirectory())
                .thenReturn(temporaryDirectory);
        final GamePreferences preferences = new GamePreferences(settingService);
        preferences.setMapScrollSpeed(5);
        final File file = new File(temporaryDirectory, ".setting.json");
        final String json = new String(
                java.nio.file.Files.readAllBytes(file.toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        final String edited = json.replaceFirst("\\{",
                "{\"keyboardShortcuts\": {\"linux\": {\"map.statistics\": 5,"
                        + " \"map.help\": [\"H\"], \"map.goals\": \"Ctrl+?\","
                        + " \"map.europe\": \"U\"}, \"macos\": 3},");
        java.nio.file.Files.write(file.toPath(),
                edited.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        final GamePreferences reloaded = new GamePreferences(
                new SettingService(new SettingDao(), persistingTool));
        // Only text values are offered; their syntax is checked by the
        // shortcut configuration.
        assertEquals(java.util.Map.of("map.goals", "Ctrl+?", "map.europe", "U"),
                reloaded.getShortcutOverrides("linux"));
        assertTrue(reloaded.getShortcutOverrides("macos").isEmpty());
        assertEquals(5, reloaded.getMapScrollSpeed());
        assertEquals(edited,
                new String(java.nio.file.Files.readAllBytes(file.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8));
        // A wrongly typed root is replaced only by an explicit save.
        settingService.update(setting -> setting
                .setKeyboardShortcuts(new com.google.gson.JsonPrimitive(3)));
        assertTrue(reloaded.getShortcutOverrides("linux").isEmpty());
        reloaded.setShortcutOverrides("linux",
                java.util.Map.of("map.europe", "U"));
        assertEquals(java.util.Map.of("map.europe", "U"),
                preferences.getShortcutOverrides("linux"));
        assertEquals(5, preferences.getMapScrollSpeed());
    }

    @BeforeEach
    public void setup() {
        settingDao = new SettingDao();
        settingService = new SettingService(settingDao, persistingTool);
    }

    @AfterEach
    public void tearDown() {
        settingService = null;
        settingDao = null;
    }

}
