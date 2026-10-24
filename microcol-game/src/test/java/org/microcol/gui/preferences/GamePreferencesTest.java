package org.microcol.gui.preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @Test
    public void verify_turn_summary_is_on_when_setting_is_missing()
            throws Exception {
        assertTrue(gamePreferences.isShowTurnSummary());
    }

    @Test
    public void verify_turn_summary_follows_stored_setting() throws Exception {
        setting.setTurnSummaryEnabled(false);
        assertFalse(gamePreferences.isShowTurnSummary());

        setting.setTurnSummaryEnabled(true);
        assertTrue(gamePreferences.isShowTurnSummary());
    }

    @Test
    public void verify_open_europe_on_arrival_is_off_when_setting_is_missing()
            throws Exception {
        assertFalse(gamePreferences.isOpenEuropeOnArrival());
    }

    @Test
    public void verify_open_europe_on_arrival_follows_stored_setting()
            throws Exception {
        setting.setOpenEuropeOnArrival(true);
        assertTrue(gamePreferences.isOpenEuropeOnArrival());

        setting.setOpenEuropeOnArrival(false);
        assertFalse(gamePreferences.isOpenEuropeOnArrival());
    }

    @Test
    public void test_setLocale_locale_is_required() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            gamePreferences.setLanguage(null);
        });
    }

    @BeforeEach
    public void setup() {
        setting = createSetting();
        when(settingService.load()).thenReturn(setting);
        gamePreferences = new GamePreferences(settingService);
    }

    @AfterEach
    public void tearDown() {
        setting = null;
        gamePreferences = null;
    }

}
