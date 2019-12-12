package org.microcol.gui.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class SettingDaoTest extends AbstractPreferencesTest {

    private final Logger logger = LoggerFactory.getLogger(SettingDaoTest.class);

    private SettingDao settingDao;

    @Test
    public void verify_test_and_loading() throws Exception {
        final Setting settingOriginal = createSetting();
        settingDao.saveToFile(settingOriginal, SETTING_FILE);

        printOutStoredSetting();

        final Optional<Setting> oSettingLoaded = settingDao.loadFromFile(SETTING_FILE);

        assertTrue(oSettingLoaded.isPresent());
        assertEquals(settingOriginal, oSettingLoaded.get());
    }

    @Test
    public void verify_test_and_loading_of_empty_setting() throws Exception {
        final Setting settingOriginal = new Setting();
        settingDao.saveToFile(settingOriginal, SETTING_FILE);

        printOutStoredSetting();

        final Optional<Setting> oSettingLoaded = settingDao.loadFromFile(SETTING_FILE);

        assertTrue(oSettingLoaded.isPresent());
        assertEquals(settingOriginal, oSettingLoaded.get());
    }

    private void printOutStoredSetting() throws IOException {
        Files.newReader(SETTING_FILE, Charset.defaultCharset()).lines()
                .forEach(logger::debug);
    }

    @BeforeEach
    public void before() {
        settingDao = new SettingDao();
    }

    @AfterEach
    public void after() {
        settingDao = null;
    }

}
