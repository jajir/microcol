package org.microcol.gui.preferences;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

public class SettingDaoTest extends AbstractPreferencesTest {

    private SettingDao settingDao;

    @Test
    public void verify_test_and_loading() throws Exception {
        final Setting settingOriginal = createSetting();
        settingDao.saveToFile(settingOriginal, SETTING_FILE);

        printOutStoredSetting();

        final Setting settingLoaded = settingDao.loadFromFile(SETTING_FILE);

        assertEquals(settingOriginal, settingLoaded);
    }

    @Test
    public void verify_test_and_loading_of_empty_setting() throws Exception {
        final Setting settingOriginal = new Setting();
        settingDao.saveToFile(settingOriginal, SETTING_FILE);

        printOutStoredSetting();

        final Setting settingLoaded = settingDao.loadFromFile(SETTING_FILE);

        assertEquals(settingOriginal, settingLoaded);
    }

    private void printOutStoredSetting() throws IOException {
        Files.newReader(SETTING_FILE, Charset.defaultCharset()).lines()
                .forEach(System.out::println);
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
