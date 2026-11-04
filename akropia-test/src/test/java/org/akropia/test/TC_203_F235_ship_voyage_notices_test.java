package org.akropia.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.page.GamePage;
import org.akropia.page.ScreenTurnReport;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_203_F235_ship_voyage_notices_test extends AbstractAkropiaTest {
    private static final File SOURCE = new File(
            "src/test/scenarios/F108-trade-route-cargo.akropia");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), SOURCE);
    }

    @Test
    @Tag("local")
    void TC_01_automatic_arrival_is_silent() throws Exception {
        // The assigned ship is one ordinary voyage update from the home port.
        ShipVoyageNoticeFixture.prepare(getContext(), SOURCE, true);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.nextTurnAndCloseDialogs();
        // Its automatic visit never asks the player to handle an arrival.
        final ScreenTurnReport report = game.openTurnReport();
        report.verifyCompletedHomeVoyage(3, true);
        report.verifyEventMessageCount("arrived", 0);
        report.verifyEventMessageCount("ship has", 0);
        report.close();
    }

    @Test
    @Tag("local")
    void TC_02_manual_arrival_reports_the_ship_once() throws Exception {
        // A manual voyage has the same remaining travel time.
        ShipVoyageNoticeFixture.prepare(getContext(), SOURCE, false);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.nextTurnAndCloseDialogs();
        final ScreenTurnReport report = game.openTurnReport();
        report.verifyCompletedHomeVoyage(3, false);
        report.verifyEventMessageCount("#3 arrived", 1);
        report.close();
        // Opening the report again reads the same result without generating a
        // duplicate.
        final ScreenTurnReport reopened = game.openTurnReport();
        reopened.verifyEventMessageCount("#3 arrived", 1);
        reopened.close();
    }
}
