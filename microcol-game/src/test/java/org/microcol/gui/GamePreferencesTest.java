package org.microcol.gui;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.util.Text.Language;

public class GamePreferencesTest {

	private GamePreferences gamePreferences;

	@Test
	public void test_storeLocale() throws Exception {
		Locale locale = new Locale("zh", "TW");

		gamePreferences.setLanguage(locale);
		final Locale loc2 = gamePreferences.getLocale();

		assertNotNull(loc2);
		assertEquals(locale, loc2);
	}
	
	@Test
	public void test_verify_default_Language_is_english() throws Exception {
		Locale locale = new Locale("zh", "TW");
		gamePreferences.setLanguage(locale);
		
		assertEquals(Language.en, gamePreferences.getLanguage());
	}

	@Test(expected = NullPointerException.class)
	public void test_setLocale_locale_is_required() throws Exception {
		gamePreferences.setLanguage(null);
	}

	@Before
	public void setup() {
		gamePreferences = new GamePreferences();
	}

	@After
	public void tearDown() {
		gamePreferences = null;
	}

}
