package org.microcol.gui;

import java.awt.Rectangle;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class GamePreferences {

	private final static String LOCALE_LANGUAGE = "locale_language";

	private final static String LOCALE_COUNTRY = "locale_country";

	private static final String PREFERENCES_STATE = "state";
	private static final String PREFERENCES_X = "x";
	private static final String PREFERENCES_Y = "y";
	private static final String PREFERENCES_WIDTH = "width";
	private static final String PREFERENCES_HEIGHT = "height";

	private final Preferences preferences = Preferences.userNodeForPackage(GamePreferences.class);

	/**
	 * Determine if application runs on Apple system.
	 * 
	 * @return return <code>true</code> when it's Apple system otherwise return
	 *         <code>false</code>
	 */
	public boolean isOSX() {
		final String osName = System.getProperty("os.name");
		return osName.contains("OS X");
	}

	public void setMainFramePosition(final Rectangle bounds) {
		preferences.putInt(PREFERENCES_X, bounds.x);
		preferences.putInt(PREFERENCES_Y, bounds.y);
		preferences.putInt(PREFERENCES_WIDTH, bounds.width);
		preferences.putInt(PREFERENCES_HEIGHT, bounds.height);
	}

	public Rectangle getMainFramePosition() {
		final int x = preferences.getInt(PREFERENCES_X, Integer.MIN_VALUE);
		final int y = preferences.getInt(PREFERENCES_Y, Integer.MIN_VALUE);
		final int width = preferences.getInt(PREFERENCES_WIDTH, Integer.MIN_VALUE);
		final int height = preferences.getInt(PREFERENCES_HEIGHT, Integer.MIN_VALUE);
		return new Rectangle(x, y, width, height);
	}

	public void setMainFrameState(final int state) {
		preferences.putInt(PREFERENCES_STATE, state);
	}

	public int getMainFrameState() {
		return preferences.getInt(PREFERENCES_STATE, Integer.MIN_VALUE);
	}

	public Locale getLocale() {
		final String language = preferences.get(LOCALE_LANGUAGE, "en");
		final String country = preferences.get(LOCALE_COUNTRY, "US");
		return new Locale(language, country);
	}

	public void setLocale(final Locale locale) {
		preferences.put(LOCALE_LANGUAGE, locale.getLanguage());
		preferences.put(LOCALE_COUNTRY, locale.getCountry());
		flush();
	}

	private void flush() {
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

}
