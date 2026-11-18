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
        report.verifyOpenEuropeAction("Open Home port");
        report.verifyNoticeIcon("#3 arrived", "turnEventUnitIcon-GALLEON");
        report.openEuropeWithMouse().close();
        // Opening the report again reads the same result without generating a
        // duplicate.
        final ScreenTurnReport reopened = game.openTurnReport();
        reopened.verifyEventMessageCount("#3 arrived", 1);
        reopened.close();
    }

    @Test
    @Tag("local")
    void TC_03_colony_notices_keep_links_after_renaming_and_loading()
            throws Exception {
        // Saved arrival, repair and famine notices retain the original colony
        // identity.
        ShipVoyageNoticeFixture.prepareColonyNotices(getContext(), SOURCE);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ScreenTurnReport report = game.openTurnReport();
        report.verifyColonyNoticeActions(0, 3);
        report.verifyNoticeIcon("Famine", "turnEventColonyIcon");
        report.captureDialog("turnEventColonyIcon",
                "turn-report-colony-icons.png");
        report.verifyOpenColonyAction(0, "Open colony");
        // The historical name never redirects the action to a different colony.
        report.openColonyWithMouse(0, "Renamed port").closeWithEscape();
        final ScreenTurnReport reopened = game.openTurnReport();
        reopened.verifyColonyNoticeActions(0, 3);
        reopened.close();
    }

    @Test
    @Tag("local")
    void TC_04_return_arrival_has_ship_icon_and_map_link() throws Exception {
        // Load a manual return voyage with one turn remaining.
        ShipVoyageNoticeFixture.prepare(getContext(), SOURCE, false, false);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.nextTurnAndCloseDialogs();
        final ScreenTurnReport report = game.openTurnReport();
        final org.akropia.model.Location arrival = org.akropia.model.Location
                .of(50, 14);

        // The recorded arrival offers its ship and an accessible map action.
        report.verifyEventMessageCount("#3 arrived", 1);
        report.verifyNoticeIcon("#3 arrived", "turnEventUnitIcon-GALLEON");
        report.verifyMapAction(arrival, "Show on map");
        report.captureDialog("turnEventShowOnMap-50-14",
                "turn-report-arrival-icons.png");
        final GamePage map = report.showOnMap(arrival);
        map.verifyThatSelectedTileIs(arrival);
        map.verifyThatTileIsVisible(arrival);

        // Reopening the report retains the same notice and navigation.
        final ScreenTurnReport reopened = map.openTurnReport();
        reopened.verifyEventMessageCount("#3 arrived", 1);
        reopened.verifyMapAction(arrival, "Show on map");
        reopened.close();
    }
}
