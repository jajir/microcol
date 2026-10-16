package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_39_F31_shared_colony_fields_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F31-shared-colony-fields.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_shared_square_must_be_released_before_reassignment()
            throws Exception {
        // The neighboring colony already works the requested world square.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        colony.dragFieldWorkerTo(0, Direction.east);
        colony.verifyFieldWorker(0, Location.of(23, 16), 4);
        colony.verifyThatStatusBarContains("Another colony works this square");
        colony.captureDialog("statusBarLabel", "shared-field-rejection.png");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 100));
        game = colony.close();

        // Moving the neighbor's last resident frees the square without
        // destroying it.
        colony = game.openColonyAt(Location.of(26, 16), "Delft");
        colony.dragFieldWorkerTo(1, Direction.east);
        colony.verifyFieldWorker(1, Location.of(27, 16), 4);
        colony.verifyColonyPresent("Delft", true);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 100));
        game = colony.close();

        // The original worker can now take the free square, once, without
        // changing stocks.
        colony = game.openColonyAt(Location.of(24, 16), "New Amsterdam");
        colony.dragFieldWorkerTo(0, Direction.east);
        colony.verifyFieldWorker(0, Location.of(25, 16), 4);
        colony.verifyColonyPresent("New Amsterdam", true);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 100));
    }

    @Test
    @Tag("local")
    void TC_02_invalid_load_preserves_the_menu_and_file() throws Exception {
        // Start and leave a real game so Continue is available.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.exitToMenu();
        final org.microcol.page.FreePlayAuditPage audit = new org.microcol.page.FreePlayAuditPage(
                getContext());
        audit.verifyContinueButtonEnabled(true);

        // Two saved colonies claim the same world square; reject before
        // exposing either.
        audit.prepareInvalidSavedGame(false);
        audit.attemptInvalidSavedGame(false);
        audit.verifyInvalidSavedGameMessage(
                "The saved game contains invalid data and could not be loaded. The current game and save file were not changed.");
        audit.captureDialog("buttonOk", "invalid-save-" + false + ".png");
        org.microcol.page.DialogMessagePage.of(getContext()).close();

        // Both buttons stay on the menu, keep the previous game reference, and
        // leave bytes intact.
        audit.verifyRejectedLoadPreservedGameAndFile();
        WelcomePage.of(getContext()).verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_03_invalid_continue_preserves_the_menu_and_file() throws Exception {
        // Start and leave a real game so Continue is available.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.exitToMenu();
        final org.microcol.page.FreePlayAuditPage audit = new org.microcol.page.FreePlayAuditPage(
                getContext());
        audit.verifyContinueButtonEnabled(true);

        // Two saved colonies claim the same world square; reject before
        // exposing either.
        audit.prepareInvalidSavedGame(true);
        audit.attemptInvalidSavedGame(true);
        audit.verifyInvalidSavedGameMessage(
                "The saved game contains invalid data and could not be loaded. The current game and save file were not changed.");
        audit.captureDialog("buttonOk", "invalid-save-" + true + ".png");
        org.microcol.page.DialogMessagePage.of(getContext()).close();

        // Both buttons stay on the menu, keep the previous game reference, and
        // leave bytes intact.
        audit.verifyRejectedLoadPreservedGameAndFile();
        WelcomePage.of(getContext()).verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_04_rejected_save_keeps_the_active_game() throws Exception {
        // A loaded game remains playable if another save fails validation.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final org.microcol.page.FreePlayAuditPage audit = new org.microcol.page.FreePlayAuditPage(
                getContext());
        audit.prepareInvalidSavedGame(false);
        audit.attemptInvalidSavedGameWhilePlaying();
        audit.verifyInvalidSavedGameMessage(
                "The saved game contains invalid data and could not be loaded. The current game and save file were not changed.");
        org.microcol.page.DialogMessagePage.of(getContext()).close();
        audit.verifyRejectedLoadPreservedGameAndFile();
        game.openColonyAt(Location.of(24, 16), "New Amsterdam")
                .verifyFieldWorker(0, Location.of(23, 16), 4);
    }

    @Test
    @Tag("local")
    void TC_05_cancel_and_stale_drop_preserve_the_world() throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(Location.of(26, 16), "Delft");
        colony.dragFieldWorkerTo(1, Direction.east);
        colony.verifyFieldWorker(1, Location.of(27, 16), 4);
        game = colony.close();
        colony = game.openColonyAt(Location.of(24, 16), "New Amsterdam");

        // Cancel a native drag over the newly freed square.
        colony.rememberWorld();
        colony.cancelFieldWorkerDrag(0, Direction.east);
        colony.verifyWorldUnchanged();
        colony.verifyFieldWorker(0, Location.of(23, 16), 4);

        // Simulate another valid assignment after drag admission, just before
        // the native drop.
        colony.dragFieldWorkerAfterNeighborTakesSquare(0, Direction.east);
        colony.verifyWorldUnchanged();
        colony.verifyFieldWorker(0, Location.of(23, 16), 4);
        colony.verifyThatStatusBarContains("Another colony works this square");
    }

    @Test
    @Tag("local")
    void TC_06_crop_change_round_trip_and_single_turn_output()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");

        // Merely opening the crop choice changes no job, equipment or stock.
        colony.rememberWorld();
        colony.openCropMenu(0);
        colony.verifyWorldUnchanged();
        colony.cancelCropMenu();
        colony.verifyCropMenuShowing(false);
        colony.verifyWorldUnchanged();
        colony.openCropMenu(0);
        colony.chooseCrop(GoodsType.TOBACCO, 3);
        colony.verifyFieldProduction(0, Location.of(23, 16), GoodsType.TOBACCO,
                3);
        game = colony.close();

        // Save and Continue restore the same worker and crop.
        game.exitToMenu();
        game = new org.microcol.page.FreePlayAuditPage(getContext())
                .continueGame();
        colony = game.openColonyAt(Location.of(24, 16), "New Amsterdam");
        colony.verifyFieldProduction(0, Location.of(23, 16), GoodsType.TOBACCO,
                3);
        game = colony.close();

        // One completed round adds one crop yield and consumes food for the one
        // resident.
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(Location.of(24, 16), "New Amsterdam");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 98));
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOBACCO, 3));
        game = colony.close();
        colony = game.openColonyAt(Location.of(26, 16), "Delft");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CORN, 102));
        colony.verifyFieldWorker(1, Location.of(25, 16), 4);
    }

    @Test
    @Tag("local")
    void TC_07_stale_crop_selection_cannot_change_a_moved_worker()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        colony.openCropMenu(0);

        // Change the job after menu opening, immediately before its action
        // handler.
        colony.moveWorkerWhenCropIsConfirmed(0);
        colony.chooseCrop(GoodsType.TOBACCO, 3);
        colony.verifyWorldUnchanged();
        colony.verifyFieldWorker(0, Location.of(24, 15), 4);
        colony.verifyThatStatusBarContains(
                "This worker or square is unavailable");
    }

    @Test
    @Tag("local")
    void TC_08_equipment_return_must_fit_before_an_outdoor_assignment()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");

        // The map colonist carries 80 tools; another 50 already occupy this
        // warehouse.
        colony.verifyNumberOfUnitsAtPier(1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 50));
        colony.rememberWorld();
        colony.moveUnitFromPietToEmptyField(0);

        // The rejected drag neither unequips nor moves the colonist or changes
        // either colony.
        colony.verifyWorldUnchanged();
        colony.verifyNumberOfUnitsAtPier(1);
        colony.verifyFieldWorker(0, Location.of(23, 16), 4);
        colony.verifyThatStatusBarContains(
                "This worker or square is unavailable");
    }
}
