package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.ConstructionPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.TerrainIdentityPage;
import org.microcol.page.TerrainBoundaryPage;
import org.microcol.page.TerrainAppearancePage;
import org.microcol.page.WelcomePage;
import org.microcol.page.PassengerTransferPage;
import org.microcol.page.FogPage;
import org.microcol.page.FramedSettingsPage;
import org.microcol.page.SettingPage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Verify that plains, marsh, forested tundra and a fresh-water lake are shown
 * with their own names, that a lake behaves like ocean for ships and fishing,
 * and that Save and Continue preserve every identity and forest state.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_80_F72_terrain_identity_test extends AbstractMicroColTest {

    private final static Location PLAINS_FOREST = Location.of(16, 15);
    private final static Location MARSH_FOREST = Location.of(17, 15);
    private final static Location TUNDRA_FOREST = Location.of(16, 16);

    private final static Location COLONY = Location.of(24, 16);

    /** New Amsterdam. */
    private final static int COLONY_ID = 0;
    private final static Location LAKE_NEXT_TO_COLONY = Location.of(25, 16);
    private final static Location SHIP_START = Location.of(26, 16);
    private final static Location LAKE_EAST = Location.of(27, 16);
    private final static Location SHORE = Location.of(26, 15);

    /** Galleon of the coastal scenario. */
    private final static int SHIP_ID = 3;

    private final static File testFileName = new File(
            "src/test/scenarios/F42-coastal-facilities.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    /**
     * Load a scoped copy with the new terrains and a lake next to the colony.
     */
    private GamePage prepareAndLoad(final String name) throws Exception {
        TerrainIdentityFixture.prepare(getContext(), testFileName, name,
                SHIP_ID, SHIP_START, PLAINS_FOREST, TerrainType.PLAINS,
                MARSH_FOREST, TerrainType.MARSH, TUNDRA_FOREST,
                TerrainType.TUNDRA, LAKE_NEXT_TO_COLONY, TerrainType.LAKE,
                SHIP_START, TerrainType.LAKE, LAKE_EAST, TerrainType.LAKE);
        return WelcomePage.of(getContext()).loadGame();
    }

    @Test
    @Tag("local")
    void TC_06_coast_joins_survive_grid_pan_resize_and_continue()
            throws Exception {
        // Load a prepared corner with straight north and west coast joins.
        TerrainIdentityFixture.prepareCoasts(getContext(), testFileName,
                "terrain-r0330-coasts.microcol", SHIP_ID, SHIP_START);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final TerrainAppearancePage terrain = new TerrainAppearancePage(
                getContext());
        final FogPage map = new FogPage(getContext());
        final Location north = Location.of(18, 15), west = Location.of(17, 16);
        game.panToTile(north);
        game.panToTile(west);
        game.moveMouseAtLocation(Location.of(20, 18));
        map.saveMapEvidence("r0330-coasts-without-grid.png");
        terrain.verifyCoastJoins(north, west, TerrainType.GRASSLAND, false);

        // Moving and resizing the viewport keep the same clean coast joins.
        game.panToTile(SHIP_START);
        map.resizeMapWindow(1100, 800);
        game.panToTile(north);
        game.panToTile(west);
        game.moveMouseAtLocation(Location.of(20, 18));
        terrain.verifyCoastJoins(north, west, TerrainType.GRASSLAND, false);

        // Enable the ordinary grid setting and continue the saved game.
        game.exitToMenu();
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        new FramedSettingsPage(getContext()).click("settingGrid");
        settings.goBack();
        game = new FreePlayAuditPage(getContext()).continueGame();
        game.panToTile(north);
        game.panToTile(west);
        game.moveMouseAtLocation(Location.of(20, 18));
        map.saveMapEvidence("r0330-coasts-with-grid.png");
        terrain.verifyCoastJoins(north, west, TerrainType.GRASSLAND, true);
    }

    @Test
    @Tag("local")
    void TC_01_new_terrains_show_their_names_and_forest_and_survive_continue()
            throws Exception {
        // Load a prepared copy with forested plains, marsh and tundra.
        GamePage gamePage = prepareAndLoad("terrain-f72-names.microcol");
        final TerrainIdentityPage terrain = new TerrainIdentityPage(
                getContext());
        gamePage.panToTile(PLAINS_FOREST);

        // Each square shows its own name and keeps its forest.
        gamePage.moveMouseAtLocation(PLAINS_FOREST);
        gamePage.verifyThatStatusBarContains("Plains");
        terrain.verifyTerrain(PLAINS_FOREST, TerrainType.PLAINS, true);
        gamePage.moveMouseAtLocation(MARSH_FOREST);
        gamePage.verifyThatStatusBarContains("Marsh");
        terrain.verifyTerrain(MARSH_FOREST, TerrainType.MARSH, true);
        gamePage.moveMouseAtLocation(TUNDRA_FOREST);
        gamePage.verifyThatStatusBarContains("Tundra");
        terrain.verifyTerrain(TUNDRA_FOREST, TerrainType.TUNDRA, true);
        terrain.captureDialog("tileName", "r0274-terrain-map.png");
        // Inspect the stronger forest/coast ink with unchanged terrain
        // identity.
        terrain.captureDialog("tileName", "r0291-map-outlines.png");

        // Continue keeps every identity and forest state.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        gamePage.panToTile(PLAINS_FOREST);
        gamePage.moveMouseAtLocation(PLAINS_FOREST);
        gamePage.verifyThatStatusBarContains("Plains");
        terrain.verifyTerrain(PLAINS_FOREST, TerrainType.PLAINS, true);
        terrain.verifyTerrain(MARSH_FOREST, TerrainType.MARSH, true);
        terrain.verifyTerrain(TUNDRA_FOREST, TerrainType.TUNDRA, true);
    }

    @Test
    @Tag("local")
    void TC_02_ship_sails_the_lake_but_not_onto_land() throws Exception {
        // Load a prepared copy with the ship on a lake.
        final GamePage gamePage = prepareAndLoad("terrain-f72-ship.microcol");
        final TerrainIdentityPage terrain = new TerrainIdentityPage(
                getContext());
        gamePage.panToTile(SHIP_START);
        gamePage.moveMouseAtLocation(SHIP_START);
        gamePage.verifyThatStatusBarContains("Lake");
        terrain.verifyTerrain(SHIP_START, TerrainType.LAKE, false);

        // The selected ship sails to the next lake square.
        gamePage.selectTile(SHIP_START);
        gamePage.dragMouseAtLocation(LAKE_EAST);
        terrain.verifyShipAt(SHIP_ID, LAKE_EAST);

        // The same ship cannot enter the neighbouring land square.
        gamePage.selectTile(LAKE_EAST);
        gamePage.dragMouseAtLocation(SHORE);
        final PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        passengers.verifyMovementRejected();
        passengers.closeRejection();
        terrain.verifyShipAt(SHIP_ID, LAKE_EAST);
    }

    @Test
    @Tag("local")
    void TC_03_lake_fishing_needs_a_dock_and_survives_continue()
            throws Exception {
        // Without a dock a lake square explains the missing facility.
        GamePage gamePage = prepareAndLoad("terrain-f72-fishing.microcol");
        final TerrainIdentityPage terrain = new TerrainIdentityPage(
                getContext());
        terrain.verifyTerrain(LAKE_NEXT_TO_COLONY, TerrainType.LAKE, false);
        ColonyScreen colony = gamePage.openColonyAt(COLONY, "New Amsterdam");
        colony.rememberWorld();
        colony.dragFieldWorkerTo(0, Direction.east);
        colony.verifyThatStatusBarContains(
                "Build a dock before assigning fishing work.");
        colony.verifyWorldUnchanged();

        // A completed dock gives the ordinary three food of the ocean.
        final ConstructionPage building = new ConstructionPage(getContext());
        building.open();
        building.select("DOCK");
        building.confirm();
        building.cancel();
        gamePage = colony.close();
        gamePage.nextTurnAndCloseDialogs();
        colony = gamePage.openColonyAt(COLONY, "New Amsterdam");
        building.verifyBuilding(ConstructionType.DOCK, 1);
        colony.dragFieldWorkerTo(0, Direction.east);
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.verifyFieldJob(0, LAKE_NEXT_TO_COLONY,
                Goods.of(GoodsType.CORN, 3));

        // An actual turn stores the food the colony quoted, fish included.
        gamePage = colony.close();
        terrain.rememberFoodAndNetProduction(COLONY_ID);
        gamePage.nextTurnAndCloseDialogs();
        terrain.verifyFoodAfterTurn(COLONY_ID);

        // Continue keeps the lake identity and the fishing job.
        gamePage.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        terrain.verifyTerrain(LAKE_NEXT_TO_COLONY, TerrainType.LAKE, false);
        building.verifyBuilding(ConstructionType.DOCK, 1);
        jobs.verifyFieldJob(0, LAKE_NEXT_TO_COLONY,
                Goods.of(GoodsType.CORN, 3));
    }

    @Test
    @Tag("local")
    void TC_04_sparse_terrain_and_living_water_survive_pan_and_continue()
            throws Exception {
        // Load the terrain scenario and inspect the shared map renderer.
        GamePage game = prepareAndLoad("terrain-r0277.microcol");
        final TerrainAppearancePage terrain = new TerrainAppearancePage(
                getContext());
        terrain.verifySparseTerrainAndMovingWater();
        // High Seas keeps the full breaking/dissolving cycle and a visible
        // crest in stationary previews.
        terrain.verifyHighSeasWavesAndStaticPreview();
        // Lake K3 ripples expand and fade below coasts; fish keep priority.
        terrain.verifyLakeRipplesAndStaticPreview();
        terrain.rememberLandDecoration();
        // Panning away and back leaves the land decoration in place.
        game.panToTile(PLAINS_FOREST);
        game.panToTile(SHIP_START);
        terrain.verifyLandDecorationUnchanged();
        terrain.captureDialog("tileName", "r0293-wide-wash-map.png");
        // Saving and continuing preserves the layout and the forest identity.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        terrain.verifyLandDecorationUnchanged();
        new TerrainIdentityPage(getContext()).verifyTerrain(PLAINS_FOREST,
                TerrainType.PLAINS, true);
        // Continue retains full High Seas participation and the same wave
        // loop.
        terrain.verifyHighSeasWavesAndStaticPreview();
        terrain.verifyLakeRipplesAndStaticPreview();
    }

    @Test
    @Tag("local")
    void TC_05_hand_drawn_boundaries_survive_pan_and_continue()
            throws Exception {
        // Load an explored patch with three land colours and irregular joins.
        TerrainIdentityFixture.prepareBoundaries(getContext(), testFileName,
                "terrain-r0293.microcol", SHIP_ID, SHIP_START);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final TerrainBoundaryPage terrain = new TerrainBoundaryPage(
                getContext());
        game.panToTile(Location.of(16, 14));
        game.moveMouseAtLocation(Location.of(16, 14));
        game.verifyThatStatusBarContains("Grass land");
        terrain.verifyIrregularBoundariesOnMap();
        terrain.rememberBoundaryAppearance();
        terrain.captureDialog("tileName", "r0293-terrain-boundaries.png");

        // Pan away and return to the same stable map drawing.
        game.panToTile(SHIP_START);
        game.panToTile(Location.of(17, 14));
        terrain.verifyBoundaryAppearanceUnchanged();
        terrain.verifyIrregularBoundariesOnMap();

        // Save and Continue preserve the terrain data and the new boundaries.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        game.panToTile(Location.of(17, 14));
        terrain.verifyBoundaryAppearanceUnchanged();
        terrain.verifyIrregularBoundariesOnMap();
    }

}
