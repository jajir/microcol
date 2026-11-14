package org.akropia.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.ConstructionType;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.gui.Tile;
import org.akropia.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_195_F227_colony_layout_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F227-colony-layout.akropia"));
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
        layout.revealProductionChoices();
        layout.verifyProductionChoices(0, GoodsType.WOOL, GoodsType.LUMBER);
        layout.verifyPreviewMatchesMap(0, 1);
        layout.hoverProductionChoices();
        layout.verifyTileFacts(0);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0362-production-en.png");
        jobs.verifyWorldUnchanged();

        // An explicit crop choice updates this worker, the inspector and
        // forecast; the worker stays here although grain yields more nearby.
        layout.moveSelection(KeyCode.ENTER);
        layout.verifyOutputFocused(GoodsType.LUMBER);
        layout.chooseOutput(GoodsType.GRAIN);
        layout.verifyOutput(GoodsType.GRAIN, 2);
        jobs.verifyFieldJob(0, Location.of(23, 16),
                org.akropia.model.Goods.of(GoodsType.GRAIN, 2));
        layout.revealProductionChoices();
        layout.verifyProductionChoices(0, GoodsType.WOOL, GoodsType.GRAIN);
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
        layout.verifyTileClickKeepsScroll();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0362-production-small.png");
        layout.revealProductionChoices();
        layout.verifyProductionChoices(0, GoodsType.WOOL, GoodsType.GRAIN);
        layout.verifyPreviewMatchesMap(0, 1);
        layout.hoverProductionChoices();
        layout.verifyTileFacts(0);
        layout.selectTile(1, 1);
        layout.showStock();
        town.verifyWarehouseGoodsLayout(19, 45);

        // Each stock shares one row with its nonzero change; torches and owls
        // show only their change.
        town.verifyGoodsForecast(GoodsType.GRAIN, 100, -2);
        town.verifyGoodsForecast(GoodsType.HORSE, 50, 2);
        town.verifyGoodsForecast(GoodsType.TOOLS, 0, 1);
        town.verifyGoodsForecast(GoodsType.LUMBER, 0, 0);
        town.verifyGoodsForecast(GoodsType.WEAPONS, 50, 0);
        town.verifyLocalProduction(GoodsType.HAMMERS, "Hammers", "0", "");
        town.verifyLocalProduction(GoodsType.TORCHES, "Torches", "", "+1");
        town.verifyLocalProduction(GoodsType.OWLS, "Owls", "", "+4");
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
        layout.verifyConditionalActions(true, false);
        layout.verifyActionNames(true, "Vývoz", "Zpět do hry");
        layout.verifyCompactBuildings(46, 22, 32, 12);
        layout.verifyBuildingLevel("id_fort", ConstructionType.FORTRESS);
        layout.verifyBuildingLevel("id_warehouse",
                ConstructionType.WAREHOUSE_EXPANSION);
        layout.verifyBuildingLevel("id_dock", ConstructionType.SHIPYARD);
        layout.verifyCenter();
        layout.selectTile(0, 1);
        layout.verifyTileClickKeepsScroll();
        layout.revealProductionChoices();
        layout.verifyProductionChoices(1, GoodsType.LUMBER, GoodsType.GRAIN);
        layout.verifyPreviewMatchesMap(0, 1);
        layout.hoverProductionChoices();
        new InGameLayoutPage(getContext())
                .saveEvidence("colony-hover-czech.png");
        layout.verifyTileFacts(1);
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

        // The narrow Czech stock row keeps one value line per product.
        layout.showStock();
        town.verifyWarehouseGoodsLayout(19, 45);
        town.verifyGoodsForecast(GoodsType.GRAIN, 100, -2);
        town.verifyGoodsForecast(GoodsType.LUMBER, 0, 6);
        town.verifyGoodsForecast(GoodsType.TOOLS, 50, 0);
        town.verifyLocalProduction(GoodsType.HAMMERS, "Kladiva", "0", "");
        town.verifyLocalProduction(GoodsType.TORCHES, "Pochodně", "", "+3");
        town.verifyLocalProduction(GoodsType.OWLS, "Sovy", "", "+2");
        final ColonyScreen other = town.close()
                .openColonyAt(Location.of(24, 16), "New Amsterdam");
        layout.verifyConditionalActions(false, false);
        other.close();
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
        layout.verifyActionNames(true, "Back to game");
        layout.verifyConditionalActions(false, false);
        new InGameLayoutPage(getContext()).saveEvidence("r0360-actions-en.png");
        bars.resizeContent(1000, 925);
        layout.verifyActionNames(true, "Back to game");
        layout.verifyConditionalActions(false, false);

        // Every icon explains its action; disabled exports explain the
        // prerequisite.
        layout.hoverAction("closeButtonId");
        layout.verifyActionExplanation(
                "Close the colony and return to the world map.");
        layout.hoverActionGap();
        layout.verifyNoActionExplanation();
        layout.hoverAction("closeButtonId");
        layout.leaveActions();
        layout.verifyNoActionExplanation();
        jobs.verifyWorldUnchanged();

        // Expanding restores the complete labels and the original return
        // action.
        bars.resizeContent(1600, 1000);
        layout.verifyActionNames(true, "Back to game");
        layout.verifyConditionalActions(false, false);
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
        layout.verifyActionNames(true, "Zpět do hry");
        layout.verifyConditionalActions(false, false);
        new InGameLayoutPage(getContext()).saveEvidence("r0360-actions-cs.png");
        bars.resizeContent(1000, 925);
        layout.verifyActionNames(true, "Zpět do hry");
        layout.verifyConditionalActions(false, false);
        layout.hoverActionGap();
        layout.verifyNoActionExplanation();
        layout.hoverAction("closeButtonId");
        layout.leaveActions();
        layout.verifyNoActionExplanation();
        bars.resizeContent(1600, 1000);
        layout.verifyActionNames(true, "Zpět do hry");
        layout.verifyConditionalActions(false, false);
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
        layout.verifyTileClickKeepsScroll();
        layout.moveSelection(KeyCode.SPACE);
        layout.verifyOutputFocused(GoodsType.LUMBER);
        layout.chooseOutputWithKeyboard(GoodsType.GRAIN);
        layout.verifyOutput(GoodsType.GRAIN, 2);
        jobs.verifyFieldJob(0, Location.of(23, 16),
                org.akropia.model.Goods.of(GoodsType.GRAIN, 2));
        layout.revealProductionChoices();
        layout.verifyProductionChoices(0, GoodsType.WOOL, GoodsType.GRAIN);

        // A source job changed after rendering invalidates the captured offer.
        layout.changeSourceCropWithoutRefresh(GoodsType.LUMBER);
        jobs.rememberWorld();
        layout.chooseOutput(GoodsType.LUMBER);
        jobs.verifyWorldUnchanged();
        layout.verifyOutput(GoodsType.LUMBER, 2);
        layout.verifyActionExplanation(
                getContext().getClassFromGuice(org.akropia.i18n.I18n.class).get(
                        org.akropia.gui.screen.colony.ColonyMsg.fieldChanged));
        town.close();
    }

    @Test
    @Tag("local")
    void TC_06_overviews_reorder_and_inline_editors() throws Exception {
        // Simplified presentation cards preserve the colony and its jobs.
        final ColonyScreen town = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(Location.of(24, 16), "New Amsterdam");
        final ColonyLayoutPage layout = new ColonyLayoutPage(getContext());
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        new GameBarWidthPage(getContext()).resizeContent(1440, 1000);
        layout.rememberWorkspaceGeometry();
        layout.verifyEmptyConstructionMeters();
        jobs.rememberWorld();
        layout.verifySimplifiedCards();
        layout.verifySectionOrder("colony-card-area",
                "colony-card-construction", "colony-card-school",
                "colony-cargo-depot");
        jobs.verifyWorldUnchanged();

        // The editors open below three summaries and the always-visible depot.
        layout.openSection("construction");
        layout.verifyDetail("constructionAdd");
        layout.verifyConstructionLists();
        layout.verifyNoSectionSeparators();
        layout.verifyStableWorkspaceGeometry();
        new InGameLayoutPage(getContext())
                .saveEvidence("colony-five-construction.png");
        layout.openSection("school");
        layout.verifyDetail("schoolPanel");
        layout.verifySchoolInputs();
        new InGameLayoutPage(getContext())
                .saveEvidence("colony-five-school.png");
        layout.verifyStableWorkspaceGeometry();
        final GamePage game = town.closeWithEscape();
        town.verifyClosed();
        game.openColonyWithKeyboard(0, "New Amsterdam");
        layout.openSection("construction");
        layout.verifyDetail("constructionAdd");
        town.closeWithEscape();
        town.verifyClosed();
        game.openColonyWithKeyboard(0, "New Amsterdam");
        layout.openSection("area");
        layout.verifyDetail("colony-field-name");
        layout.verifyStableWorkspaceGeometry();
        jobs.verifyWorldUnchanged();
        new InGameLayoutPage(getContext())
                .saveEvidence("colony-five-overviews.png");
        town.close();
    }

    @Test
    @Tag("local")
    void TC_07_depot_shows_selected_carrier_and_outside_units_together()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyLayoutPage layout = new ColonyLayoutPage(getContext());
        // Prepare a carrier at the colony before opening its overview.
        layout.clearDepot(0);
        final ColonyScreen emptyTown = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        layout.verifyEmptyDepot();
        new InGameLayoutPage(getContext())
                .saveEvidence("colony-empty-depot.png");
        emptyTown.close();
        layout.placeCarrierAtColony(3, 0);
        final int smallCarrier = layout.addSmallCarrier(0);
        final ColonyScreen town = game.openColonyAt(Location.of(24, 16),
                "New Amsterdam");
        layout.verifyDepot("Cargo Depot - Galleon #3");
        layout.verifyAvailableContainers(6);
        layout.verifyDepotSpacing();
        layout.hoverDepot();
        town.verifyThatStatusBarContains("The depot transfers goods and units");
        layout.hoverContainer();
        town.verifyThatStatusBarContains("Transport people, goods or animals");
        layout.hoverDepot();
        town.verifyThatStatusBarContains("The depot transfers goods and units");

        // Selecting or pressing a differently sized carrier keeps both icons
        // still.
        layout.rememberCarrierGeometry();
        layout.pressCarrier(smallCarrier);
        layout.verifyCarrierGeometryUnchanged();
        layout.releaseCarrier();
        layout.verifyCarrierGeometryUnchanged();
        layout.verifyAvailableContainers(2);
        town.selectShipInPort(3);
        layout.verifyCarrierGeometryUnchanged();
        layout.verifyAvailableContainers(6);
        layout.verifyDepot("Cargo Depot - Galleon #3");
        new InGameLayoutPage(getContext())
                .saveEvidence("colony-combined-depot.png");
        town.close();
    }

}
