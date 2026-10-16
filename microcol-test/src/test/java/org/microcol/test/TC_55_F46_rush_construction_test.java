package org.microcol.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.*;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Explicit paid completion, cancellation and stale offers preserve one
 * transaction.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_55_F46_rush_construction_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F46-rush-construction.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_then_pay_taxed_quote_once_and_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage building = new ConstructionPage(getContext());
        ConstructionRushPage rush = new ConstructionRushPage(getContext());
        rush.setToolAsk(3);
        building.assignCarpenter(4);
        building.open();
        building.select("BLACKSMITHS_SHOP");
        building.confirm();
        building.select("WAREHOUSE");
        building.confirm();
        colony.rememberWorld();
        // Thirty missing hammers and five tools at ask three/tax ten cost 76.
        rush.open();
        rush.verifyQuote("Blacksmith shop", 34, 15, 30, 5, 76, 9924, true);
        rush.captureDialog("constructionRushDialog", "r83-rush-quote.png");
        rush.cancel();
        colony.verifyWorldUnchanged();
        rush.open();
        rush.confirm();
        building.verifyBuilding(ConstructionType.BLACKSMITHS_SHOP, 1);
        building.verifyBuilding(ConstructionType.BLACKSMITHS_HOUSE, 0);
        building.verifyQueue(List.of("WAREHOUSE"), List.of(1), 0);
        rush.verifyStocks(9924, 0, 0);
        building.cancel();
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        building.verifyBuilding(ConstructionType.BLACKSMITHS_SHOP, 1);
        building.verifyQueue(List.of("WAREHOUSE"), List.of(1), 0);
        rush.verifyStocks(9924, 0, 0);
    }

    @Test
    @Tag("local")
    void TC_02_insufficient_gold_and_stale_materials_do_not_charge()
            throws Exception {
        ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage building = new ConstructionPage(getContext());
        ConstructionRushPage rush = new ConstructionRushPage(getContext());
        rush.setToolAsk(3);
        building.assignCarpenter(4);
        building.open();
        building.select("BLACKSMITHS_SHOP");
        building.confirm();
        rush.setGold(75);
        colony.rememberWorld();
        rush.open();
        rush.verifyQuote("Blacksmith shop", 34, 15, 30, 5, 76, -1, false);
        rush.verifyFeedback("Not enough gold.", false);
        rush.cancel();
        colony.verifyWorldUnchanged();
        // A changed balance after review invalidates the old offer.
        rush.setGold(10000);
        rush.open();
        rush.addHammer();
        colony.rememberWorld();
        rush.confirm();
        rush.verifyFeedback("The offer changed. Nothing was spent.", false);
        colony.verifyWorldUnchanged();
        rush.cancel();
        rush.open();
        rush.verifyQuote("Blacksmith shop", 35, 15, 29, 5, 74, 9926, true);
        rush.confirm();
        rush.verifyStocks(9926, 0, 0);
        building.verifyBuilding(ConstructionType.BLACKSMITHS_SHOP, 1);
        building.verifyQueue(List.of(), List.of(), 0);
        building.cancel();
    }
}
