package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.*;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Real coastal upgrades unlock fishing and ship construction at their proper
 * tiers.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_51_F42_coastal_facilities_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F42-coastal-facilities.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_dock_enables_fishing_without_changing_rejected_work()
            throws Exception {
        // Without a dock, dragging a real worker to water explains the missing
        // facility.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        colony.rememberWorld();
        colony.dragFieldWorkerTo(0, Direction.east);
        colony.verifyThatStatusBarContains(
                "Build a dock before assigning fishing work.");
        colony.verifyWorldUnchanged();
        ConstructionPage building = new ConstructionPage(getContext());
        building.open();
        building.select("DOCK");
        building.verifyOffer(true, "Available.", 52, 0);
        building.cancel();
        colony.verifyWorldUnchanged();
        // Complete the actual dock, keeping the same original worker.
        building.open();
        building.select("DOCK");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.DOCK, 1);
        colony.rememberWorld();
        colony.cancelFieldWorkerDrag(0, Direction.east);
        colony.verifyWorldUnchanged();
        colony.dragFieldWorkerTo(0, Direction.east);
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.verifyFieldJob(0, Location.of(25, 16),
                Goods.of(GoodsType.CORN, 3));
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        building.verifyBuilding(ConstructionType.DOCK, 1);
        jobs.verifyFieldJob(0, Location.of(25, 16),
                Goods.of(GoodsType.CORN, 3));
    }

    @Test
    @Tag("local")
    void TC_02_shipyard_retains_fishing_and_completes_both_supported_ships()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage building = new ConstructionPage(getContext());
        CoastalFacilitiesPage coast = new CoastalFacilitiesPage(getContext());
        // Neither an absent dock nor the dock itself allows a ship project.
        building.open();
        building.select("GALLEON");
        building.verifyOffer(false,
                "Build a shipyard before constructing ships.", 100, 100);
        building.select("DOCK");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.open();
        building.select("FRIGATE");
        building.verifyOffer(false,
                "Build a shipyard before constructing ships.", 100, 100);
        building.cancel();
        colony.dragFieldWorkerTo(0, Direction.east);
        // Arrange sufficient materials; each upgrade is still ordered through
        // the picker.
        coast.prepareProjectStocks(77, 50);
        building.open();
        building.select("DRYDOCK");
        building.verifyOffer(true, "Available.", 80, 50);
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.DRYDOCK, 1);
        building.open();
        building.select("GALLEON");
        building.verifyOffer(false,
                "Build a shipyard before constructing ships.", 100, 100);
        building.cancel();
        coast.prepareProjectStocks(237, 100);
        building.open();
        building.select("SHIPYARD");
        building.verifyOffer(true, "Available.", 240, 100);
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.SHIPYARD, 1);
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.verifyFieldJob(0, Location.of(25, 16),
                Goods.of(GoodsType.CORN, 3));
        // Cancel leaves the world intact, then real construction creates each
        // ship once.
        colony.rememberWorld();
        building.open();
        building.select("GALLEON");
        building.verifyOffer(true, "Available.", 100, 100);
        building.cancel();
        colony.verifyWorldUnchanged();
        coast.prepareProjectStocks(97, 100);
        building.open();
        building.select("GALLEON");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        coast.verifyShips(UnitType.GALLEON, 2);
        coast.verifyStocks(0, 0);
        coast.prepareProjectStocks(97, 100);
        building.open();
        building.select("FRIGATE");
        building.verifyOffer(true, "Available.", 100, 100);
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        coast.verifyShips(UnitType.FRIGATE, 1);
        coast.verifyStocks(0, 0);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        coast.verifyShips(UnitType.GALLEON, 2);
        coast.verifyShips(UnitType.FRIGATE, 1);
        building.verifyBuilding(ConstructionType.SHIPYARD, 1);
        jobs.verifyFieldJob(0, Location.of(25, 16),
                Goods.of(GoodsType.CORN, 3));
    }
}
