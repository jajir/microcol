package org.microcol.gui.preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.util.PersistingTool;

import com.google.common.io.Files;

/**
 * This test initialize {@link SettingDao} to really read and write to json
 * file.
 */
public class SettingServicePersistingTest {

    private SettingDao settingDao;

    private final PersistingTool persistingTool = mock(PersistingTool.class);

    private SettingService settingService;

    @Test
    public void verify_that_default_setting_is_used() throws Exception {
        when(persistingTool.getRootSaveDirectory()).thenReturn(Files.createTempDir());

        assertEquals(GamePreferences.DEFAULT_ANIMATION_SPEED,
                settingService.load().getAnimationSpeed());
        assertNotNull(settingService.load().getLocale());
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
