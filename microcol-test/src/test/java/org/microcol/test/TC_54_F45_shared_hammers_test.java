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
 * Shared work survives queue edits and pays for only one completed head per
 * update.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_54_F45_shared_hammers_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F45-shared-hammers.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_shared_work_survives_changes_surplus_and_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        // Approved icons appear beside real production before queue actions.
        colony.verifyProductionArtwork("id_town_hall", "good-bell.png");
        colony.verifyProductionArtwork("id_church", "good-cross.png");
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
}
