package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Direction;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Founder;
import org.microcol.model.Location;
import org.microcol.model.NativeLandStatus;
import org.microcol.model.NativeNation;
import org.microcol.model.unit.UnitActionType;
import org.microcol.page.ClearForestPage;
import org.microcol.page.ColonyNameDialog;
import org.microcol.page.ColonyScreen;
import org.microcol.page.CongressPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeLandPage;
import org.microcol.page.NativeRelationsPage;
import org.microcol.page.PlowPage;
import org.microcol.page.RoadPage;
import org.microcol.page.WelcomePage;
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
public class TC_114_F105_native_land_test extends AbstractMicroColTest {

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

    private static final String CLAIMANT = "This square is claimed by: Aztec city, capital.";

    private static final String CONSEQUENCE = "If you use it without paying, the settlement's alarm toward you rises by 5 once at the first use and by 2 each turn while the square stays in use.";

    private static final String CANNOT_PAY = "The land cannot be paid for now: not enough gold, or the offer has changed.";

    private static final String FOUNDING_LAND = "Colony square: Claimed land of Aztec city, capital: not paid, 200 gold.\n"
            + "First field: Claimed land of Aztec city, capital: paid.";

    private static final String FOUNDING_NOTE = "Nothing is paid now. The price is paid only when you confirm the colony name; canceling then pays nothing.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F105-native-land.microcol"));
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
                "Claimed land of Aztec city, capital: not paid, 200 gold.");
        game.moveMouseAtLocation(COLONY_SQUARE);
        game.verifyThatStatusBarContains(
                "Claimed land of Aztec city, capital: paid.");
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
                "Claimed land of Aztec city, capital: paid.");

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

        // Knowingly continuing unpaid starts the work without any payment and
        // without the taking: that comes only when the work succeeds.
        forest.useControl();
        forest.confirm();
        land.continueUnpaid();
        land.verifyDialogClosed();
        forest.verifyUnitWork(FOREST_SQUARE, 0, UnitActionType.clearForest, 0,
                80);
        land.verifyDutchGold(100);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2200);
        land.verifyLandStatus(FOREST_SQUARE, NativeLandStatus.UNPAID);
        land.verifyTakenSquares(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 0);

        // Continue from disk keeps the payment, both orders and the unpaid
        // square, and replays nothing.
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
        forest.verifyUnitWork(FOREST_SQUARE, 0, UnitActionType.clearForest, 0,
                80);
        forest.verifyRemainingUpdates(FOREST_SQUARE, 0, 4);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 0);
    }

    @Test
    @Tag("local")
    void TC_02_unpaid_plow_takes_land_once_and_keeps_adding_pressure()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        PlowPage plow = new PlowPage(getContext());
        NativeLandPage land = new NativeLandPage(getContext());
        NativeRelationsPage relations = new NativeRelationsPage(getContext());

        // Plow the unpaid square and choose to continue without paying.
        game.selectTile(PLOW_SQUARE);
        plow.useControl();
        land.verifyDialogShows(CLAIMANT, "Price: 200 gold. You have 300 gold.",
                CONSEQUENCE);
        land.continueUnpaid();
        land.verifyDialogClosed();
        plow.verifyUnitWork(PLOW_SQUARE, 0, UnitActionType.plowField, 0, 80);
        land.verifyDutchGold(300);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2000);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.UNPAID);
        land.verifyTakenSquares(0);

        // Three updates of ordinary work add only the surroundings, 3.0 each:
        // alarm 3, 5, 7 and tribal anger alarm / 20.
        game.nextTurnAndCloseDialogs();
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 3);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.15);
        game.nextTurnAndCloseDialogs();
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 5);
        game.nextTurnAndCloseDialogs();
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 7);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.35);
        plow.verifyRemainingUpdates(PLOW_SQUARE, 0, 1);
        land.verifyTakenSquares(0);

        // The fourth update completes the plow: the one-time 5 is added to the
        // 7, then the update decays it by 1 and adds 3.0 plus 2 for the now
        // unpaid square in use: 12 - 1 + 5 = 16, tribal anger 16 / 20.
        game.nextTurnAndCloseDialogs();
        plow.verifyFieldAt(PLOW_SQUARE, true);
        land.verifyTakenSquares(1);
        land.verifyDutchGold(300);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2000);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.UNPAID);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 16);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.8);

        // Continue keeps the values and does not take the square again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        land.verifyTakenSquares(1);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.UNPAID);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 16);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.8);

        // The next update only adds the per-update 2 on top of the 3.0, with no
        // second 5: 16 - 1 + 5 = 20, tribal anger 20 / 20.
        game.nextTurnAndCloseDialogs();
        land.verifyTakenSquares(1);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 20);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                1.0);
    }

    @Test
    @Tag("local")
    void TC_03_minuit_arrival_waives_the_unpaid_land_effects()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        PlowPage plow = new PlowPage(getContext());
        CongressPage congress = new CongressPage(getContext());
        NativeLandPage land = new NativeLandPage(getContext());
        NativeRelationsPage relations = new NativeRelationsPage(getContext());

        // Choose Minuit. Choosing is not arrival: the square is still unpaid.
        congress.open();
        congress.select(Founder.PETER_MINUIT);
        congress.choose();
        congress.close();
        relations.verifySelectedFounder(EuropeanNation.DUTCH,
                Founder.PETER_MINUIT);
        relations.verifyFounderMember(EuropeanNation.DUTCH,
                Founder.PETER_MINUIT, false);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.UNPAID);

        // Start the same unpaid plow as in the unpaid scenario; before arrival
        // the question is still asked.
        game.selectTile(PLOW_SQUARE);
        plow.useControl();
        land.verifyDialogShows(CLAIMANT, "Price: 200 gold. You have 300 gold.",
                CONSEQUENCE);
        land.continueUnpaid();
        land.verifyDialogClosed();
        plow.verifyUnitWork(PLOW_SQUARE, 0, UnitActionType.plowField, 0, 80);

        // At the next update Minuit joins: the square is waived from now on.
        // The surroundings still add 3.0, so alarm 3 and anger 3 / 20. Nothing
        // is paid or refunded, and the earlier paid squares stay paid.
        game.nextTurnAndCloseDialogs();
        relations.verifyFounderMember(EuropeanNation.DUTCH,
                Founder.PETER_MINUIT, true);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.WAIVED);
        land.verifyLandStatus(COLONY_SQUARE, NativeLandStatus.PAID);
        land.verifyPaidSquares(8);
        land.verifyDutchGold(300);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2000);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 3);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.15);
        game.moveMouseAtLocation(PLOW_SQUARE);
        game.verifyThatStatusBarContains(
                "Claimed land of Aztec city, capital: payment waived.");

        // Two more updates add 3.0 each: alarm 5 and 7.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 7);
        plow.verifyRemainingUpdates(PLOW_SQUARE, 0, 1);

        // The fourth update completes the plow without the taking and without
        // unpaid pressure: 7 - 1 + 3 = 9 instead of 16, tribal anger 9 / 20.
        game.nextTurnAndCloseDialogs();
        plow.verifyFieldAt(PLOW_SQUARE, true);
        land.verifyTakenSquares(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 9);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.45);

        // Continue keeps the membership, the waiver and the values.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        relations.verifyFounderMember(EuropeanNation.DUTCH,
                Founder.PETER_MINUIT, true);
        land.verifyLandStatus(PLOW_SQUARE, NativeLandStatus.WAIVED);
        land.verifyTakenSquares(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 9);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.45);
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
        land.continueUnpaid();
        land.verifyColonyNameLand(FOUNDING_LAND
                + "\nThe colony will use claimed native land without paying.");
        naming.cancel();
        land.verifyColonyAt(PLOW_SQUARE, false);
        land.verifyTakenSquares(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 0);

        // Choosing to pay charges nothing yet; canceling the name keeps every
        // amount and right as it was.
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
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 3);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.15);

        // Continue keeps the colony, the payment and the values.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        land.verifyColonyAt(PLOW_SQUARE, true);
        land.verifyDutchGold(100);
        land.verifyPaidSquares(9);
        land.verifyTakenSquares(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 3);
    }

    @Test
    @Tag("local")
    void TC_05_field_work_and_road_ask_before_using_unpaid_land()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        NativeLandPage land = new NativeLandPage(getContext());
        NativeRelationsPage relations = new NativeRelationsPage(getContext());
        RoadPage road = new RoadPage(getContext());

        // Drag New Amsterdam's worker from the capital's square to the unpaid
        // forest field north of the colony: the land question comes first.
        ColonyScreen colony = game.openColonyAt(COLONY_SQUARE, "New Amsterdam");
        colony.dragFieldWorkerTo(FIELD_WORKER_ID, Direction.north);
        land.verifyDialogShows(CLAIMANT, "Price: 200 gold. You have 300 gold.",
                CONSEQUENCE);

        // Canceling keeps the worker on its old field and takes nothing.
        land.cancel();
        land.verifyDialogClosed();
        land.verifyWorkerOnSquare(FIELD_WORKER_ID, AZTEC_CAPITAL);
        land.verifyTakenSquares(0);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 0);

        // Continuing unpaid moves the worker; this first unpaid use adds 5 at
        // once and costs no gold.
        colony.dragFieldWorkerTo(FIELD_WORKER_ID, Direction.north);
        land.continueUnpaid();
        land.verifyDialogClosed();
        land.verifyWorkerOnSquare(FIELD_WORKER_ID, FOREST_SQUARE);
        land.verifyTakenSquares(1);
        land.verifyDutchGold(300);
        land.verifyClaimantTreasury(AZTEC_CAPITAL, 2000);
        land.verifyLandStatus(FOREST_SQUARE, NativeLandStatus.UNPAID);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 5);

        // The pioneer on that same square starts a road. The square is still
        // unpaid, so the question appears; canceling starts no work.
        game = colony.close();
        game.selectTile(FOREST_SQUARE);
        road.captureUnit(FOREST_SQUARE);
        road.useControl();
        land.verifyDialogShows(CLAIMANT, "Price: 200 gold. You have 300 gold.",
                CONSEQUENCE);
        land.cancel();
        land.verifyDialogClosed();
        road.verifyUnitWork(FOREST_SQUARE, UnitActionType.noAction, 3, 80);

        // Continuing unpaid starts the road; the square was already taken, so
        // nothing more is added now.
        road.useControl();
        land.continueUnpaid();
        land.verifyDialogClosed();
        road.verifyUnitWork(FOREST_SQUARE, UnitActionType.buildRoad, 0, 80);
        land.verifyTakenSquares(1);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 5);

        // The next update adds the surroundings, 3.0, and 2 for the unpaid
        // square in use, counted once although it is both taken and worked:
        // 5 - 1 + 5 = 9, tribal anger 9 / 20.
        game.nextTurnAndCloseDialogs();
        land.verifyTakenSquares(1);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 9);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.45);
    }
}
