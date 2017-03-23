package org.microcol.gui;

import static org.junit.Assert.assertNotNull;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.event.GameController;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class IntegrationTest {

	private FrameFixture window;

	@Test
	public void test_that_next_turn_button_is_on_screen() {
		final JButtonFixture buttonFixture = window.button("nextTurnButton");
		assertNotNull(buttonFixture);
	}

	// @BeforeClass
	// public static void setUpOnce() {
	// FailOnThreadViolationRepaintManager.install();
	// }

	@Before
	public void setUp() {
		MainFrameView frame = GuiActionRunner.execute(() -> {
			final Injector injector = Guice.createInjector(Stage.PRODUCTION, new MicroColModule());
			final MainFrameView mainFrame = injector.getInstance(MainFrameView.class);
			final GameController gameController = injector.getInstance(GameController.class);
			mainFrame.setVisible(true);
			gameController.newGame();
			return mainFrame;
		});
		window = new FrameFixture(frame);
		window.show(); // shows the frame to test
	}

	@After
	public void tearDown() {
		window.cleanUp();
	}
}
