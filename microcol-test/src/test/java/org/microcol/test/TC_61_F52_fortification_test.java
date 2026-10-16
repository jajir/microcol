package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_61_F52_fortification_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F52-fortification.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_issue_inspect_and_cancel_without_refunding_movement()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FortifyPage fortify = new FortifyPage(getContext());
        // Issuing the order consumes this turn, without granting defense yet.
        game.selectTile(Location.of(24, 15));
        fortify.verifyAction("Fortify", true);
        fortify.changeOrderWithKeyboard();
        fortify.verifyOrder(104, true, false, 0);
        fortify.verifyAction("Cancel fortifying", true);
        // Inspecting another unit and returning preserves the pending order.
        game.selectTile(Location.of(25, 15));
        fortify.verifyAction("Fortify", false);
        game.selectTile(Location.of(24, 15));
        fortify.verifyOrder(104, true, false, 0);
        fortify.changeOrder();
        fortify.verifyOrder(104, false, false, 0);
        fortify.verifyAction("Fortify", false);
    }

    @Test
    @Tag("local")
    void TC_02_continue_preserves_each_stage_and_next_turn_activates_once()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FortifyPage fortify = new FortifyPage(getContext());
        game.selectTile(Location.of(24, 15));
        fortify.changeOrder();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        // Continue restores the completed turn, without activating
        // fortification.
        fortify.verifyOrder(104, true, false, 0);
        game.nextTurnAndCloseDialogs();
        game.selectTile(Location.of(24, 15));
        fortify.verifyOrder(104, true, true, 1);
        fortify.verifyAction("Cancel fortification", true);
        fortify.captureDialog("fortifyUnit", "r106-fortified-order.png");
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        game.selectTile(Location.of(24, 15));
        fortify.verifyOrder(104, true, true, 1);
        // A drag explains the persistent order without silently moving or
        // canceling it.
        game.moveMouseAtLocation(Location.of(24, 14));
        game.moveMouseAtLocation(Location.of(24, 15));
        game.dragMouseAtLocationWithFeedback(Location.of(24, 14),
                "Fortified: cancel to move or attack. No spent movement is restored.");
        fortify.verifyLocation(104, Location.of(24, 15));
        fortify.verifyOrder(104, true, true, 1);
        // Explicit cancellation preserves only the allowance already restored
        // this turn.
        fortify.changeOrderWithKeyboard();
        fortify.verifyOrder(104, false, false, 1);
        fortify.verifyAction("Fortify", true);
    }
}
