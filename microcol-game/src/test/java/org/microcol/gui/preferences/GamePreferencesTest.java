package org.microcol.gui.preferences;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.util.Language;

public class GamePreferencesTest extends AbstractPreferencesTest {

    private final static Locale LOCALE_STRANGE = new Locale("zh", "TW");

    // Tested object
    private GamePreferences gamePreferences;

    private final SettingService settingService = mock(SettingService.class);

    private Setting setting;

    @Test
    public void verify_that_strange_locale_is_loaded() throws Exception {
        setting.setLocale(LOCALE_STRANGE);

        assertEquals(LOCALE_STRANGE, gamePreferences.getLocale());
    }

    @Test
    public void test_verify_default_Language_is_english() throws Exception {
        setting.setLocale(LOCALE_STRANGE);

        assertEquals(Language.en, gamePreferences.getLanguage());
    }

    @Test(expected = NullPointerException.class)
    public void test_setLocale_locale_is_required() throws Exception {
        gamePreferences.setLanguage(null);
    }

    @Before
    public void setup() {
        setting = createSetting();
        when(settingService.load()).thenReturn(setting);
        gamePreferences = new GamePreferences(settingService);
    }

    @After
    public void tearDown() {
        setting = null;
        gamePreferences = null;
    }

}
