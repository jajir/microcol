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
