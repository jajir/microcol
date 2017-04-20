package org.microcol.gui;

import java.awt.Rectangle;
import java.util.Locale;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.microcol.gui.util.Text;
import org.microcol.gui.util.Text.Language;

import com.google.common.base.Preconditions;

public class GamePreferences {

	private static final String PREFERENCES_LOCALE_LANGUAGE = "locale-language";
	private static final String PREFERENCES_LOCALE_COUNTRY = "locale-country";
	private static final String PREFERENCES_STATE = "state";
	private static final String PREFERENCES_X = "x";
	private static final String PREFERENCES_Y = "y";
	private static final String PREFERENCES_WIDTH = "width";
	private static final String PREFERENCES_HEIGHT = "height";
	private static final String PREFERENCES_VOLUME = "volume";
	private static final String PREFERENCES_ANIMATION_SPEED = "animationSpeed";
	private static final String PREFERENCES_SHOW_GRID = "showGrid";

	/**
	 * When there are no value in preferences than this value is used.
	 */
	private static final int DEFAULT_VOLUME = 75;

	/**
	 * When there are no value in preferences than this value is used.
	 */
	private static final int DEFAULT_ANIMATION_SPEED = 3;

	private static final String SYSTEM_PROPERTY_DEVELOPMENT = "development";

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

	public boolean isDevelopment() {
		return Boolean.valueOf(System.getProperty(SYSTEM_PROPERTY_DEVELOPMENT, "false"));
	}

	public void setMainFramePosition(final Rectangle bounds) {
		preferences.putInt(PREFERENCES_X, bounds.x);
		preferences.putInt(PREFERENCES_Y, bounds.y);
		preferences.putInt(PREFERENCES_WIDTH, bounds.width);
		preferences.putInt(PREFERENCES_HEIGHT, bounds.height);
		flush();
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

	public void setLanguage(final Locale locale) {
		Preconditions.checkNotNull(locale);
		preferences.put(PREFERENCES_LOCALE_LANGUAGE, locale.getLanguage());
		preferences.put(PREFERENCES_LOCALE_COUNTRY, locale.getCountry());
		flush();
	}

	public Locale getLocale() {
		final String language = preferences.get(PREFERENCES_LOCALE_LANGUAGE, Locale.getDefault().getLanguage());
		final String country = preferences.get(PREFERENCES_LOCALE_COUNTRY, Locale.getDefault().getCountry());
		return new Locale(language, country);
	}

	public Text.Language getLanguage() {
		Optional<Language> lang = Language.resolve(getLocale());
		if (lang.isPresent()) {
			return lang.get();
		}
		return Language.en;
	}

	public void setVolume(final int volume) {
		preferences.putInt(PREFERENCES_VOLUME, volume);
		flush();
	}

	public int getVolume() {
		return preferences.getInt(PREFERENCES_VOLUME, DEFAULT_VOLUME);
	}

	public void setAnimationSpeed(final int animationSpeed) {
		preferences.putInt(PREFERENCES_ANIMATION_SPEED, animationSpeed);
		flush();
	}

	public int getAnimationSpeed() {
		return preferences.getInt(PREFERENCES_ANIMATION_SPEED, DEFAULT_ANIMATION_SPEED);
	}

	public void setShowGrid(final boolean isGridShown) {
		preferences.putBoolean(PREFERENCES_SHOW_GRID, isGridShown);
		flush();
	}

	public boolean isGridShown() {
		return preferences.getBoolean(PREFERENCES_SHOW_GRID, false);
	}

	private void flush() {
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

}
