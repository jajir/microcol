package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.PassengerTransferPage;
import org.microcol.page.SettingPage;
import org.microcol.page.TreasureCargoPage;
import org.microcol.page.TreasureCargoPage.Holds;
import org.microcol.page.TreasureCargoPage.Layout;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Six-hold treasure train in a galleon (R-0134). A train of the native conquest
 * stands beside a Dutch galleon of six holds on the real map. In the colony
 * port, where the dock draws the holds, six free holds take the train by drag
 * and drop as one unit in six occupied crates, with only the train's own crate
 * to drag; Continue keeps it and unloading frees all six. With five free holds
 * the dock refuses the drop and the ore stays. On the map the same train boards
 * by the unit drag, a refused boarding shows the movement message, and the
 * passenger chooser offers the train once. Capacity and the reservation
 * arithmetic are checked in the model tests, and the one-hold passenger path
 * stays in the passenger transfer scenarios.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_140_F131_treasure_cargo_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F21-passenger-transfer.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_six_free_holds_take_one_train_that_continue_keeps_and_unloads()
            throws Exception {
        // Load the colony port with the train on its square and six free holds.
        WelcomePage.of(getContext()).loadGame();
        final TreasureCargoPage holds = new TreasureCargoPage(getContext());
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        holds.loadScenario(FIXTURE, Layout.PORT, Holds.ALL_FREE);
        holds.verifyTrainAt(TreasureCargoPage.COLONY);
        ColonyScreen colony = holds.openDock();
        holds.verifySixFreeHolds();

        // Drop the train on a crate: one unit fills all six crates.
        passengers.embarkInPort(TreasureCargoPage.TRAIN_ID, 0, false);
        holds.verifyOneTrainInSixHolds();
        holds.captureHolds("treasure-cargo-port-en.png");

        // A crate that only reserves a hold has no train to drag out.
        holds.dragHoldOutside(3);
        holds.verifyOneTrainInSixHolds();

        // Continue keeps the one train with its full reservation.
        GamePage game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = holds.openDock();
        holds.verifyOneTrainInSixHolds();

        // Dragging the train's own crate out releases all six holds.
        passengers.disembarkInPort(TreasureCargoPage.TRAIN_ID, 0, false);
        holds.verifyTrainAt(TreasureCargoPage.COLONY);
        holds.verifySixFreeHolds();
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_02_five_free_holds_refuse_the_train_and_keep_the_goods()
            throws Exception {
        // One hold carries ore, so only five holds are free.
        WelcomePage.of(getContext()).loadGame();
        final TreasureCargoPage holds = new TreasureCargoPage(getContext());
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        holds.loadScenario(FIXTURE, Layout.PORT, Holds.ONE_ORE_HOLD);
        ColonyScreen colony = holds.openDock();
        holds.verifyOreHold(TreasureCargoPage.ORE_AMOUNT);
        holds.verifyOreCrate(TreasureCargoPage.ORE_AMOUNT);

        // The dock accepts the drop on neither a free crate nor the ore crate.
        passengers.dragPassengerToSlot(TreasureCargoPage.TRAIN_ID, 1, false);
        holds.verifyTrainRefused(TreasureCargoPage.COLONY);
        passengers.dragPassengerToSlot(TreasureCargoPage.TRAIN_ID, 0, false);
        holds.verifyTrainRefused(TreasureCargoPage.COLONY);
        holds.verifyOreHold(TreasureCargoPage.ORE_AMOUNT);
        holds.verifyOreCrate(TreasureCargoPage.ORE_AMOUNT);

        // Continue shows the same refusal: no train and no goods were moved.
        GamePage game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = holds.openDock();
        holds.verifyTrainRefused(TreasureCargoPage.COLONY);
        holds.verifyOreHold(TreasureCargoPage.ORE_AMOUNT);
        holds.verifyOreCrate(TreasureCargoPage.ORE_AMOUNT);
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_03_czech_hold_view_shows_one_train_in_six_holds() throws Exception {
        // Switch to Czech and load the colony port with six free holds.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final TreasureCargoPage holds = new TreasureCargoPage(getContext());
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        holds.loadScenario(FIXTURE, Layout.PORT, Holds.ALL_FREE);
        final ColonyScreen colony = holds.openDock();
        holds.verifySixFreeHolds();

        // The boarded train fills the six crates in the Czech screen as well.
        passengers.embarkInPort(TreasureCargoPage.TRAIN_ID, 0, false);
        holds.verifyOneTrainInSixHolds();
        holds.captureHolds("treasure-cargo-port-cs.png");
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_04_map_drag_boards_the_train_and_continue_keeps_it()
            throws Exception {
        // The galleon at sea has six free holds and the train stands ashore.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final TreasureCargoPage holds = new TreasureCargoPage(getContext());
        holds.loadScenario(FIXTURE, Layout.SEA, Holds.ALL_FREE);
        holds.verifyTrainAt(TreasureCargoPage.SHORE);

        // Dragging the train onto the ship boards it into all six holds.
        game.moveMouseAtLocation(TreasureCargoPage.SHORE);
        game.dragMouseAtLocation(TreasureCargoPage.WATER);
        game.waitForNumberOfUnitsAt(TreasureCargoPage.SHORE, 0);
        holds.verifyTrainAboard();

        // Continue keeps the one train and its six reserved holds.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        holds.verifyTrainAboard();
    }

    @Test
    @Tag("local")
    void TC_05_chooser_offers_the_aboard_train_once_and_unloading_frees_the_holds()
            throws Exception {
        // The train is already aboard at sea, reserving all six holds.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final TreasureCargoPage holds = new TreasureCargoPage(getContext());
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        holds.loadScenario(FIXTURE, Layout.SEA, Holds.TRAIN_ABOARD);
        holds.verifyTrainAboard();

        // Dragging the ship to the shore opens a chooser with one train only.
        game.moveMouseAtLocation(TreasureCargoPage.WATER);
        game.dragMouseAtLocation(TreasureCargoPage.SHORE);
        passengers.verifyChoiceCount(1);
        passengers.verifyChoice(TreasureCargoPage.TRAIN_ID, true);
        passengers.captureDialog("passenger-confirm",
                "treasure-cargo-chooser-en.png");

        // Cancel leaves the train aboard; confirming lands it and frees all
        // six.
        passengers.cancel();
        holds.verifyTrainAboard();
        game.moveMouseAtLocation(TreasureCargoPage.WATER);
        game.dragMouseAtLocation(TreasureCargoPage.SHORE);
        passengers.choose(TreasureCargoPage.TRAIN_ID);
        passengers.confirm();
        game.waitForNumberOfUnitsAt(TreasureCargoPage.SHORE, 1);
        holds.verifyTrainAt(TreasureCargoPage.SHORE);
        holds.verifyHoldsReleased();
    }

    @Test
    @Tag("local")
    void TC_06_map_drag_with_five_free_holds_shows_the_movement_message()
            throws Exception {
        // One hold carries ore at sea, so the ship has only five free holds.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final TreasureCargoPage holds = new TreasureCargoPage(getContext());
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        holds.loadScenario(FIXTURE, Layout.SEA, Holds.ONE_ORE_HOLD);
        holds.verifyOreHold(TreasureCargoPage.ORE_AMOUNT);

        // The boarding drag is refused with the movement message, nothing
        // moves.
        game.moveMouseAtLocation(TreasureCargoPage.SHORE);
        game.dragMouseAtLocation(TreasureCargoPage.WATER);
        passengers.verifyMovementRejected();
        passengers.closeRejection();
        holds.verifyTrainRefused(TreasureCargoPage.SHORE);
        holds.verifyOreHold(TreasureCargoPage.ORE_AMOUNT);
    }

}
