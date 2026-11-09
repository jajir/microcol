package org.akropia.test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.akropia.model.Location;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.TerrainType;
import org.akropia.page.DeveloperModePage;
import org.akropia.page.MapEditorPage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Developer map editor lifecycle (R-0435): developer-only entry, a new ocean
 * map with rejected and canceled sizes, the unsaved-changes guard, saving and
 * reopening a draft, and failed or canceled files that keep the current draft.
 * The shared fixture starts in developer mode.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_210_F241_map_editor_lifecycle_test extends AbstractAkropiaTest {

    private static final Location LAND = Location.of(4, 4);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F15-free-play-audit.akropia"));
    }

    private static Path scratch() throws Exception {
        return Files.createTempDirectory(
                Files.createDirectories(Path.of("target", "map-editor")),
                "lifecycle-");
    }

    @Test
    @Tag("local")
    void TC_01_editor_is_offered_only_in_developer_mode() throws Exception {
        final MapEditorPage editor = new MapEditorPage(getContext());

        // Developer mode shows Map editor right of Settings.
        editor.verifyMenuEntry(true, "Map editor");

        // Without developer mode the entry is hidden and the screen route is
        // refused.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        new DeveloperModePage(getContext()).setModeInSettings(false);
        settings.goBack();
        editor.verifyMenuEntry(false, "Map editor");
        editor.requestEditorDirectly();
        editor.verifyEditorShown(false);

        // Turning developer mode on again offers the entry, which opens the
        // editor with an empty ocean draft.
        WelcomePage.of(getContext()).openSetting();
        new DeveloperModePage(getContext()).setModeInSettings(true);
        settings.goBack();
        editor.verifyMenuEntry(true, "Map editor");
        editor.openFromMenu();
        editor.verifyEditorShown(true);
        editor.verifyTitle("Unsaved scenario, 40 × 30 squares");
    }

    @Test
    @Tag("local")
    void TC_02_new_map_rejects_invalid_sizes_and_cancel_keeps_the_draft()
            throws Exception {
        final MapEditorPage editor = new MapEditorPage(getContext());
        editor.openFromMenu();

        // Sizes that are no whole numbers or outside the bounds are explained
        // in the dialog, which stays open.
        editor.pressNew();
        editor.typeDimensions("abc", "12");
        editor.confirmDimensions();
        editor.verifyDimensionDialog("Width and height must be whole numbers.");
        editor.typeDimensions("9", "12");
        editor.confirmDimensions();
        editor.verifyDimensionDialog(
                "Width and height must be from 10 to 100 squares.");
        editor.typeDimensions("12", "101");
        editor.confirmDimensions();
        editor.verifyDimensionDialog(
                "Width and height must be from 10 to 100 squares.");

        // Cancel keeps the current draft.
        editor.cancelDimensions();
        editor.verifyNoDimensionDialog();
        editor.verifyTitle("Unsaved scenario, 40 × 30 squares");

        // Valid sizes create an ocean map without settlements or units.
        editor.pressNew();
        editor.typeDimensions("12", "10");
        editor.confirmDimensions();
        editor.verifyTitle("Unsaved scenario, 12 × 10 squares");
        editor.verifyMessage("New ocean map 12 × 10 squares created.");
        editor.clickTile(Location.of(12, 10));
        editor.verifyTile("Square [12, 10]: Ocean");
        final File ocean = scratch().resolve("ocean.akropia").toFile();
        editor.chooseSaveFile(ocean);
        editor.pressSave();
        editor.verifySavedOcean(ocean, 12, 10);
    }

    @Test
    @Tag("local")
    void TC_03_unsaved_changes_are_protected_saved_and_reopened()
            throws Exception {
        final MapEditorPage editor = new MapEditorPage(getContext());
        final File draft = scratch().resolve("draft.akropia").toFile();
        editor.openFromMenu();
        editor.pressNew();
        editor.typeDimensions("10", "10");
        editor.confirmDimensions();

        // Paint one grassland square.
        editor.chooseTerrain(TerrainType.GRASSLAND);
        editor.clickTile(LAND);
        editor.verifyTitle(
                "Unsaved scenario, 10 × 10 squares, unsaved changes");

        // Cancel on New keeps the changed draft.
        editor.pressNew();
        editor.verifyUnsavedQuestionShown();
        editor.answerUnsavedCancel();
        editor.verifyNoDimensionDialog();
        editor.verifyTile("Square [4, 4]: Grass land");

        // Leaving with Escape asks too; Cancel stays in the editor.
        editor.pressEscape();
        editor.answerUnsavedCancel();
        editor.verifyEditorShown(true);

        // Save from the guard writes the unfinished draft and continues.
        editor.chooseSaveFile(draft);
        editor.pressBack();
        editor.answerUnsavedSave();
        editor.verifyEditorShown(false);
        editor.verifySavedTerrain(draft, LAND, TerrainType.GRASSLAND);

        // The draft reopens for further editing.
        editor.openFromMenu();
        editor.pressNew();
        editor.typeDimensions("20", "15");
        editor.confirmDimensions();
        editor.chooseOpenFile(draft);
        editor.pressOpen();
        editor.verifyTitle("draft.akropia, 10 × 10 squares");
        editor.verifyMessage("Opened draft.akropia.");
        editor.chooseTool("editorToolSelect");
        editor.clickTile(LAND);
        editor.verifyTile("Square [4, 4]: Grass land");

        // Discard on leaving returns to the saved state for the next visit.
        editor.chooseTerrain(TerrainType.HILL);
        editor.clickTile(LAND);
        editor.pressBack();
        editor.answerUnsavedDiscard();
        editor.verifyEditorShown(false);
        editor.openFromMenu();
        editor.verifyTitle("draft.akropia, 10 × 10 squares");
        editor.chooseTool("editorToolSelect");
        editor.clickTile(LAND);
        editor.verifyTile("Square [4, 4]: Grass land");
    }

    @Test
    @Tag("local")
    void TC_04_failed_or_canceled_files_keep_the_draft() throws Exception {
        final MapEditorPage editor = new MapEditorPage(getContext());
        final Path dir = scratch();
        final File broken = dir.resolve("broken.akropia").toFile();
        Files.writeString(broken.toPath(), "{\"model\": [1, 2",
                StandardCharsets.UTF_8);
        final File kept = dir.resolve("kept.akropia").toFile();
        editor.openFromMenu();
        editor.pressNew();
        editor.typeDimensions("10", "10");
        editor.confirmDimensions();
        editor.chooseSaveFile(kept);
        editor.pressSave();
        final String keptContent = Files.readString(kept.toPath(),
                StandardCharsets.UTF_8);
        editor.chooseTerrain(TerrainType.GRASSLAND);
        editor.clickTile(LAND);

        // A damaged file is refused; the changed draft stays open.
        editor.chooseOpenFile(broken);
        editor.pressOpen();
        editor.answerUnsavedDiscard();
        editor.verifyMessage("broken.akropia could not be opened; the current"
                + " map stays open. The file is not a valid scenario.");
        editor.verifyTitle("kept.akropia, 10 × 10 squares, unsaved changes");
        editor.verifyTile("Square [4, 4]: Grass land");

        // Canceling Open or Save keeps the draft and the previous file.
        editor.chooseOpenFile(null);
        editor.pressOpen();
        editor.answerUnsavedDiscard();
        editor.verifyMessage(
                "Opening was canceled; the current map stays open.");
        editor.chooseSaveFile(null);
        editor.pressSave();
        editor.verifyMessage("Saving was canceled; the map was not saved.");
        editor.verifyTitle("kept.akropia, 10 × 10 squares, unsaved changes");
        editor.verifyFileContent(kept, keptContent);

        // A write into a missing folder fails without reporting success.
        editor.chooseSaveFile(
                dir.resolve("missing").resolve("new.akropia").toFile());
        editor.pressSave();
        editor.verifyMessage("Saving to new.akropia failed; the previous file"
                + " is kept and the map stays unsaved.");
        editor.verifyTitle("kept.akropia, 10 × 10 squares, unsaved changes");
        editor.verifyFileContent(kept, keptContent);
    }

    @Test
    @Tag("local")
    void TC_05_czech_compact_editor_is_used_from_the_keyboard()
            throws Exception {
        final MapEditorPage editor = new MapEditorPage(getContext());
        final File file = scratch().resolve("klavesnice.akropia").toFile();

        // Czech texts in a compact window.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();
        editor.resizeWindow(1000, 700);
        editor.verifyMenuEntry(true, "Editor map");
        editor.openFromMenu();
        editor.verifyTitle("Neuložený scénář, 40 × 30 polí");
        editor.verifyLayoutFitsWindow(300);

        // Czech size feedback, then a new map.
        editor.pressNew();
        editor.typeDimensions("5", "10");
        editor.confirmDimensions();
        editor.verifyDimensionDialog(
                "Šířka a výška musí být od 10 do 100 polí.");
        editor.typeDimensions("12", "10");
        editor.confirmDimensions();
        editor.verifyMessage("Vytvořena nová oceánská mapa 12 × 10 polí.");

        // A click paints one square; arrows and Enter or Space paint the
        // next ones.
        editor.chooseTerrain(TerrainType.GRASSLAND);
        editor.clickTile(Location.of(2, 2));
        editor.pressMapKeys(KeyCode.RIGHT, KeyCode.ENTER, KeyCode.DOWN,
                KeyCode.SPACE);
        editor.verifyTile("Pole [3, 3]: Zelená země");
        editor.verifyMessage("Pole [3, 3] změněno.");

        // A refused edit is explained in Czech.
        editor.chooseBonus(ResourceOverlay.ORE);
        editor.clickTile(Location.of(9, 9));
        editor.verifyMessage(
                "Nic se nezměnilo. To může nést jen pevnina. [9, 9]");
        editor.verifyLayoutFitsWindow(300);
        editor.captureDialog("mapEditor", "r0434-editor-czech-compact.png");
        editor.captureDialog("mapEditor",
                "r0435-editor-czech-compact-1000.png");

        // The narrower window keeps every Czech text whole as well.
        editor.resizeWindow(900, 700);
        editor.verifyLayoutFitsWindow(300);
        editor.captureDialog("mapEditor", "r0435-editor-czech-compact-900.png");

        // The keyboard edits are saved; Escape then leaves the saved map.
        editor.chooseSaveFile(file);
        editor.pressSave();
        editor.verifyTitle("klavesnice.akropia, 12 × 10 polí");
        editor.verifySavedTerrain(file, Location.of(2, 2),
                TerrainType.GRASSLAND);
        editor.verifySavedTerrain(file, Location.of(3, 2),
                TerrainType.GRASSLAND);
        editor.verifySavedTerrain(file, Location.of(3, 3),
                TerrainType.GRASSLAND);
        editor.verifySavedTerrain(file, Location.of(2, 3), TerrainType.OCEAN);
        editor.pressEscape();
        editor.verifyEditorShown(false);
    }

}
