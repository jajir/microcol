package org.akropia.test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.akropia.model.EuropeanNation;
import org.akropia.model.Location;
import org.akropia.model.NativeNation;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.TerrainType;
import org.akropia.model.UnitType;
import org.akropia.page.GamePage;
import org.akropia.page.MapEditorPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Authoring a playable scenario in the developer map editor (R-0435). Every
 * test creates a 12 × 10 ocean map and paints a grassland island in rows 3 to
 * 5, columns 3 to 6; the sea east of it carries the ship. The shared fixture
 * starts in developer mode.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_211_F242_map_editor_scenario_test extends AbstractAkropiaTest {

    private static final Location SEA = Location.of(9, 4);

    private static final Location SCOUT = Location.of(3, 5);

    private static final Location VILLAGE = Location.of(4, 4);

    private static final int CARAVEL_ID = 1;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.akropia"));
    }

    private static File scratch(final String name) throws Exception {
        return Files
                .createTempDirectory(Files.createDirectories(
                        Path.of("target", "map-editor")), "scenario-")
                .resolve(name).toFile();
    }

    /** Open the editor with a new ocean map and a painted island. */
    private MapEditorPage island() {
        final MapEditorPage editor = new MapEditorPage(getContext());
        editor.openFromMenu();
        editor.pressNew();
        editor.typeDimensions("12", "10");
        editor.confirmDimensions();
        // Paint the island square by square with the terrain tool.
        editor.chooseTerrain(TerrainType.GRASSLAND);
        for (int y = 3; y <= 5; y++) {
            for (int x = 3; x <= 6; x++) {
                editor.clickTile(Location.of(x, y));
            }
        }
        return editor;
    }

    @Test
    @Tag("local")
    void TC_01_terrain_forest_river_and_bonus_are_edited_and_removed()
            throws Exception {
        final MapEditorPage editor = island();
        final File file = scratch("terrain.akropia");

        // Dragging the terrain tool paints hills across a row.
        editor.chooseTerrain(TerrainType.HILL);
        editor.dragTiles(Location.of(3, 7), Location.of(6, 7));
        editor.verifyTile("Square [6, 7]: Hill");

        // Forest, rivers and an ore bonus on the island.
        editor.chooseTool("editorToolForest");
        editor.clickTile(Location.of(3, 3));
        editor.chooseRiver(RiverSize.SMALL);
        editor.clickTile(Location.of(4, 4));
        editor.chooseRiver(RiverSize.LARGE);
        editor.clickTile(Location.of(5, 4));
        editor.captureDialog("mapEditor", "r0434-editor-rivers.png");
        editor.chooseBonus(ResourceOverlay.ORE);
        editor.clickTile(Location.of(6, 5));

        // A bonus on the ocean is refused and explained.
        editor.clickTile(Location.of(9, 9));
        editor.verifyMessage("Nothing was changed. Only a land square can"
                + " carry this. [9, 9]");

        // A forest on a river square is refused by the terrain rules.
        editor.chooseTool("editorToolForest");
        editor.clickTile(Location.of(5, 4));
        editor.verifyMessageContains(
                "Nothing was changed. The terrain rules do not allow this.");

        // A click plants a forest; from there the keyboard moves the
        // selection and applies the same tool.
        editor.clickTile(Location.of(3, 4));
        editor.pressMapKeys(KeyCode.DOWN, KeyCode.RIGHT, KeyCode.ENTER);
        editor.verifyTile("Square [4, 5]: Grass land");
        editor.verifyControl("editorRemoveForest", true);

        // The square panel removes the forest at (3,3) and the small river.
        editor.chooseTool("editorToolSelect");
        editor.clickTile(Location.of(3, 3));
        editor.clickRemove("editorRemoveForest");
        editor.verifyControl("editorRemoveForest", false);
        editor.clickTile(Location.of(4, 4));
        editor.clickRemove("editorRemoveRiver");
        editor.verifyControl("editorRemoveRiver", false);

        // The saved scenario holds exactly these edits.
        editor.chooseSaveFile(file);
        editor.pressSave();
        editor.verifySavedTerrain(file, Location.of(4, 7), TerrainType.HILL);
        editor.verifySavedTerrain(file, Location.of(6, 7), TerrainType.HILL);
        editor.verifySavedForest(file, Location.of(3, 3), false);
        editor.verifySavedForest(file, Location.of(3, 4), true);
        editor.verifySavedForest(file, Location.of(4, 5), true);
        editor.verifySavedRiver(file, Location.of(4, 4), RiverSize.NONE);
        editor.verifySavedRiver(file, Location.of(5, 4), RiverSize.LARGE);
        editor.verifySavedBonus(file, Location.of(6, 5), ResourceOverlay.ORE);
        editor.verifySavedBonus(file, Location.of(9, 9), ResourceOverlay.NONE);

        // Removing the bonus and painting the river square as ocean leaves
        // neither behind.
        editor.clickTile(Location.of(6, 5));
        editor.clickRemove("editorRemoveBonus");
        editor.chooseTerrain(TerrainType.OCEAN);
        editor.clickTile(Location.of(5, 4));
        editor.pressSave();
        editor.verifySavedBonus(file, Location.of(6, 5), ResourceOverlay.NONE);
        editor.verifySavedRiver(file, Location.of(5, 4), RiverSize.NONE);
        editor.verifySavedTerrain(file, Location.of(5, 4), TerrainType.OCEAN);
    }

    @Test
    @Tag("local")
    void TC_02_native_settlement_is_placed_edited_and_removed()
            throws Exception {
        final MapEditorPage editor = island();
        final File file = scratch("natives.akropia");

        // An Inca capital on the island.
        editor.chooseTribe(NativeNation.INCA, true);
        editor.clickTile(VILLAGE);
        editor.verifyControl("editorSettlementRemove", true);

        // A second Inca capital and a settlement at sea are refused.
        editor.clickTile(Location.of(6, 4));
        editor.verifyMessage("Nothing was changed. This tribe already has a"
                + " capital. [6, 4]");
        editor.clickTile(SEA);
        editor.verifyMessage("Nothing was changed. A native settlement needs"
                + " land without mountains, arctic or a river. [9, 4]");

        // The settlement form changes the tribe, capital and teaching skill.
        editor.chooseTool("editorToolSelect");
        editor.clickTile(VILLAGE);
        editor.chooseSettlementForm(NativeNation.SIOUX, true,
                UnitType.SEASONED_SCOUT);
        editor.chooseSaveFile(file);
        editor.pressSave();
        editor.verifySavedSettlement(file, VILLAGE, NativeNation.SIOUX, true,
                UnitType.SEASONED_SCOUT);
        editor.verifySavedSettlementCount(file, 1);
        editor.captureDialog("mapEditor", "r0434-editor-capital.png");

        // Removing it leaves no settlement.
        editor.clickRemove("editorSettlementRemove");
        editor.verifyControl("editorSettlementRemove", false);
        editor.pressSave();
        editor.verifySavedSettlementCount(file, 0);
    }

    @Test
    @Tag("local")
    void TC_03_nation_units_and_passengers_survive_save_and_reopen()
            throws Exception {
        final MapEditorPage editor = island();
        final File file = scratch("expedition.akropia");

        // A caravel at sea and a scout on land for England.
        editor.chooseNation(EuropeanNation.ENGLISH);
        editor.chooseUnit(UnitType.CARAVEL);
        editor.clickTile(SEA);
        editor.chooseUnit(UnitType.SEASONED_SCOUT);
        editor.clickTile(SCOUT);

        // A colonist cannot stand at sea.
        editor.chooseUnit(UnitType.COLONIST);
        editor.clickTile(Location.of(8, 8));
        editor.verifyMessage("Nothing was changed. This unit cannot stand on"
                + " this terrain. [8, 8]");

        // The caravel takes two passengers and refuses a third.
        editor.chooseTool("editorToolSelect");
        editor.clickTile(SEA);
        editor.verifyUnitLabel(CARAVEL_ID, "Caravel #1");
        editor.choosePassenger(CARAVEL_ID, UnitType.COLONIST);
        editor.choosePassenger(CARAVEL_ID, UnitType.HARDY_PIONEER);
        editor.choosePassenger(CARAVEL_ID, UnitType.COLONIST);
        editor.verifyMessage(
                "Nothing was changed. The ship has no free hold. [9, 4]");
        editor.captureDialog("mapEditor", "r0434-editor-expedition.png");

        // With the ship and its passengers selected, every text stays whole
        // in 900 and 1000 pixel wide windows beside a usable map.
        editor.resizeWindow(900, 750);
        editor.verifyLayoutFitsWindow(300);
        editor.captureDialog("mapEditor", "r0435-editor-expedition-900.png");
        editor.resizeWindow(1000, 750);
        editor.verifyLayoutFitsWindow(300);
        editor.captureDialog("mapEditor", "r0435-editor-expedition-1000.png");

        // Saving and reopening keeps the nation, positions and passengers.
        editor.chooseSaveFile(file);
        editor.pressSave();
        editor.pressNew();
        editor.typeDimensions("10", "10");
        editor.confirmDimensions();
        editor.chooseOpenFile(file);
        editor.pressOpen();
        editor.verifySavedExpedition(file, EuropeanNation.ENGLISH, SEA,
                UnitType.CARAVEL,
                List.of(UnitType.COLONIST, UnitType.HARDY_PIONEER), SCOUT,
                UnitType.SEASONED_SCOUT);
        editor.chooseTool("editorToolSelect");
        editor.clickTile(SEA);
        editor.verifyUnitLabel(CARAVEL_ID, "Caravel #1");

        // A removed passenger leaves the ship; the scout is unit 2, the
        // colonist 3 and the pioneer 4.
        editor.clickRemove("editorRemoveUnit-4");
        editor.pressSave();
        editor.verifySavedExpedition(file, EuropeanNation.ENGLISH, SEA,
                UnitType.CARAVEL, List.of(UnitType.COLONIST), SCOUT,
                UnitType.SEASONED_SCOUT);
    }

    @Test
    @Tag("local")
    void TC_04_unplayable_draft_stays_and_valid_one_starts_exactly()
            throws Exception {
        final MapEditorPage editor = new MapEditorPage(getContext());
        final File file = scratch("playable.akropia");
        editor.openFromMenu();
        editor.pressNew();
        editor.typeDimensions("12", "10");
        editor.confirmDimensions();

        // An ocean without an expedition cannot be played.
        editor.pressPlay();
        editor.verifyEditorShown(true);
        editor.verifyMessage("The scenario cannot be played yet. Place at"
                + " least one unit of your expedition.");

        // An island, a Scythian village without a capital and the expedition.
        editor.chooseTerrain(TerrainType.GRASSLAND);
        for (int y = 3; y <= 5; y++) {
            for (int x = 3; x <= 6; x++) {
                editor.clickTile(Location.of(x, y));
            }
        }
        editor.chooseNation(EuropeanNation.SPANISH);
        editor.chooseTribe(NativeNation.SIOUX, false);
        editor.clickTile(VILLAGE);
        editor.chooseUnit(UnitType.CARAVEL);
        editor.clickTile(SEA);
        editor.chooseUnit(UnitType.SEASONED_SCOUT);
        editor.clickTile(SCOUT);
        editor.chooseTool("editorToolSelect");
        editor.clickTile(SEA);
        editor.choosePassenger(CARAVEL_ID, UnitType.COLONIST);
        editor.choosePassenger(CARAVEL_ID, UnitType.EXPERT_FARMER);

        // A tribe without a capital is named with its square, which is
        // selected.
        editor.pressPlay();
        editor.verifyEditorShown(true);
        editor.verifyMessage("The scenario cannot be played yet. A tribe with"
                + " settlements needs a capital. (Scythians) [4, 4]");
        editor.verifyTile("Square [4, 4]: Grass land");

        // With a capital the saved scenario starts exactly as authored and
        // stays unchanged.
        editor.chooseSettlementForm(NativeNation.SIOUX, true,
                UnitType.SEASONED_SCOUT);
        editor.chooseSaveFile(file);
        editor.pressSave();
        final String saved = Files.readString(file.toPath(),
                StandardCharsets.UTF_8);
        editor.pressPlay();
        editor.verifyEditorShown(false);
        editor.verifyStartedGame(EuropeanNation.SPANISH, 12, 10, SEA,
                UnitType.CARAVEL,
                List.of(UnitType.COLONIST, UnitType.EXPERT_FARMER), SCOUT,
                UnitType.SEASONED_SCOUT, VILLAGE, NativeNation.SIOUX);
        editor.verifyFileContent(file, saved);

        // Two whole turns keep the authored expedition, the passengers and
        // the village; control returns to the player each time.
        final GamePage game = GamePage.of(getContext());
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        editor.verifyGameAfterTurns(EuropeanNation.SPANISH, 2, SEA,
                List.of(UnitType.COLONIST, UnitType.EXPERT_FARMER), 4, VILLAGE,
                NativeNation.SIOUX);
    }

}
