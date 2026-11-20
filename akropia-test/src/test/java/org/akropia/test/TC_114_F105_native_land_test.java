package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Direction;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Founder;
import org.akropia.model.Location;
import org.akropia.model.NativeLandStatus;
import org.akropia.model.NativeNation;
import org.akropia.model.unit.UnitActionType;
import org.akropia.page.ClearForestPage;
import org.akropia.page.ColonyNameDialog;
import org.akropia.page.ColonyScreen;
import org.akropia.page.CongressPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.NativeLandPage;
import org.akropia.page.NativeRelationsPage;
import org.akropia.page.PlowPage;
import org.akropia.page.RoadPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Native land claims of the Aztec capital next to the Dutch colony. The Dutch
 * start with 300 gold and paid rights on their colony square, its worked south
 * field and the six free squares around (22, 16). A pioneer stands on the
 * unpaid open grassland at (22, 16) and another on the unpaid forest at (24,
 * 15). Unpaid squares of the capital cost 200 gold.
 *
 * <p>
 * Every Dutch update adds 3.0 of local alarm from the surroundings: 0.25 for
 * each of the two colonists in New Amsterdam and the two pioneers, and 2 for
 * the armed frigate two squares away. The first unpaid use of a claimed square
 * adds 5 at once, and each unpaid square in use adds 2 at every later update;
 * with Minuit the square is waived and adds neither.
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_114_F105_native_land_test extends AbstractAkropiaTest {

    /** Aztec capital whose claim covers every square used here. */
    private static final Location AZTEC_CAPITAL = Location.of(23, 15);

    /**
     * Unpaid open grassland where the first pioneer stands; also the site of
     * the founding scenario.
     */
    private static final Location PLOW_SQUARE = Location.of(22, 16);

    /**
     * Unpaid forest where the second pioneer stands; also the field north of
     * New Amsterdam.
     */
    private static final Location FOREST_SQUARE = Location.of(24, 15);

    /** Square of New Amsterdam: claimed, and already paid by the Dutch. */
    private static final Location COLONY_SQUARE = Location.of(24, 16);

    /** Claimed by the Cherokee capital, which the Dutch have never seen. */
    private static final Location CHEROKEE_SQUARE = Location.of(31, 17);

    /** New Amsterdam's worker that stands on the capital's own square. */
    private static final int FIELD_WORKER_ID = 0;

    private static final String CLAIMANT = "This square is claimed by: Egyptians city, capital.";

    private static final String CONSEQUENCE = "Purchased land remains yours even if relations change.";

    private static final String CANNOT_PAY = "The land cannot be paid for now: not enough gold, or the offer has changed.";

    private static final String FOUNDING_LAND = "Colony square: Claimed land of Egyptians city, capital: not paid, 200 gold.\n"
            + "First field: Claimed land of Egyptians city, capital: paid.";

    private static final String FOUNDING_NOTE = "Nothing is paid now. The price is paid only when you confirm the colony name; canceling then pays nothing.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F105-native-land.akropia"));
    }

    @Test
    @Tag("local")
    void TC_06_friendly_pioneer_claims_land_without_payment() throws Exception {
        // Start on friendly claimed land, before any movement acquires it.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final NativeLandPage land = new NativeLandPage(getContext());
        land.loadFriendlyFixture(
                new File("src/test/scenarios/F105-native-land.akropia"));
        final PlowPage plow = new PlowPage(getContext());
        // Starting work grants ownership immediately without a purchase dialog.
        game.selectTile(PLOW_SQUARE);
        plow.useControl();
        plow.verifyUnitWork(PLOW_SQUARE, 0, UnitActionType.plowField, 0, 80);
        land.verifyLandOwner(PLOW_SQUARE, "Dutch");
        land.verifyPaidSquares(8);
        land.verifyDutchGold(300);
        // Continue preserves the title and the unfinished work.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        land.verifyLandOwner(PLOW_SQUARE, "Dutch");
        plow.verifyUnitWork(PLOW_SQUARE, 0, UnitActionType.plowField, 0, 80);
    }

    @Test
    @Tag("local")
    void TC_01_cancel_pay_and_insufficient_funds_before_pioneer_work()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        PlowPage plow = new PlowPage(getContext());
        ClearForestPage forest = new ClearForestPage(getContext());
        NativeLandPage land = new NativeLandPage(getContext());
        NativeRelationsPage relations = new NativeRelationsPage(getContext());

        // The map states who claims a square and whether the Dutch paid. The
        // colony square is paid; the pioneers' squares are not.
        game.moveMouseAtLocation(PLOW_SQUARE);
        game.verifyThatStatusBarContains(
                "Claimed land of Egyptians city, capital: not paid, 200 gold.");
        game.moveMouseAtLocation(COLONY_SQUARE);
        game.verifyThatStatusBarContains(
                "Claimed land of Egyptians city, capital: paid.");
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.UNPAID);
        land.verifyLandPrice(PLOW_SQUARE, 200);
        land.verifyLandStatus(COLONY_SQUARE, NativeLandStatus.PAID);
        land.verifyPaidSquares(8);
        land.verifyTakenSquares(0);

        // A claim whose owner was never seen reveals nothing.
        land.verifyNoLandInformation(CHEROKEE_SQUARE);

        // Plowing the unpaid square first shows the claimant, the price and
        // the unpaid consequence before anything is committed.
        game.selectTile(PLOW_SQUARE);
        plow.useControl();
        land.verifyDialogShows(CLAIMANT, "Price: 200 gold. You have 300 gold.",
                CONSEQUENCE);
        new org.akropia.page.PaperButtonsPage(getContext()).verifyModalActions(
                "nativeLandConfirm", "nativeLandCancel", "nativeLandConfirm");
        land.verifyPayEnabled(true);

        // Canceling starts no work and changes neither side's gold or alarm.
        land.cancel();
        land.verifyDialogClosed();
        plow.verifyUnitWork(PLOW_SQUARE, 0, UnitActionType.noAction, 1, 80);
        land.verifyDutchGold(300);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2000);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.UNPAID);
        land.verifyPaidSquares(8);
        land.verifyTakenSquares(0);

        // With enough gold, paying moves 200 to the capital, records the
        // right and starts the work. Paying is not an unpaid taking.
        plow.useControl();
        land.pay();
        land.verifyDialogClosed();
        plow.verifyUnitWork(PLOW_SQUARE, 0, UnitActionType.plowField, 0, 80);
        land.verifyDutchGold(100);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2200);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.PAID);
        land.verifyPaidSquares(9);
        land.verifyTakenSquares(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 0);
        game.moveMouseAtLocation(PLOW_SQUARE);
        game.verifyThatStatusBarContains(
                "Claimed land of Egyptians city, capital: paid.");

        // Clearing the forest first asks its own question, then the land one.
        // With only 100 gold the 200 cannot be paid: paying is disabled and
        // says why, yet continuing unpaid and canceling stay available.
        game.selectTile(FOREST_SQUARE);
        forest.useControl();
        forest.confirm();
        land.verifyDialogShows(CLAIMANT, "Price: 200 gold. You have 100 gold.",
                CONSEQUENCE);
        land.verifyPayEnabled(false);
        land.verifyDialogMessage(CANNOT_PAY);

        // Canceling leaves the pioneer idle and every amount unchanged.
        land.cancel();
        land.verifyDialogClosed();
        forest.verifyUnitWork(FOREST_SQUARE, 0, UnitActionType.noAction, 1, 80);
        land.verifyDutchGold(100);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2200);
        land.verifyPaidSquares(9);

        // Continue keeps the purchase and the canceled forest action.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        land.verifyDutchGold(100);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2200);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.PAID);
        land.verifyLandStatus(FOREST_SQUARE, NativeLandStatus.UNPAID);
        land.verifyPaidSquares(9);
        land.verifyTakenSquares(0);
        plow.verifyUnitWork(PLOW_SQUARE, 0, UnitActionType.plowField, 0, 80);
        plow.verifyRemainingUpdates(PLOW_SQUARE, 0, 4);
        forest.verifyUnitWork(FOREST_SQUARE, 0, UnitActionType.noAction, 1, 80);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 0);
    }

    @Test
    @Tag("local")
    void TC_04_founding_previews_its_land_and_pays_only_on_confirm()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        NativeLandPage land = new NativeLandPage(getContext());
        NativeRelationsPage relations = new NativeRelationsPage(getContext());

        // The pioneer on the unpaid grassland founds a colony. Before naming,
        // the land question lists every claimed square the founding would
        // use: the unpaid colony square and the paid first field.
        game.selectTile(PLOW_SQUARE);
        ColonyNameDialog naming = game.foundColony();
        land.verifyDialogShows(FOUNDING_LAND,
                "Price: 200 gold. You have 300 gold.", CONSEQUENCE);
        land.verifyFoundingNote(FOUNDING_NOTE);
        land.verifyPayEnabled(true);

        // Canceling founds nothing, opens no naming and takes no land.
        land.cancel();
        land.verifyDialogClosed();
        land.verifyColonyNameDialogClosed();
        land.verifyColonyAt(PLOW_SQUARE, false);
        land.verifyDutchGold(300);
        land.verifyPaidSquares(8);
        land.verifyTakenSquares(0);

        // Continuing unpaid opens naming, which repeats the land and the
        // decision. Canceling the name still uses no land at all.
        naming = game.foundColony();
        land.pay();
        land.verifyColonyNameLand(FOUNDING_LAND
                + "\n200 gold for native land will be paid when the colony is founded.");
        land.verifyDutchGold(300);
        naming.cancel();
        land.verifyColonyAt(PLOW_SQUARE, false);
        land.verifyDutchGold(300);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2000);
        land.verifyPaidSquares(8);
        land.verifyTakenSquares(0);

        // Paying and confirming founds the colony and buys the colony square
        // in one step; nothing is taken unpaid.
        naming = game.foundColony();
        land.pay();
        naming.enterName("Fort Orange");
        naming.save();
        land.verifyColonyAt(PLOW_SQUARE, true);
        land.verifyDutchGold(100);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2200);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.PAID);
        land.verifyPaidSquares(9);
        land.verifyTakenSquares(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 0);

        // Back on the map, the next update adds only the surroundings, 3.0:
        // the founder is now a resident inside the new colony.
        game = ColonyScreen.of(getContext(), "Fort Orange").close();
        game.nextTurnAndCloseDialogs();
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 0);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0);

        // Continue keeps the colony, the payment and the values.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        land.verifyColonyAt(PLOW_SQUARE, true);
        land.verifyDutchGold(100);
        land.verifyPaidSquares(9);
        land.verifyTakenSquares(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 0);
    }

    @Test
    @Tag("local")
    void TC_05_field_purchase_also_allows_road_work_and_survives_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final NativeLandPage land = new NativeLandPage(getContext());
        final RoadPage road = new RoadPage(getContext());
        // Cancellation keeps the field worker and gold unchanged.
        final ColonyScreen colony = game.openColonyAt(COLONY_SQUARE,
                "New Amsterdam");
        colony.dragFieldWorkerTo(FIELD_WORKER_ID, Direction.north);
        land.verifyDialogShows(CLAIMANT, "Price: 200 gold. You have 300 gold.",
                CONSEQUENCE);
        land.cancel();
        land.verifyWorkerOnSquare(FIELD_WORKER_ID, AZTEC_CAPITAL);
        land.verifyDutchGold(300);
        // Purchasing authorizes work permanently for this player.
        colony.dragFieldWorkerTo(FIELD_WORKER_ID, Direction.north);
        land.pay();
        land.verifyDialogClosed();
        land.verifyWorkerOnSquare(FIELD_WORKER_ID, FOREST_SQUARE);
        land.verifyDutchGold(100);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2200);
        land.verifyLandStatus(FOREST_SQUARE, NativeLandStatus.PAID);
        colony.close();
        // The pioneer uses the same purchased title without paying again.
        game.selectTile(FOREST_SQUARE);
        road.captureUnit(FOREST_SQUARE);
        road.useControl();
        land.verifyDialogClosed();
        road.verifyUnitWork(FOREST_SQUARE, UnitActionType.buildRoad, 0, 80);
        land.verifyDutchGold(100);
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        land.verifyLandStatus(FOREST_SQUARE, NativeLandStatus.PAID);
        land.verifyWorkerOnSquare(FIELD_WORKER_ID, FOREST_SQUARE);
        land.verifyDutchGold(100);
    }
}
