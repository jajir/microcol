package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * When user finish all missions than should be able to start free game. Test
 * verify that it's possible.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_12_F11_start_free_game extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/F11-ship_to_next_unit.microcol");

    TC_12_F11_start_free_game() {
	super("src/test/scenarios-finished/");
    }

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_start_free_game() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).startFreeGame();

	// Perform next turn and ignore turn events dialog. When it's possible new game
	// was loaded.
	gamePage.nextTurnAndCloseDialogs();

    }

}
