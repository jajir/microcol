package org.microcol.test;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.ExpertPurchasePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.LaborReportPage;
import org.microcol.page.PassengerTransferPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The labor report (R-0198) of the Dutch nation counts its free colonists by
 * type and shows each person's real place. On the reference fixture two
 * colonists work the New Amsterdam fields, one passenger stands aboard the
 * galleon on the map, and two colonists stand on the European pier. Show opens
 * the colony, the map tile or Europe; Escape closes the report without any
 * change. A master blacksmith hired in Europe appears on the pier at once and
 * still after Save and Continue.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_166_F199_labor_report_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    /** Colonists working the New Amsterdam fields. */
    private static final int FIELD_WORKER_ID = 0;

    /** The passenger in the hold of the galleon on the map. */
    private static final int GALLEON_PASSENGER_ID = 4;

    /** Colonists standing on the European pier. */
    private static final int PIER_COLONIST_ID = 100;

    /** The galleon with the passenger is on this map tile. */
    private static final Location GALLEON_TILE = Location.of(25, 16);

    /** The specialist hired in Europe is on the pier with this unit ID. */
    private static final int HIRED_ID = 103;

    /** The galleon with the passenger, and the galleon in the European port. */
    private static final int GALLEON_ID = 3;
    private static final int EUROPE_SHIP_ID = 102;

    /**
     * The first free hold of the galleon in the European port; its second hold
     * carries ore in the fixture.
     */
    private static final int FREE_HOLD = 0;

    /** A pier colonist that the Crown takes while the report stays open. */
    private static final int LOST_ID = 101;

    private static final String PERSON_UNAVAILABLE = "This person is no longer one of your people or no longer exists. The list was refreshed.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_labor_report_places_navigation_cancel_and_continue()
            throws Exception {
        // The report opens from the statistics with five free colonists, one
        // row for their type and each person's real place.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final LaborReportPage labor = new LaborReportPage(getContext());
        labor.rememberMoves(PIER_COLONIST_ID);
        labor.open();
        labor.verifyTabSelected();
        labor.verifyTotal(5);
        labor.verifyTypeRow("COLONIST", "Free Colonist: 5");
        labor.verifyTypeSelected("COLONIST", true);
        labor.verifyTypeAbsent("MASTER_BLACKSMITH");
        labor.verifyPlaceContains(FIELD_WORKER_ID,
                "Colony New Amsterdam, field producing");
        labor.verifyPlaceContains(GALLEON_PASSENGER_ID,
                "Aboard Galleon (ID " + GALLEON_ID + "): Map tile 25, 16");
        labor.verifyPlaceContains(PIER_COLONIST_ID, "European port or pier");
        labor.verifyShowEnabled(FIELD_WORKER_ID, true);
        labor.verifyShowEnabled(GALLEON_PASSENGER_ID, true);
        labor.verifyShowEnabled(PIER_COLONIST_ID, true);

        // Show on a field worker opens its colony; leaving the colony returns
        // to
        // the map.
        labor.show(FIELD_WORKER_ID);
        final ColonyScreen colony = ColonyScreen.of(getContext(),
                "New Amsterdam");
        game = colony.closeWithEscape();

        // Show on the passenger aboard the galleon selects that map tile.
        labor.open();
        labor.show(GALLEON_PASSENGER_ID);
        game = GamePage.of(getContext());
        game.verifyThatSelectedTileIs(GALLEON_TILE);

        // Show on a pier colonist opens Europe; leaving Europe returns to the
        // map.
        labor.open();
        labor.show(PIER_COLONIST_ID);
        final EuropePortScreen europe = EuropePortScreen.of(getContext());
        game = europe.close();

        // The labor tab is reached by keyboard, and Escape closes the report
        // without changing the nation.
        labor.openWithKeyboard();
        labor.verifyTabSelectedAndFocused();
        game = labor.closeWithEscape();
        labor.verifyMovesUnchanged(PIER_COLONIST_ID);

        // The player hires a master blacksmith in Europe. The report then lists
        // him as a new row on the pier.
        final EuropePortScreen port = game.openEuroperPort();
        final ExpertPurchasePage purchase = new ExpertPurchasePage(
                getContext());
        port.openBuyUnitScreen();
        purchase.verifyOffer(true, "");
        purchase.confirm();
        purchase.verifyTreasuryAndPierExperts(2200, List.of(HIRED_ID));
        game = port.close();

        labor.open();
        labor.verifyTotal(6);
        labor.verifyTypeRow("COLONIST", "Free Colonist: 5");
        labor.verifyTypeRow("MASTER_BLACKSMITH", "Master Blacksmith: 1");
        labor.selectType("MASTER_BLACKSMITH");
        labor.verifyTypeSelected("MASTER_BLACKSMITH", true);
        labor.verifyPlaceContains(HIRED_ID, "European port or pier");
        labor.verifyShowEnabled(HIRED_ID, true);
        game = labor.closeWithEscape();

        // A real boarding moves a pier colonist into the free hold of the
        // galleon
        // in port. The colonist total does not change; the place names the
        // galleon.
        final EuropePortScreen boarding = game.openEuroperPort();
        new PassengerTransferPage(getContext()).embarkInPort(PIER_COLONIST_ID,
                FREE_HOLD, true);
        game = boarding.close();
        labor.rememberMoves(PIER_COLONIST_ID);
        labor.open();
        labor.verifyTotal(6);
        labor.selectType("COLONIST");
        labor.verifyTypeRow("COLONIST", "Free Colonist: 5");
        labor.verifyPlaceContains(PIER_COLONIST_ID, "Aboard Galleon (ID "
                + EUROPE_SHIP_ID + "): European port or pier");
        labor.verifyShowEnabled(PIER_COLONIST_ID, true);

        // Show on the boarded colonist opens Europe, where the galleon is.
        labor.show(PIER_COLONIST_ID);
        game = EuropePortScreen.of(getContext()).close();
        labor.open();
        labor.verifyMovesUnchanged(PIER_COLONIST_ID);
        labor.verifyTotal(6);
        labor.closeWithEscape();

        // A colonist taken by the Crown while the report stays open is refused
        // when Show is pressed. The list is rebuilt without him, and the nation
        // learns nothing else about him.
        labor.open();
        labor.verifyPlaceContains(LOST_ID, "European port or pier");
        labor.loseToCrownWhileOpen(LOST_ID);
        labor.show(LOST_ID);
        labor.verifyFeedback(PERSON_UNAVAILABLE);
        labor.verifyPersonRowAbsent(LOST_ID);
        labor.verifyTotal(5);
        labor.verifyTypeRow("COLONIST", "Free Colonist: 4");
        game = labor.closeWithEscape();

        // Save and Continue keep the boarded colonist aboard, the lost colonist
        // gone and the hired specialist on the pier.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        labor.open();
        labor.verifyTotal(5);
        labor.verifyTypeRow("COLONIST", "Free Colonist: 4");
        labor.verifyTypeRow("MASTER_BLACKSMITH", "Master Blacksmith: 1");
        labor.selectType("MASTER_BLACKSMITH");
        labor.verifyPlaceContains(HIRED_ID, "European port or pier");
        labor.selectType("COLONIST");
        labor.verifyPlaceContains(PIER_COLONIST_ID, "Aboard Galleon (ID "
                + EUROPE_SHIP_ID + "): European port or pier");
        labor.verifyPersonRowAbsent(LOST_ID);
        labor.closeWithEscape();
    }
}
