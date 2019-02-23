package org.microcol.gui.preferences;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.util.PersistingTool;

import com.google.common.io.Files;

/**
 * Test for update method.
 */
public class SettingServiceTest extends AbstractPreferencesTest {

    //Tested object
    private SettingService settingService;

    private final SettingDao settingDao = mock(SettingDao.class);

    private final PersistingTool persistingTool = mock(PersistingTool.class);

    @Test
    public void verify_that_update_save_changes() throws Exception {
        final Setting setting = createSetting();
        when(persistingTool.getRootSaveDirectory()).thenReturn(Files.createTempDir());
        when(settingDao.loadFromFile(any(File.class))).thenReturn(setting);
        settingService.update(set -> {
            set.setAnimationSpeed(231);
        });

        // verify that saving to file was called
        verify(settingDao).saveToFile(eq(setting), any(File.class));

        assertEquals(Integer.valueOf(231), setting.getAnimationSpeed());
    }

    @Before
    public void before() {
        settingService = new SettingService(settingDao, persistingTool);
    }

    @After
    public void after() {
        settingService = null;
    }

}
