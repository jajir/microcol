package org.microcol.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.ConstructionPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Construction admission, explicit confirmation and actual completion. */
@ExtendWith(ApplicationExtension.class)
public class TC_32_F24_construction_eligibility_test
        extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F24-construction-eligibility.microcol");
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_population_predecessor_cancel_and_confirm_once()
            throws Exception {
        // Three residents cannot start a four-resident smithy upgrade.
        ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.open();
        construction.select("BLACKSMITHS_SHOP");
        construction.verifyOffer(false, "Requires 4 residents; currently 3.",
                64, 20);
        construction.select("IRON_WORKS");
        construction.verifyOffer(false, "Requires: Blacksmith shop.", 240, 100);
        construction.captureDialog("constructionProjects",
                "construction-unavailable.png");
        construction.escape();
        construction.verifyPickerClosed();
        construction.verifyQueue(List.of(), List.of(), 58);
        construction.verifyStocksAndPopulation(58, 19, 3);

        // Assign the fourth resident through the colony's carpenter workplace.
        construction.assignCarpenter(4);
        colony.verifyThatProductionIs(GoodsType.HAMMERS, 6);
        construction.open();
        construction.select("BLACKSMITHS_SHOP");
        construction.verifyOffer(true, "Available.", 64, 20);
        construction.cancel();
        construction.verifyPickerClosed();
        construction.verifyQueue(List.of(), List.of(), 58);
        construction.verifyStocksAndPopulation(58, 19, 4);

        // Explicit confirmation queues exactly once, without spending
        // resources.
        construction.open();
        construction.select("BLACKSMITHS_SHOP");
        construction.confirmTwice();
        construction.verifyQueue(List.of("BLACKSMITHS_SHOP"), List.of(0), 58);
        construction.select("BLACKSMITHS_SHOP");
        construction.verifyOffer(false, "already queued", 64, 20);
        construction.escape();
        construction.verifyPickerClosed();
        construction.verifyStocksAndPopulation(58, 19, 4);
    }

    @Test
    @Tag("local")
    void TC_02_production_waits_for_last_tool_then_completes_once_and_continues()
            throws Exception {
        // Admit a real project, then let the two carpenters produce six
        // hammers.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.assignCarpenter(4);
        construction.open();
        construction.select("BLACKSMITHS_SHOP");
        construction.confirm();
        construction.select("WAREHOUSE");
        construction.confirm();
        construction.cancel();
        construction.verifyPickerClosed();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyQueue(List.of("BLACKSMITHS_SHOP", "WAREHOUSE"),
                List.of(0, 1), 64);
        construction.verifyStocksAndPopulation(64, 19, 4);
        construction.verifyBuilding(ConstructionType.BLACKSMITHS_HOUSE, 1);
        construction.verifyBuilding(ConstructionType.BLACKSMITHS_SHOP, 0);
        construction.verifyStatus("Waiting for tools: 1.");
        construction.captureDialog("constructionOpen",
                "construction-waiting.png");

        // Reopening and cancelling cannot discard completed hammer work.
        construction.open();
        construction.select("LUMBER_MILL");
        construction.escape();
        construction.verifyPickerClosed();
        construction.verifyQueue(List.of("BLACKSMITHS_SHOP", "WAREHOUSE"),
                List.of(0, 1), 64);
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyQueue(List.of("BLACKSMITHS_SHOP", "WAREHOUSE"),
                List.of(0, 1), 64);
        construction.verifyStatus("Waiting for tools: 1.");

        // Bring the final tool; the turn completes only the first project
        // and preserves the queued warehouse.
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 14);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 20));
        construction.verifyBuilding(ConstructionType.BLACKSMITHS_SHOP, 0);
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyQueue(List.of("WAREHOUSE"), List.of(1), 6);
        construction.verifyBuilding(ConstructionType.BLACKSMITHS_HOUSE, 0);
        construction.verifyBuilding(ConstructionType.BLACKSMITHS_SHOP, 1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 0));
        construction.verifyStatus("Waiting for hammers.");
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyBuilding(ConstructionType.BLACKSMITHS_SHOP, 1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 0));
        construction.verifyQueue(List.of("WAREHOUSE"), List.of(1), 6);
    }

    @Test
    @Tag("local")
    void TC_03_mixed_queue_order_removal_and_continue() throws Exception {
        // Finish the mill through construction before its wagon offer becomes
        // available.
        ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.open();
        construction.select("LUMBER_MILL");
        construction.confirm();
        construction.cancel();
        construction.verifyPickerClosed();
        GamePage game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyBuilding(ConstructionType.LUMBER_MILL, 1);
        construction.open();
        construction.select("WAGON");
        construction.confirm();
        construction.select("WAREHOUSE");
        construction.confirm();
        construction.verifyQueue(List.of("WAGON", "WAREHOUSE"), List.of(0, 1),
                9);
        construction.selectQueue(1);
        construction.moveUp();
        construction.verifyQueue(List.of("WAREHOUSE", "WAGON"), List.of(1, 0),
                9);
        construction.moveDown();
        construction.verifyQueue(List.of("WAGON", "WAREHOUSE"), List.of(0, 1),
                9);
        construction.cancel();
        construction.verifyPickerClosed();
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyQueue(List.of("WAGON", "WAREHOUSE"), List.of(0, 1),
                9);
        construction.open();
        construction.selectQueue(0);
        construction.remove();
        construction.verifyQueue(List.of("WAREHOUSE"), List.of(1), 9);
        construction.cancel();
        construction.verifyPickerClosed();
        construction.verifyStocksAndPopulation(9, 19, 3);
    }

    @Test
    @Tag("local")
    void TC_04_stale_population_requires_a_new_selection() throws Exception {
        // Simulate an external workforce change after the player reviews the
        // offer.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.open();
        construction.select("LUMBER_MILL");
        construction.verifyOffer(true, "Available.", 52, 0);
        getContext().getRobot().interact(
                () -> getModel().getUnitById(200).placeToLocation(COLONY));
        construction.confirm();
        construction.verifyChangedOffer();
        construction.verifyQueue(List.of(), List.of(), 58);
        construction.select("LUMBER_MILL");
        construction.verifyOffer(false, "Requires 3 residents; currently 2.",
                52, 0);
        construction.cancel();
        construction.verifyPickerClosed();
        construction.verifyStocksAndPopulation(58, 19, 2);
    }

    @Test
    @Tag("local")
    void TC_05_czech_picker_and_keyboard_confirmation() throws Exception {
        // Set the language through settings and reach construction with
        // Tab/Space.
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame().openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.openWithKeyboard();
        construction.select("LUMBER_MILL");
        construction.verifyCzechOffer();
        construction.captureDialog("constructionProjects",
                "construction-czech.png");
        construction.confirmWithKeyboard();
        construction.verifyQueue(List.of("LUMBER_MILL"), List.of(0), 58);
        construction.escape();
        construction.verifyPickerClosed();
    }

    @Test
    @Tag("local")
    void TC_06_replaced_game_cannot_accept_the_old_selection()
            throws Exception {
        // Loading another model while the picker is open invalidates its
        // selection.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.open();
        construction.select("LUMBER_MILL");
        org.microcol.model.Model previous = getModel();
        getContext().getRobot()
                .interact(() -> getContext().getClassFromGuice(
                        org.microcol.gui.event.model.GameController.class)
                        .loadModelFromFile(FIXTURE));
        construction.confirm();
        construction.verifyChangedOffer();
        construction.verifyReplacedModelUnchanged(previous);
        construction.escape();
        construction.verifyPickerClosed();
    }

    @Test
    @Tag("local")
    void TC_07_restored_project_waits_for_its_missing_predecessor()
            throws Exception {
        // A valid saved queue can retain an admitted project after prerequisite
        // loss.
        com.google.gson.JsonObject saved = new com.google.gson.Gson().fromJson(
                java.nio.file.Files.readString(FIXTURE.toPath()),
                com.google.gson.JsonObject.class);
        com.google.gson.JsonObject colonyData = saved.getAsJsonObject("model")
                .getAsJsonArray("colonies").get(0).getAsJsonObject();
        com.google.gson.JsonObject entry = new com.google.gson.JsonObject();
        entry.addProperty("type", "CONSTRUCTION");
        entry.addProperty("id", 9);
        entry.addProperty("constructionType", "IRON_WORKS");
        colonyData.getAsJsonArray("buildingQueue").add(entry);
        colonyData.getAsJsonObject("colonyWarehouse").addProperty("HAMMERS",
                240);
        colonyData.getAsJsonObject("colonyWarehouse").addProperty("TOOLS", 100);
        File paused = new File(System.getProperty("user.home"),
                "construction-paused.microcol");
        java.nio.file.Files.writeString(paused.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(paused);
        ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.verifyStatus("Requires: Blacksmith shop.");
        construction.verifyQueue(List.of("IRON_WORKS"), List.of(9), 240);
        construction.verifyStocksAndPopulation(240, 100, 3);
        // A real turn must preserve progress and tools while reporting the
        // missing shop.
        GamePage game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyStatus("Requires: Blacksmith shop.");
        construction.verifyQueue(List.of("IRON_WORKS"), List.of(9), 243);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 100));
        construction.verifyBuilding(ConstructionType.IRON_WORKS, 0);
    }
}
