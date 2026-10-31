package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.gui.Tile;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_195_F227_colony_layout_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F227-colony-layout.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_sections_and_explicit_tile_production() throws Exception {
        // Read the model-backed summary and all sixteen stable building
        // positions.
        final ColonyScreen town = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(Location.of(24, 16), "New Amsterdam");
        final ColonyLayoutPage layout = new ColonyLayoutPage(getContext());
        new GameBarWidthPage(getContext()).resizeContent(1440, 1000);
        layout.verifyMapTileSize(Tile.TILE_WIDTH_IN_PX);
        layout.verifyLayout("New Amsterdam", 7);
        layout.verifyCompactBuildings(46, 22, 32, 12);
        layout.verifyBuildingLevel("id_warehouse",
                ConstructionType.WAREHOUSE_BASIC);
        layout.verifyCenter();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0371-colony-english.png");

        // Selecting a worked tile by keyboard is read-only.
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();
        layout.selectTile(1, 1);
        layout.verifyMapFocused();
        layout.verifySelectedTileColor(1, 1,
                javafx.scene.paint.Color.web("#c99638"));
        layout.moveSelection(KeyCode.LEFT);
        layout.verifySelectedTileColor(0, 1,
                javafx.scene.paint.Color.web("#c99638"));
        layout.verifyOutput(GoodsType.LUMBER, 2);
        layout.verifyProductionChoices(0);
        layout.verifyPreviewMatchesMap(0, 1);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0362-production-en.png");
        jobs.verifyWorldUnchanged();

        // An explicit crop choice updates this worker, the inspector and
        // forecast.
        layout.moveSelection(KeyCode.ENTER);
        layout.verifyOutputFocused(GoodsType.LUMBER);
        layout.chooseOutput(GoodsType.CORN);
        layout.verifyOutput(GoodsType.CORN, 2);
        jobs.verifyFieldJob(0, Location.of(23, 16),
                org.microcol.model.Goods.of(GoodsType.CORN, 2));
        layout.verifyLayout("New Amsterdam", 7);
        layout.verifyCompactBuildings(46, 22, 32, 12);
        layout.selectTile(2, 0);
        layout.verifyEmptyField();
        layout.verifyPreviewMatchesMap(2, 0);
        layout.selectTile(1, 1);
        layout.verifyCenter();

        // The warehouse remains reachable at the original window width.
        new GameBarWidthPage(getContext()).resizeContent(1000, 925);
        layout.verifyCompactBuildings(46, 22, 32, 12);
        layout.verifyMapTileSize(Tile.TILE_WIDTH_IN_PX);
        layout.selectTile(0, 1);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0362-production-small.png");
        layout.verifyProductionChoices(0);
        layout.verifyPreviewMatchesMap(0, 1);
        layout.selectTile(1, 1);
        layout.showStock();
        town.verifyWarehouseGoodsLayout(19, 45);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0371-colony-small.png");
        town.close();
    }

    @Test
    @Tag("local")
    void TC_02_czech_upgraded_town() throws Exception {
        // Use the normal language settings and open a town with every family
        // upgraded.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final ColonyScreen town = settings.goBack().loadGame()
                .openColonyAt(Location.of(26, 16), "Delft");
        new GameBarWidthPage(getContext()).resizeContent(1440, 1000);
        final ColonyLayoutPage layout = new ColonyLayoutPage(getContext());
        layout.verifyMapTileSize(Tile.TILE_WIDTH_IN_PX);
        layout.verifyLayout("Delft", 0);
        layout.verifyCompactBuildings(46, 22, 32, 12);
        layout.verifyBuildingLevel("id_fort", ConstructionType.FORTRESS);
        layout.verifyBuildingLevel("id_warehouse",
                ConstructionType.WAREHOUSE_EXPANSION);
        layout.verifyBuildingLevel("id_dock", ConstructionType.SHIPYARD);
        layout.verifyCenter();
        layout.selectTile(0, 1);
        layout.verifyProductionChoices(1);
        layout.verifyPreviewMatchesMap(0, 1);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0362-production-cs.png");
        layout.selectTile(1, 1);
        layout.showStock();
        town.verifyWarehouseGoodsLayout(19, 45);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0371-colony-czech-upgrades.png");
        new GameBarWidthPage(getContext()).resizeContent(1000, 925);
        layout.verifyCompactBuildings(46, 22, 32, 12);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0371-colony-czech-small.png");
        town.close();
    }

    @Test
    @Tag("local")
    void TC_03_english_action_names_and_hover_help() throws Exception {
        // Wide town names must fit before and after switching to compact icons.
        final ColonyScreen town = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(Location.of(24, 16), "New Amsterdam");
        final ColonyLayoutPage layout = new ColonyLayoutPage(getContext());
        final GameBarWidthPage bars = new GameBarWidthPage(getContext());
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();
        bars.resizeContent(1600, 1000);
        layout.verifyActionNames(true, "Exports", "Education…", "Treasure",
                "Route cargo…", "Trade route…", "Load", "Unload",
                "Back to game");
        layout.hoverAction("educationOpen");
        layout.verifyActionExplanation(
                "Choose a teacher and student, manage their training, or cancel a course.");
        new InGameLayoutPage(getContext()).saveEvidence("r0360-actions-en.png");
        bars.resizeContent(1000, 925);
        layout.verifyActionNames(false, "Exports", "Education…", "Treasure",
                "Route cargo…", "Trade route…", "Load", "Unload",
                "Back to game");

        // Every icon explains its action; disabled exports explain the
        // prerequisite.
        layout.hoverAction("customHouseOpen");
        layout.verifyActionExplanation(
                "Build a custom house to choose automatic exports.");
        layout.hoverAction("educationOpen");
        layout.verifyActionExplanation(
                "Choose a teacher and student, manage their training, or cancel a course.");
        layout.hoverAction("treasureOpen");
        layout.verifyActionExplanation(
                "Choose a treasure in this colony and review its payout and transport fee before redeeming it.");
        layout.hoverAction("tradeRouteCargoOpen");
        layout.verifyActionExplanation(
                "Set which goods to unload and load at each trade-route stop, or process a carrier waiting here.");
        layout.hoverAction("tradeRouteOpen");
        layout.verifyActionExplanation(
                "Create or edit a trade route, choose its stops and assign a ship or wagon.");
        layout.hoverAction("quickLoad");
        layout.verifyActionExplanation(
                "Propose goods from the warehouse to load onto the selected ship. Review the amount before confirming.");
        layout.hoverAction("quickUnload");
        layout.verifyActionExplanation(
                "Propose cargo from the selected ship to unload into the warehouse. Review the amount before confirming.");
        layout.hoverAction("closeButtonId");
        layout.verifyActionExplanation(
                "Close the colony and return to the world map.");
        layout.hoverActionGap();
        layout.verifyNoActionExplanation();
        layout.hoverAction("quickUnload");
        layout.leaveActions();
        layout.verifyNoActionExplanation();
        jobs.verifyWorldUnchanged();

        // Expanding restores the complete labels and the original return
        // action.
        bars.resizeContent(1600, 1000);
        layout.verifyActionNames(true, "Exports", "Education…", "Treasure",
                "Route cargo…", "Trade route…", "Load", "Unload",
                "Back to game");
        town.close();
    }

    @Test
    @Tag("local")
    void TC_04_czech_action_names_and_hover_help() throws Exception {
        // Switching language refreshes both compact names and their
        // explanations.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final ColonyScreen town = settings.goBack().loadGame()
                .openColonyAt(Location.of(24, 16), "New Amsterdam");
        final ColonyLayoutPage layout = new ColonyLayoutPage(getContext());
        final GameBarWidthPage bars = new GameBarWidthPage(getContext());
        bars.resizeContent(1600, 1000);
        layout.verifyActionNames(true, "Vývoz", "Vzdělávání…", "Poklad",
                "Náklad trasy…", "Obchodní trasa…", "Naložit", "Vyložit",
                "Zpět do hry");
        layout.hoverAction("educationOpen");
        layout.verifyActionExplanation(
                "Vyberte učitele a žáka, spravujte jejich výuku nebo ji zrušte.");
        new InGameLayoutPage(getContext()).saveEvidence("r0360-actions-cs.png");
        bars.resizeContent(1000, 925);
        layout.verifyActionNames(false, "Vývoz", "Vzdělávání…", "Poklad",
                "Náklad trasy…", "Obchodní trasa…", "Naložit", "Vyložit",
                "Zpět do hry");
        layout.hoverAction("customHouseOpen");
        layout.verifyActionExplanation(
                "Pro automatický vývoz nejprve postavte celnici.");
        layout.hoverAction("quickLoad");
        layout.verifyActionExplanation(
                "Navrhněte zboží ze skladu k naložení na vybranou loď. Před potvrzením zkontrolujte množství.");
        layout.hoverActionGap();
        layout.verifyNoActionExplanation();
        layout.hoverAction("quickUnload");
        layout.leaveActions();
        layout.verifyNoActionExplanation();
        bars.resizeContent(1600, 1000);
        layout.verifyActionNames(true, "Vývoz", "Vzdělávání…", "Poklad",
                "Náklad trasy…", "Obchodní trasa…", "Naložit", "Vyložit",
                "Zpět do hry");
        town.close();
    }

    @Test
    @Tag("local")
    void TC_05_keyboard_choice_and_stale_offer_rejection() throws Exception {
        // Reach the current product from the map and change it using the
        // keyboard.
        final ColonyScreen town = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(Location.of(24, 16), "New Amsterdam");
        final ColonyLayoutPage layout = new ColonyLayoutPage(getContext());
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        layout.selectTile(0, 1);
        layout.moveSelection(KeyCode.SPACE);
        layout.verifyOutputFocused(GoodsType.LUMBER);
        layout.chooseOutputWithKeyboard(GoodsType.CORN);
        layout.verifyOutput(GoodsType.CORN, 2);
        jobs.verifyFieldJob(0, Location.of(23, 16),
                org.microcol.model.Goods.of(GoodsType.CORN, 2));

        // A source job changed after rendering invalidates the captured offer.
        layout.changeSourceCropWithoutRefresh(GoodsType.LUMBER);
        jobs.rememberWorld();
        layout.chooseOutput(GoodsType.LUMBER);
        jobs.verifyWorldUnchanged();
        layout.verifyOutput(GoodsType.LUMBER, 2);
        layout.verifyActionExplanation(getContext()
                .getClassFromGuice(org.microcol.i18n.I18n.class)
                .get(org.microcol.gui.screen.colony.ColonyMsg.fieldChanged));
        town.close();
    }

}
