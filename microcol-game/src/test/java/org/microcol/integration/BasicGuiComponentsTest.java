package org.microcol.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.MainFrameView;
import org.microcol.gui.MicroColException;
import org.microcol.gui.MicroColModule;
import org.microcol.gui.event.model.GameController;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class BasicGuiComponentsTest {

	private FrameFixture window;

	@Test
	public void test_that_next_turn_button_is_on_screen() {
		final JButtonFixture buttonFixture = window.button("nextTurnButton");
		assertNotNull(buttonFixture);
	}

	@Test
	public void test_that_next_turn_doesnt_fail() {
		final JButtonFixture buttonFixture = window.button("nextTurnButton");
		assertNotNull(buttonFixture);
		buttonFixture.click();
		assertTrue(true);
	}

	@Test
	public void test_load_file() {
//		JMenuItemFixture loadGame = window.menuItem("");
		final JButtonFixture buttonFixture = window.button("nextTurnButton");
		assertNotNull(buttonFixture);
		buttonFixture.click();
		assertTrue(true);
	}

	// @BeforeClass
	// public static void setUpOnce() {
	// FailOnThreadViolationRepaintManager.install();
	// }

	private void cleanPreferences() {
		try {
			Preferences.userNodeForPackage(org.microcol.gui.GamePreferences.class).clear();
		} catch (BackingStoreException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

	@Before
	public void setUp() {
		cleanPreferences();
		MainFrameView frame = GuiActionRunner.execute(() -> {
			final Injector injector = Guice.createInjector(Stage.PRODUCTION, new MicroColModule());
			final MainFrameView mainFrame = injector.getInstance(MainFrameView.class);
			final GameController gameController = injector.getInstance(GameController.class);
			mainFrame.setVisible(true);
			gameController.startNewGame();
			return mainFrame;
		});
		window = new FrameFixture(frame);
		/**
		 * Following code again call pack. Game is running and there are world
		 * map which could be huge. Another pack call resize window to size much
		 * more bigger than screen really is.
		 */
		// window.show(); // shows the frame to test
	}

	@After
	public void tearDown() {
		window.cleanUp();
	}

}
