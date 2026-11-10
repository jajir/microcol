package org.akropia.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.*;
import org.akropia.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Shared work survives queue edits and pays for only one completed head per
 * update.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_54_F45_shared_hammers_test extends AbstractAkropiaTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F45-shared-hammers.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_shared_work_survives_changes_surplus_and_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        // Approved icons appear beside real production before queue actions.
        colony.verifyProductionArtwork("id_town_hall", "good-owls.png");
        colony.verifyProductionArtwork("id_church", "good-torches.png");
        colony.verifyProductionArtwork("id_carpenter", "good-hammer.png");
        ConstructionPage building = new ConstructionPage(getContext());
        building.open();
        building.select("LUMBER_MILL");
        building.confirm();
        building.select("WAREHOUSE");
        building.confirm();
        building.verifySharedHammerDisplay(180);
        // Reorder A to B and back without assigning the pool twice.
        building.selectQueue(1);
        building.moveUp();
        building.verifyQueue(List.of("WAREHOUSE", "LUMBER_MILL"), List.of(1, 0),
                180);
        building.moveDown();
        building.verifyQueue(List.of("LUMBER_MILL", "WAREHOUSE"), List.of(0, 1),
                180);
        building.selectQueue(0);
        building.remove();
        building.selectQueue(0);
        building.remove();
        building.verifyQueue(List.of(), List.of(), 180);
        building.cancel();
        colony.rememberWorld();
        building.open();
        building.select("LUMBER_MILL");
        building.cancel();
        colony.verifyWorldUnchanged();
        // Complete only the mill, retaining 131 hammers for the warehouse.
        building.open();
        building.select("LUMBER_MILL");
        building.confirm();
        building.select("WAREHOUSE");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.LUMBER_MILL, 1);
        building.verifyQueue(List.of("WAREHOUSE"), List.of(1), 131);
        building.open();
        building.select("WAGON");
        building.confirm();
        building.verifySharedHammerDisplay(131);
        building.captureDialog("constructionQueue", "r109-shared-hammers.png");
        building.cancel();
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyQueue(List.of("WAREHOUSE", "WAGON"), List.of(1, 2), 131);
        // Enough resources exist for both, but only the head completes this
        // update.
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.WAREHOUSE, 1);
        building.verifyQueue(List.of("WAGON"), List.of(2), 57);
        building.verifyResourceProgress("constructionWork", "Not required", "",
                "", 0, false);
        building.verifyResourceProgress("constructionTools", "100 / 40",
                "+0/turn", "Ready", 1, true);
        building.verifyForecast("Estimated turns: 1 (at current production).");
        building.verifyOutsideUnits(UnitType.WAGON, 0);
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyQueue(List.of(), List.of(), 63);
        building.verifyOutsideUnits(UnitType.WAGON, 1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 60));
        // An empty queue retains newly produced hammers and survives Continue.
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyQueue(List.of(), List.of(), 69);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        building.verifyQueue(List.of(), List.of(), 69);
        building.verifyOutsideUnits(UnitType.WAGON, 1);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0365-production-icons.png");
    }

    @Test
    @Tag("local")
    void TC_02_construction_meters_show_work_optional_tools_and_shortage()
            throws Exception {
        // Prepare a partially worked project with only twelve tools in stock.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        getContext().getRobot().interact(() -> {
            getContext().getPrimaryStage().setWidth(1440);
            getContext().getPrimaryStage().setHeight(1000);
        });
        getContext().getRobot().interact(() -> {
            final Colony modelColony = getModel().getColonyAt(COLONY)
                    .orElseThrow();
            modelColony.getWarehouse()
                    .removeGoods(Goods.of(GoodsType.HAMMERS, 144));
            modelColony.getWarehouse()
                    .removeGoods(Goods.of(GoodsType.TOOLS, 88));
        });
        final ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        final ConstructionPage building = new ConstructionPage(getContext());
        building.verifyEmptyProgress();

        // A second carpenter makes six hammers per turn; tools are not needed.
        building.assignCarpenter(4);
        building.open();
        building.select("LUMBER_MILL");
        building.confirm();
        building.cancel();
        building.verifyResourceProgress("constructionWork", "36 / 52",
                "+6/turn", "Turns: 3", 36.0 / 52, true);
        building.verifyResourceProgress("constructionTools", "Not required", "",
                "", 0, false);
        building.verifyStatus("Construction in progress.");
        building.verifyForecast("Estimated turns: 3 (at current production).");
        new InGameLayoutPage(getContext())
                .saveEvidence("construction-optional-tools.png");

        // Change the head to a smithy: work continues, but eight tools are
        // missing.
        building.open();
        building.selectQueue(0);
        building.remove();
        building.select("BLACKSMITHS_SHOP");
        building.confirm();
        building.cancel();
        building.verifyResourceProgress("constructionWork", "36 / 64",
                "+6/turn", "Turns: 5", 36.0 / 64, true);
        building.verifyResourceProgress("constructionTools", "12 / 20",
                "+0/turn", "8 missing", 0.6, true);
        building.verifyStatus("Waiting for tools: 8.");
        building.verifyForecast("Completion unknown at current production.");
        new InGameLayoutPage(getContext())
                .saveEvidence("construction-missing-tools.png");

        // Removing the last project removes both meters, without consuming
        // stock.
        building.open();
        building.selectQueue(0);
        building.remove();
        building.cancel();
        building.verifyEmptyProgress();
        colony.verifyWarehouseGoods(Goods.of(GoodsType.HAMMERS, 36));
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 12));
    }

    @Test
    @Tag("local")
    void TC_03_construction_without_carpenters_has_no_completion_estimate()
            throws Exception {
        // Prepare idle carpenters and insufficient work before opening the
        // city.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        getContext().getRobot().interact(() -> {
            getModel().getColonyAt(COLONY).orElseThrow().getWarehouse()
                    .removeGoods(Goods.of(GoodsType.HAMMERS, 144));
            getModel().getUnitById(200).placeToLocation(COLONY);
        });
        final ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        final ConstructionPage building = new ConstructionPage(getContext());
        // Warehouse needs no population increase or tools, so work is the
        // blocker.
        building.open();
        building.select("WAREHOUSE");
        building.confirm();
        building.cancel();
        building.verifyResourceProgress("constructionWork", "36 / 80",
                "+0/turn", "44 missing", 0.45, true);
        building.verifyResourceProgress("constructionTools", "Not required", "",
                "", 0, false);
        building.verifyStatus("Waiting for hammers.");
        building.verifyForecast("Completion unknown at current production.");
        colony.close();
    }
}
