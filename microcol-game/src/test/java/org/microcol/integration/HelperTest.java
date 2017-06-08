package org.microcol.integration;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.Test;

public class HelperTest {
	@Test
	public void test_printOut_syste_resulution() throws Exception {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		System.out.println(width);
		System.out.println(height);
	}

	@Test
	public void test_sysResolution() throws Exception {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		System.out.println(width);
		System.out.println(height);
	}

	/**
	 * test could help with manual testing of default setting.
	 * 
	 * @throws BackingStoreException
	 */
	@Test
	public void test_clear_preferences() throws BackingStoreException {
		Preferences.userNodeForPackage(org.microcol.gui.GamePreferences.class).clear();
	}

}
