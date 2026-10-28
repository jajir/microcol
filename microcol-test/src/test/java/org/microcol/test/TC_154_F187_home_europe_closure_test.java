package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.HomeEuropePage;
import org.microcol.page.IndependencePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Declaring independence warns that home Europe closes, Cancel and Escape keep
 * the port and every crossing as they were, and a confirmed declaration closes
 * the port: hiring, purchases and the pier are unavailable, the Crown holds the
 * ship, goods and people in the port, the ship sailing to Europe turns back
 * with its cargo, and the ship sailing out is unchanged. Continue keeps all of
 * it.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_154_F187_home_europe_closure_test extends AbstractMicroColTest {

    /** The year 1747 of the shared independence scenario, as in TC_81. */
    private final static int YEAR_1747 = 700;

    private final static int END_YEAR = 1900;

    /** Eight residents in every colony, so 1600 liberty is full support. */
    private final static long FULL = 1600L;

    /** West sea-lane square where the merchantman started its crossing. */
    private final static Location LANE = Location.of(1, 17);

    private final static int INBOUND = IndependenceFixture.INBOUND_SHIP;
    private final static int INBOUND_PASSENGER = IndependenceFixture.INBOUND_PASSENGER;
    private final static int DOCKED = IndependenceFixture.PORT_SHIP;
    private final static int DOCKED_PASSENGER = IndependenceFixture.PORT_PASSENGER;
    private final static int PIER = IndependenceFixture.PIER_PERSON;
    private final static int OUTBOUND = IndependenceFixture.OUTBOUND_SHIP;

    private final static File testFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    private GamePage prepareAndLoad(final String name) throws Exception {
        IndependenceFixture.prepareWithHomeEurope(getContext(), testFileName,
                name, YEAR_1747, END_YEAR,
                new long[] { FULL, FULL, FULL, FULL });
        return WelcomePage.of(getContext()).loadGame();
    }

    @Test
    @Tag("local")
    void TC_01_warning_cancel_and_escape_keep_the_port_and_both_crossings()
            throws Exception {
        // Load full support with a ship sailing to Europe, a ship sailing out,
        // a ship and a person waiting in the port.
        GamePage gamePage = prepareAndLoad("home-europe-f154-warning.microcol");
        final IndependencePage independence = new IndependencePage(
                getContext());
        final HomeEuropePage home = new HomeEuropePage(getContext());
        EuropePortScreen europe = gamePage.openEuroperPort();
        home.selectVoyage(INBOUND);
        home.verifyVoyage(INBOUND, true, 1, 3, "Turns remaining: 3");
        home.verifyCrossingCargo(INBOUND, GoodsType.ORE, 7, INBOUND_PASSENGER);
        home.selectVoyage(OUTBOUND);
        home.verifyVoyage(OUTBOUND, false, 0, 2, "Turns remaining: 2");
        home.verifyShipInPort(DOCKED, DOCKED_PASSENGER, GoodsType.MUSKET, 40);
        home.verifyPersonOnPier(PIER);
        home.verifySeized(false, INBOUND, INBOUND_PASSENGER, DOCKED,
                DOCKED_PASSENGER, PIER, OUTBOUND);
        independence.verifyHomeEuropeOpen();
        home.openPierMenu(PIER);
        home.verifyPierMenuOpen(PIER);
        home.closePierMenu();
        gamePage = europe.close();

        // The confirmation warns that home Europe closes before anything runs.
        gamePage.declareIndependence();
        independence.rememberUnitCount();
        independence.verifyConfirmationShown(
                "National support for independence: 100%.",
                "No armed veteran will be mustered now. Only veterans standing in colonies with at least 50% local support are mustered.");
        independence.verifyEuropeWarning(
                "Your home Europe will be closed to you, and the Crown will send a royal expedition against you. Treasure cannot be redeemed in Europe or collected by the King after independence.");

        // Cancel changes nothing: the port and both crossings stay as saved.
        independence.cancel();
        independence.verifyConfirmationClosed();
        independence.verifyDeclared(false);
        independence.verifyUnitCountUnchanged();
        europe = gamePage.openEuroperPort();
        home.selectVoyage(INBOUND);
        home.verifyVoyage(INBOUND, true, 1, 3, "Turns remaining: 3");
        home.verifyCrossingCargo(INBOUND, GoodsType.ORE, 7, INBOUND_PASSENGER);
        home.selectVoyage(OUTBOUND);
        home.verifyVoyage(OUTBOUND, false, 0, 2, "Turns remaining: 2");
        home.verifyShipInPort(DOCKED, DOCKED_PASSENGER, GoodsType.MUSKET, 40);
        home.verifyPersonOnPier(PIER);
        home.verifySeized(false, INBOUND, INBOUND_PASSENGER, DOCKED,
                DOCKED_PASSENGER, PIER, OUTBOUND);
        independence.verifyHomeEuropeOpen();
        gamePage = europe.close();

        // Escape cancels the same way.
        gamePage.declareIndependence();
        independence.pressEscape();
        independence.verifyConfirmationClosed();
        independence.verifyDeclared(false);
        independence.verifyUnitCountUnchanged();
        europe = gamePage.openEuroperPort();
        home.selectVoyage(INBOUND);
        home.verifyVoyage(INBOUND, true, 1, 3, "Turns remaining: 3");
        home.selectVoyage(OUTBOUND);
        home.verifyVoyage(OUTBOUND, false, 0, 2, "Turns remaining: 2");
        home.verifySeized(false, INBOUND, INBOUND_PASSENGER, DOCKED,
                DOCKED_PASSENGER, PIER, OUTBOUND);
        independence.verifyHomeEuropeOpen();
        europe.close();
    }

    @Test
    @Tag("local")
    void TC_02_confirm_closes_the_port_reverses_the_inbound_ship_and_continue_keeps_it()
            throws Exception {
        // Load the same game and confirm the declaration.
        GamePage gamePage = prepareAndLoad("home-europe-f154-confirm.microcol");
        final IndependencePage independence = gamePage.declareIndependence();
        independence.confirm();
        independence.verifyConfirmationClosed();
        final DialogMessagePage dialogMessagePage = DialogMessagePage
                .of(getContext());
        dialogMessagePage.verifyReady();
        dialogMessagePage.close();
        dialogMessagePage.close();
        independence.verifyDeclared(true);
        independence.verifyDeclareButtonVisible(false);

        // The crossing to Europe turned back with its cargo, one update from
        // its coast; the crossing out is unchanged; no crossing can turn to
        // Europe.
        final HomeEuropePage home = new HomeEuropePage(getContext());
        EuropePortScreen europe = gamePage.openEuroperPort();
        home.selectVoyage(INBOUND);
        home.verifyVoyage(INBOUND, false, 1, 1, "Turns remaining: 1");
        home.verifyCrossingCargo(INBOUND, GoodsType.ORE, 7, INBOUND_PASSENGER);
        home.selectVoyage(OUTBOUND);
        home.verifyVoyage(OUTBOUND, false, 0, 2, "Turns remaining: 2");
        home.selectVoyage(INBOUND);
        home.verifyReverseControl(INBOUND, false);
        home.selectVoyage(OUTBOUND);
        home.verifyReverseControl(OUTBOUND, false);

        // Hiring and purchases are closed, and the Crown holds the ship, its
        // goods and passenger and the person on the pier.
        independence.verifyHomeEuropeClosed();
        home.verifyShipInPort(DOCKED, DOCKED_PASSENGER, GoodsType.MUSKET, 40);
        home.verifyPersonOnPier(PIER);
        home.verifySeized(true, DOCKED, DOCKED_PASSENGER, PIER);
        home.verifySeized(false, INBOUND, INBOUND_PASSENGER, OUTBOUND);
        home.openPierMenu(PIER);
        home.verifyPierMenuClosed(PIER);
        home.closePierMenu();
        gamePage = europe.close();

        // Continue neither reopens the port nor loses the reversed cargo.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        independence.verifyDeclared(true);
        europe = gamePage.openEuroperPort();
        home.selectVoyage(INBOUND);
        home.verifyVoyage(INBOUND, false, 1, 1, "Turns remaining: 1");
        home.verifyCrossingCargo(INBOUND, GoodsType.ORE, 7, INBOUND_PASSENGER);
        home.selectVoyage(OUTBOUND);
        home.verifyVoyage(OUTBOUND, false, 0, 2, "Turns remaining: 2");
        independence.verifyHomeEuropeClosed();
        home.verifyShipInPort(DOCKED, DOCKED_PASSENGER, GoodsType.MUSKET, 40);
        home.verifyPersonOnPier(PIER);
        home.verifySeized(true, DOCKED, DOCKED_PASSENGER, PIER);
        home.verifySeized(false, INBOUND, INBOUND_PASSENGER, OUTBOUND);
        europe.close();
    }

    @Test
    @Tag("local")
    void TC_03_reversed_ship_arrives_once_at_its_coast_while_the_port_stays_held()
            throws Exception {
        // Confirm the declaration and let one owner update pass.
        final GamePage gamePage = prepareAndLoad(
                "home-europe-f154-update.microcol");
        final IndependencePage independence = gamePage.declareIndependence();
        independence.confirm();
        final DialogMessagePage dialogMessagePage = DialogMessagePage
                .of(getContext());
        dialogMessagePage.verifyReady();
        dialogMessagePage.close();
        dialogMessagePage.close();
        gamePage.nextTurnAndCloseDialogs();

        // The reversed ship stands on the coast it left, with its cargo, and
        // the
        // ship sailing out is one update nearer its arrival.
        final HomeEuropePage home = new HomeEuropePage(getContext());
        final EuropePortScreen europe = gamePage.openEuroperPort();
        home.verifyArrivedAtSource(INBOUND, LANE, GoodsType.ORE, 7,
                INBOUND_PASSENGER);
        home.selectVoyage(OUTBOUND);
        home.verifyVoyage(OUTBOUND, false, 1, 1, "Turns remaining: 1");

        // The port is still held and closed.
        home.verifyShipInPort(DOCKED, DOCKED_PASSENGER, GoodsType.MUSKET, 40);
        home.verifyPersonOnPier(PIER);
        home.verifySeized(true, DOCKED, DOCKED_PASSENGER, PIER);
        independence.verifyHomeEuropeClosed();
        europe.close();

        // Sailing east stays on the map; sailing back west cannot reopen
        // Europe.
        final Location innerLane = Location.of(2, 17);
        gamePage.panToTile(LANE);
        gamePage.selectTile(LANE);
        gamePage.dragMouseAtLocation(innerLane);
        home.verifyArrivedAtSource(INBOUND, innerLane, GoodsType.ORE, 7,
                INBOUND_PASSENGER);
        gamePage.selectTile(innerLane);
        gamePage.dragMouseAtLocationWithFeedback(LANE,
                "Home Europe is closed after independence.");
        home.verifyArrivedAtSource(INBOUND, innerLane, GoodsType.ORE, 7,
                INBOUND_PASSENGER);
    }
}
