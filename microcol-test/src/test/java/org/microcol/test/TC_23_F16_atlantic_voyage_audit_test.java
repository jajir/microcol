package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AtlanticAuditPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Verify baseline transit and persistence. Immediate lane departure and return
 * away from the origin are observations of gaps, not desired rules.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_23_F16_atlantic_voyage_audit_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F16-atlantic-audit.microcol");
    private static final Location START = Location.of(25, 16);
    private static final Location LANE = Location.of(26, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        Files.copy(FIXTURE.toPath(),
                Path.of(System.getProperty("user.home"), ".microcol",
                        "autosave.microcol"),
                StandardCopyOption.REPLACE_EXISTING);
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_02_menu_preserves_latest_departure() throws Exception {
        // This profile starts with a known autosave, making Continue available.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        byte[] saved = audit.readAutosave();
        game.moveMouseAtLocation(START);
        game.dragMouseAtLocation(LANE);
        new AtlanticAuditPage(getContext()).verifyVoyage(3, true);

        // Menu entry saves departure; Continue restores the same voyage and
        // cargo.
        game.exitToMenu().verifyMainScreen();
        audit.verifyAutosaveChanged(saved);
        audit.verifyContinueButtonEnabled(true);
        game = audit.continueGame();
        game.verifyNumberOfUnitsAt(START, 0);
        new AtlanticAuditPage(getContext()).verifyVoyage(3, true);
        new AtlanticAuditPage(getContext()).verifyCargo();
    }

    @Test
    @Tag("local")
    void TC_01_atlantic_round_trip_preserves_cargo() throws Exception {
        // Enter an adjacent sea lane; the baseline immediately starts transit.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        AtlanticAuditPage audit = new AtlanticAuditPage(getContext());
        audit.verifyCargo();
        game.moveMouseAtLocation(START);
        game.dragMouseAtLocation(LANE);
        audit.verifyVoyage(3, true);
        audit.verifyCargo();

        // Continue from the automatic turn snapshot while the ship is at sea.
        game.nextTurnAndCloseDialogs();
        audit.verifyVoyage(2, true);
        game.exitToMenu().verifyMainScreen();
        game = new FreePlayAuditPage(getContext()).continueGame();
        audit.verifyVoyage(2, true);
        audit.verifyCargo();

        // The saved voyage completes after the remaining two turns.
        game.nextTurnAndCloseDialogs();
        audit.verifyVoyage(1, true);
        game.nextTurnAndCloseDialogs();
        audit.verifyInEurope();
        audit.verifyCargo();

        // Drag the same ship from Europe toward the New World.
        EuropePortScreen europe = game.openEuroperPort();
        europe.verifyNumberOfShipsInPort(1);
        europe.selectUnitFromPort(0);
        audit.sendShipBack();
        audit.verifyVoyage(3, false);
        game = europe.close();
        game.nextTurnAndCloseDialogs();
        audit.verifyVoyage(2, false);
        game.nextTurnAndCloseDialogs();
        audit.verifyVoyage(1, false);
        game.nextTurnAndCloseDialogs();

        // Baseline uses another eastern arrival, but preserves the cargo.
        audit.verifyReturnedAwayFrom(LANE);
        audit.verifyCargo();
    }
}
