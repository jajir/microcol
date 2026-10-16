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

        // Duplicate colony identity is already invalid; exercise the real
        // loader's rejection.
        audit.prepareInvalidSavedGame(false);
        audit.attemptInvalidSavedGame(false);
        audit.verifyInvalidSavedGameMessage(
                "The saved game contains invalid data and could not be loaded. The current game and save file were not changed.");
        audit.captureDialog("buttonOk", "invalid-save-" + false + ".png");
        org.microcol.page.DialogMessagePage.of(getContext()).close();

        // Both buttons stay on the menu, keep the previous game reference, and
        // leave bytes intact.
        audit.verifyRejectedLoadPreservedGameAndFile();
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

        // Duplicate colony identity is already invalid; exercise the real
        // loader's rejection.
        audit.prepareInvalidSavedGame(true);
        audit.attemptInvalidSavedGame(true);
        audit.verifyInvalidSavedGameMessage(
                "The saved game contains invalid data and could not be loaded. The current game and save file were not changed.");
        audit.captureDialog("buttonOk", "invalid-save-" + true + ".png");
        org.microcol.page.DialogMessagePage.of(getContext()).close();

        // Both buttons stay on the menu, keep the previous game reference, and
        // leave bytes intact.
        audit.verifyRejectedLoadPreservedGameAndFile();
    }

}
