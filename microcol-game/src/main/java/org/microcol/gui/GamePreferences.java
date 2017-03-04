package org.microcol.gui;

import java.awt.Rectangle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class GamePreferences {

	private final static String PREFERENCES_LANGUAGE = "language";
	private static final String PREFERENCES_STATE = "state";
	private static final String PREFERENCES_X = "x";
	private static final String PREFERENCES_Y = "y";
	private static final String PREFERENCES_WIDTH = "width";
	private static final String PREFERENCES_HEIGHT = "height";
	private static final String PREFERENCES_VOLUME = "volume";

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

	public void setLanguage(final Text.Language language) {
		preferences.put(PREFERENCES_LANGUAGE, language.name());
		flush();
	}

	public Text.Language getLanguage() {
		final String name = preferences.get(PREFERENCES_LANGUAGE, Text.Language.en.name());
		return Text.Language.valueOf(name);
	}

	public void setVolume(final int volume) {
		preferences.putInt(PREFERENCES_VOLUME, volume);
		flush();
	}

	public int getVolume() {
		return preferences.getInt(PREFERENCES_VOLUME, 75);
	}

	private void flush() {
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

}
