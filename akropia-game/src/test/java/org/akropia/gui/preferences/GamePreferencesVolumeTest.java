package org.akropia.gui.preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.model.campaign.po.PersistingTool;

/**
 * Separate music and effects volumes (R-0227) in the application setting file,
 * independent of any audio device.
 */
class GamePreferencesVolumeTest {

    @TempDir
    Path directory;

    private final PersistingTool files = mock(PersistingTool.class);

    @BeforeEach
    void setUp() {
        when(files.getRootSaveDirectory()).thenReturn(directory.toFile());
    }

    @Test
    void aNewInstallationUsesTheDefaultVolumeForBoth() {
        final GamePreferences preferences = restart();

        assertEquals(GamePreferences.DEFAULT_VOLUME,
                preferences.getMusicVolume());
        assertEquals(GamePreferences.DEFAULT_VOLUME,
                preferences.getEffectsVolume());
    }

    @Test
    void eachVolumeIsSavedAloneAndSurvivesARestart() {
        final GamePreferences preferences = restart();
        preferences.setMusicVolume(0);
        assertEquals(0, preferences.getMusicVolume());
        assertEquals(GamePreferences.DEFAULT_VOLUME,
                preferences.getEffectsVolume());

        preferences.setEffectsVolume(100);
        assertEquals(0, preferences.getMusicVolume());
        assertEquals(100, preferences.getEffectsVolume());

        final GamePreferences restarted = restart();
        assertEquals(0, restarted.getMusicVolume());
        assertEquals(100, restarted.getEffectsVolume());

        // Silencing the effects does not unmute or change the music.
        restarted.setEffectsVolume(0);
        restarted.setMusicVolume(40);
        final GamePreferences again = restart();
        assertEquals(40, again.getMusicVolume());
        assertEquals(0, again.getEffectsVolume());
    }

    @Test
    void aSettingWithoutEffectsVolumeKeepsTheSavedMusicVolumeForBoth()
            throws IOException {
        saveFile("{\"volume\": 0, \"version\": \"1.0\"}");
        final GamePreferences silent = restart();
        // A player who chose silence stays silent.
        assertEquals(0, silent.getMusicVolume());
        assertEquals(0, silent.getEffectsVolume());

        saveFile("{\"volume\": 30, \"version\": \"1.0\"}");
        final GamePreferences quiet = restart();
        assertEquals(30, quiet.getMusicVolume());
        assertEquals(30, quiet.getEffectsVolume());

        // Changing the music later does not move the effects with it.
        quiet.setMusicVolume(80);
        assertEquals(80, quiet.getMusicVolume());
        assertEquals(30, restart().getEffectsVolume());
    }

    @Test
    void changingTheMusicKeepsTheEffectsAnOldSettingImplied()
            throws IOException {
        // Silent, missing and out-of-range music without an effects value.
        final String[] musicValues = { "\"volume\": 0,", "",
                "\"volume\": 250," };
        final int[] impliedEffects = { 0, GamePreferences.DEFAULT_VOLUME, 100 };
        for (int i = 0; i < musicValues.length; i++) {
            final String json = "{" + musicValues[i] + " \"version\": \"1.0\"}";
            saveFile(json);
            final GamePreferences preferences = restart();
            assertEquals(impliedEffects[i], preferences.getEffectsVolume());
            // Reading alone never rewrites the player's file.
            assertEquals(json, readFile());

            preferences.setMusicVolume(40);
            preferences.setMusicVolume(60);
            final GamePreferences restarted = restart();
            assertEquals(60, restarted.getMusicVolume(), json);
            assertEquals(impliedEffects[i], restarted.getEffectsVolume(), json);
        }
    }

    @Test
    void changingTheMusicLeavesASavedEffectsVolumeAlone() throws IOException {
        saveFile("{\"volume\": 30, \"effectsVolume\": 0,"
                + " \"version\": \"1.0\"}");
        final GamePreferences preferences = restart();
        preferences.setMusicVolume(90);

        final GamePreferences restarted = restart();
        assertEquals(90, restarted.getMusicVolume());
        assertEquals(0, restarted.getEffectsVolume());
    }

    @Test
    void editedValuesOutsideTheRangeAreClamped() throws IOException {
        saveFile("{\"volume\": 250, \"effectsVolume\": -5,"
                + " \"version\": \"1.0\"}");
        final GamePreferences preferences = restart();

        assertEquals(100, preferences.getMusicVolume());
        assertEquals(0, preferences.getEffectsVolume());
    }

    @Test
    void aMalformedSettingFallsBackToDefaults() throws IOException {
        saveFile("{\"volume\": \"loud\", \"effectsVolume\": 20,"
                + " \"version\": \"1.0\"}");
        final GamePreferences preferences = restart();

        assertEquals(GamePreferences.DEFAULT_VOLUME,
                preferences.getMusicVolume());
        assertEquals(GamePreferences.DEFAULT_VOLUME,
                preferences.getEffectsVolume());
    }

    @Test
    void aVolumeOutsideTheRangeIsRejectedWithoutSaving() {
        final GamePreferences preferences = restart();
        preferences.setMusicVolume(10);
        preferences.setEffectsVolume(20);

        assertThrows(IllegalArgumentException.class,
                () -> preferences.setMusicVolume(101));
        assertThrows(IllegalArgumentException.class,
                () -> preferences.setMusicVolume(-1));
        assertThrows(IllegalArgumentException.class,
                () -> preferences.setEffectsVolume(101));
        assertThrows(IllegalArgumentException.class,
                () -> preferences.setEffectsVolume(-1));

        final GamePreferences restarted = restart();
        assertEquals(10, restarted.getMusicVolume());
        assertEquals(20, restarted.getEffectsVolume());
    }

    /** Preferences of a freshly started application on the same files. */
    private GamePreferences restart() {
        return new GamePreferences(new SettingService(new SettingDao(), files));
    }

    private void saveFile(final String json) throws IOException {
        Files.write(directory.resolve(".setting.json"),
                json.getBytes(StandardCharsets.UTF_8));
    }

    private String readFile() throws IOException {
        return Files.readString(directory.resolve(".setting.json"));
    }

}
