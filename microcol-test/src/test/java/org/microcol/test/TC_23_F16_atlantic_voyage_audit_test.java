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
 * Verify outward lane departure, exact return and transit persistence.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_23_F16_atlantic_voyage_audit_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F16-atlantic-audit.microcol");
    private static final Location START = Location.of(25, 16);
    private static final Location LANE = Location.of(26, 16);
    private static final Location OUTWARD = Location.of(27, 16);

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
        new AtlanticAuditPage(getContext()).verifyShipMapPainting(true);
        game.moveMouseAtLocation(START);
        game.dragMouseAtLocation(LANE);
        new AtlanticAuditPage(getContext()).verifyReturnedTo(LANE);
        game.moveMouseAtLocation(LANE);
        game.dragMouseAtLocation(OUTWARD);
        final AtlanticAuditPage voyage = new AtlanticAuditPage(getContext());
        final int duration = voyage.verifyVoyageSample(true, false);
        final int weather = voyage.getVoyageWeather();
        voyage.verifyShipMapPainting(false);

        // Menu entry saves departure; Continue restores the same voyage and
        // cargo.
        game.exitToMenu().verifyMainScreen();
        audit.verifyAutosaveChanged(saved);
        audit.verifyContinueButtonEnabled(true);
        game = audit.continueGame();
        game.verifyNumberOfUnitsAt(START, 0);
        voyage.verifyVoyageMetadata(weather, duration, duration, true);
        voyage.verifyCargo();
    }

    @Test
    @Tag("local")
    void TC_01_atlantic_round_trip_preserves_cargo() throws Exception {
        // Enter the lane first, then take a separate outward step to depart.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        AtlanticAuditPage audit = new AtlanticAuditPage(getContext());
        audit.verifyCargo();
        game.moveMouseAtLocation(START);
        game.dragMouseAtLocation(LANE);
        audit.verifyReturnedTo(LANE);
        audit.verifyCargo();
        game.moveMouseAtLocation(LANE);
        game.dragMouseAtLocation(OUTWARD);
        final int outwardDuration = audit.verifyVoyageSample(true, false);
        final int outwardWeather = audit.getVoyageWeather();
        audit.verifyCargo();

        // Continue at departure preserves the exact sampled weather and time.
        game.exitToMenu().verifyMainScreen();
        game = new FreePlayAuditPage(getContext()).continueGame();
        audit.verifyVoyageMetadata(outwardWeather, outwardDuration,
                outwardDuration, true);
        audit.verifyCargo();

        // Advance only the sampled number of owner updates.
        for (int remaining = outwardDuration - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            if (remaining > 0) {
                audit.verifyVoyageMetadata(outwardWeather, outwardDuration,
                        remaining, true);
            }
        }
        audit.verifyInEurope();
        audit.verifyCargo();

        // Drag the same ship from Europe toward the New World.
        EuropePortScreen europe = game.openEuroperPort();
        europe.verifyNumberOfShipsInPort(1);
        europe.selectUnitFromPort(0);
        audit.sendShipBack();
        final int returnDuration = audit.verifyVoyageSample(false, false);
        final int returnWeather = audit.getVoyageWeather();
        game = europe.close();
        for (int remaining = returnDuration - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            if (remaining > 0) {
                audit.verifyVoyageMetadata(returnWeather, returnDuration,
                        remaining, false);
            }
        }

        // Return to the recorded source lane square with the same cargo.
        audit.verifyReturnedTo(LANE);
        audit.verifyCargo();
    }
}
