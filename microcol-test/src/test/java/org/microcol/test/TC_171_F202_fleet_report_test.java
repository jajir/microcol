package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FleetReportPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.PassengerTransferPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The fleet report (R-0202) of the Dutch nation lists its two galleons with the
 * real place, passengers by type, goods and holds of each. The galleon on the
 * map carries one colonist; the galleon in the European port carries seven ore.
 * Show opens the map tile or Europe; Escape closes the report without any
 * change. A colonist boarded through the real transfer refreshes the port
 * galleon at once and still after Save and Continue. The report is read in the
 * English and the Czech labels at 900x750.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_171_F202_fleet_report_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    /** The galleon on the map, with the colonist in its second hold. */
    private static final int MAP_GALLEON_ID = 3;

    /** The galleon in the European port, with ore in its second hold. */
    private static final int PORT_GALLEON_ID = 102;

    /** The colonist standing on the European pier. */
    private static final int PIER_COLONIST_ID = 100;

    /** The first free hold of the port galleon. */
    private static final int FREE_HOLD = 0;

    /** The map galleon stands on this tile. */
    private static final Location MAP_GALLEON_TILE = Location.of(25, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_fleet_report_cargo_navigation_cancel_and_continue()
            throws Exception {
        // The report opens from the statistics with two ships. Each shows its
        // real place, its passengers by type, its goods and its holds; neither
        // is on a voyage or under repair.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FleetReportPage fleet = new FleetReportPage(getContext());
        fleet.open();
        fleet.verifyTabSelected();
        fleet.verifyTotal("Ships of your nation: 2");
        fleet.verifyPlace(MAP_GALLEON_ID, "Map tile 25, 16");
        fleet.verifyCargo(MAP_GALLEON_ID,
                "Passengers: 1 (Free Colonist: 1); goods: none");
        fleet.verifyHolds(MAP_GALLEON_ID,
                "Occupied holds: 1 of 6, free holds: 5");
        fleet.verifyPlace(PORT_GALLEON_ID, "European port or pier");
        fleet.verifyCargo(PORT_GALLEON_ID, "Passengers: 0; goods: Ore 7");
        fleet.verifyHolds(PORT_GALLEON_ID,
                "Occupied holds: 1 of 6, free holds: 5");
        fleet.verifyVoyageAbsent(MAP_GALLEON_ID);
        fleet.verifyVoyageAbsent(PORT_GALLEON_ID);
        fleet.verifyRepairAbsent(MAP_GALLEON_ID);
        fleet.verifyRepairAbsent(PORT_GALLEON_ID);
        fleet.verifyShowEnabled(MAP_GALLEON_ID, true);
        fleet.verifyShowEnabled(PORT_GALLEON_ID, true);
        fleet.verifyOwnShipCargoInModel(MAP_GALLEON_ID, 1, 1);
        fleet.verifyOwnShipCargoInModel(PORT_GALLEON_ID, 0, 1);

        // Show on the map galleon selects its tile on the map.
        fleet.show(MAP_GALLEON_ID);
        game = GamePage.of(getContext());
        game.verifyThatSelectedTileIs(MAP_GALLEON_TILE);

        // Show on the port galleon opens Europe; leaving Europe returns to the
        // map.
        fleet.open();
        fleet.show(PORT_GALLEON_ID);
        game = EuropePortScreen.of(getContext()).close();

        // The fleet tab is reached by keyboard, and Escape closes the report
        // without changing any ship.
        fleet.openWithKeyboard();
        fleet.verifyTabSelectedAndFocused();
        game = fleet.closeWithEscape();
        fleet.open();
        fleet.verifyCargo(PORT_GALLEON_ID, "Passengers: 0; goods: Ore 7");
        fleet.verifyHolds(PORT_GALLEON_ID,
                "Occupied holds: 1 of 6, free holds: 5");
        game = fleet.closeWithEscape();

        // A real boarding moves the pier colonist into the free hold of the
        // port galleon. The report shows the new passenger and the hold count
        // at once, while the map galleon stays as it was.
        final EuropePortScreen boarding = game.openEuroperPort();
        new PassengerTransferPage(getContext()).embarkInPort(PIER_COLONIST_ID,
                FREE_HOLD, true);
        game = boarding.close();
        fleet.open();
        fleet.verifyTotal("Ships of your nation: 2");
        fleet.verifyCargo(PORT_GALLEON_ID,
                "Passengers: 1 (Free Colonist: 1); goods: Ore 7");
        fleet.verifyHolds(PORT_GALLEON_ID,
                "Occupied holds: 2 of 6, free holds: 4");
        fleet.verifyCargo(MAP_GALLEON_ID,
                "Passengers: 1 (Free Colonist: 1); goods: none");
        // The English labels fit the 900x750 window without cut-off sentences.
        fleet.resizeWindow(900, 750);
        fleet.verifyWindowSize(900, 750);
        fleet.verifyReportInsideWindow();
        fleet.verifySentencesInsideViewport(MAP_GALLEON_ID);
        fleet.verifySentencesInsideViewport(PORT_GALLEON_ID);
        fleet.captureDialog("fleet", "fleet-report-english.png");
        game = fleet.closeWithEscape();

        // Save and Continue keep the boarded colonist in the port galleon and
        // the map galleon unchanged.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        fleet.open();
        fleet.verifyCargo(PORT_GALLEON_ID,
                "Passengers: 1 (Free Colonist: 1); goods: Ore 7");
        fleet.verifyHolds(PORT_GALLEON_ID,
                "Occupied holds: 2 of 6, free holds: 4");
        fleet.verifyCargo(MAP_GALLEON_ID,
                "Passengers: 1 (Free Colonist: 1); goods: none");
        fleet.closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_02_czech_labels_fit_the_window_and_show_the_same_cargo()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();

        // The Czech labels name the same ships, places, passengers, goods and
        // holds as the English labels.
        final FleetReportPage fleet = new FleetReportPage(getContext());
        fleet.open();
        fleet.verifyTabSelected();
        fleet.verifyTotal("Lodí vašeho národa: 2");
        fleet.verifyPlace(MAP_GALLEON_ID, "Políčko mapy 25, 16");
        fleet.verifyCargo(MAP_GALLEON_ID,
                "Pasažéři: 1 (svobodný kolonista: 1); zboží: žádné");
        fleet.verifyHolds(MAP_GALLEON_ID,
                "Obsazené prostory: 1 z 6, volné prostory: 5");
        fleet.verifyPlace(PORT_GALLEON_ID, "Evropský přístav nebo molo");
        fleet.verifyCargo(PORT_GALLEON_ID,
                "Pasažéři: 0; zboží: Železná ruda 7");
        fleet.verifyHolds(PORT_GALLEON_ID,
                "Obsazené prostory: 1 z 6, volné prostory: 5");
        // The longer Czech sentences wrap in the 900x750 window.
        fleet.resizeWindow(900, 750);
        fleet.verifyWindowSize(900, 750);
        fleet.verifyReportInsideWindow();
        fleet.verifySentencesInsideViewport(MAP_GALLEON_ID);
        fleet.verifySentencesInsideViewport(PORT_GALLEON_ID);
        fleet.captureDialog("fleet", "fleet-report-czech.png");
        fleet.closeWithEscape();
    }
}
