package org.microcol.gui.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.microcol.gui.util.PersistingTool;

import com.google.common.io.Files;

/**
 * Test for update method.
 */
public class SettingServiceTest extends AbstractPreferencesTest {

    // Tested object
    private SettingService settingService;

    private final SettingDao settingDao = mock(SettingDao.class);

    private final PersistingTool persistingTool = mock(PersistingTool.class);

    @org.junit.jupiter.api.Test
    public void verify_that_update_save_changes() throws Exception {
        final Setting setting = createSetting();
        when(persistingTool.getRootSaveDirectory()).thenReturn(Files.createTempDir());
        when(settingDao.loadFromFile(any(File.class))).thenReturn(Optional.of(setting));
        settingService.update(set -> {
            set.setAnimationSpeed(231);
        });

        // verify that saving to file was called
        verify(settingDao).saveToFile(eq(setting), any(File.class));

        assertEquals(Integer.valueOf(231), setting.getAnimationSpeed());
    }

    @BeforeEach
    public void before() {
        settingService = new SettingService(settingDao, persistingTool);
    }

    @AfterEach
    public void after() {
        settingService = null;
    }

}
