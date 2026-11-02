package org.akropia.test;

import java.io.File;
import org.akropia.page.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** R-0398, UC-005/038: review idle colonists before ending a turn. */
@ExtendWith(ApplicationExtension.class)
public class TC_199_F52_end_turn_warnings_test extends AbstractAkropiaTest {
    private static final File SOURCE = new File(
            "src/test/scenarios/F52-fortification.akropia");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), SOURCE);
    }

    @Test
    @Tag("local")
    void TC_01_back_escape_and_open_colony_do_not_end_turn() throws Exception {
        // An exhausted colonist waits outside the colony buildings.
        GoToFixture.prepare(getContext(), SOURCE, "F52-idle-warning.akropia",
                GoToFixture.COLONY, 0);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final String before = warning.snapshot();
        // Back and Escape retain the entire model and leave commands usable.
        warning.requestTurn();
        warning.verifyWarning(0, "New Amsterdam (1)");
        warning.back();
        warning.verifyUnchanged(before);
        warning.verifyClosed();
        warning.requestTurn();
        warning.escape();
        warning.verifyUnchanged(before);
        warning.verifyClosed();
        // The colony link navigates without assigning work or advancing time.
        warning.requestTurn();
        warning.openColony(0);
        final ColonyScreen colony = ColonyScreen.of(getContext(),
                "New Amsterdam");
        warning.verifyUnchanged(before);
        colony.close();
        warning.verifyClosed();
    }

    @Test
    @Tag("local")
    void TC_02_continue_is_for_one_turn_and_reload_recomputes_warning()
            throws Exception {
        GoToFixture.prepare(getContext(), SOURCE, "F52-idle-repeat.akropia",
                GoToFixture.COLONY, 0);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final int before = warning.turn();
        // The common journey explicitly chooses End turn anyway.
        game.nextTurnAndCloseDialogs();
        warning.verifyTurn(before + 1);
        warning.requestTurn();
        warning.verifyWarning(0, "New Amsterdam (1)");
        warning.back();
        // Continue reloads the position; no saved acknowledgement suppresses
        // it.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        warning.requestTurn();
        warning.verifyWarning(0, "New Amsterdam (1)");
        warning.back();
        warning.verifyTurn(before + 1);
    }

    @Test
    @Tag("local")
    void TC_03_workers_and_passengers_do_not_block_turn() throws Exception {
        // The original fixture has workers and a passenger, but no idle person
        // on a colony square.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final int before = warning.turn();
        game.nextTurnAndCloseDialogs();
        warning.verifyTurn(before + 1);
        warning.verifyClosed();
    }
}
