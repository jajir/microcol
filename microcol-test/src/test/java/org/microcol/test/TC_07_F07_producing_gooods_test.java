package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.GamePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_07_F07_producing_gooods_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F07-producing-goods.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_07_unstaffed_crosses_survive_reopening_and_continue()
            throws Exception {
        // Load two established colonies with unstaffed chapels.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(Location.of(22, 12), "Delft");
        colony.verifyCrossProduction(1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CROSS, 0));

        // Reopening the colony previews production without collecting it.
        game = colony.close();
        colony = game.openColonyAt(Location.of(22, 12), "Delft");
        colony.verifyCrossProduction(1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CROSS, 0));
        game = colony.close();

        // One complete round credits exactly one cross to each colony.
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(Location.of(22, 12), "Delft");
        colony.verifyCrossProduction(1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CROSS, 1));
        game = colony.close();
        colony = game.openColonyAt(Location.of(25, 14), "New Amsterdam");
        colony.verifyCrossProduction(1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CROSS, 1));
        game = colony.close();

        // Menu recovery and Continue preserve the already collected amount.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(Location.of(22, 12), "Delft");
        colony.verifyCrossProduction(1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CROSS, 1));
        game = colony.close();

        // The next round adds one more, without replaying the previous round.
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(Location.of(22, 12), "Delft");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.CROSS, 2));
    }

    @Test
    @Tag("local")
    void TC_07_producing_goods() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // Open colony Delft at [22,12].
        ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12),
                "Delft");

        // Verify number of corn is 167.
        colonyScreen.verifyNumberOfGoodsInWrehouse(0, 167);

        // Verify number of tobacco 54.
        colonyScreen.verifyNumberOfGoodsInWrehouse(2, 54);

        // Close colony screen.
        gamePage = colonyScreen.close();

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Open colony Delft at [22,12].
        colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

        // Verify number of corn is 170.
        colonyScreen.verifyNumberOfGoodsInWrehouse(0, 170);

        // Verify number of corn is 63.
        colonyScreen.verifyNumberOfGoodsInWrehouse(2, 63);

        // Close colony screen.
        gamePage = colonyScreen.close();

        // Perform next turn and ignore turn events dialog.
        gamePage.nextTurnAndCloseDialogs();

        // Open colony Delft at [22,12].
        colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

        // Verify number of corn is 173.
        colonyScreen.verifyNumberOfGoodsInWrehouse(0, 173);

        // Verify number of corn is 72.
        colonyScreen.verifyNumberOfGoodsInWrehouse(2, 72);
    }

}
