package org.akropia.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.akropia.gui.FileSelectingService;
import org.akropia.gui.Point;
import org.akropia.gui.Tile;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.screen.game.gamepanel.Area;
import org.akropia.gui.screen.game.gamepanel.VisibleAreaService;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Location;
import org.akropia.model.NativeNation;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.TerrainType;
import org.akropia.model.UnitType;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.generation.ScenarioDraft;
import org.akropia.model.store.ModelPo;
import org.akropia.model.store.NativeSettlementPo;
import org.akropia.model.store.UnitPo;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.eventbus.EventBus;
import com.google.inject.Key;
import com.google.inject.name.Names;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Developer map editor (R-0435): its menu entry, top menu, tools, square panel,
 * files and launch. Saved scenarios are read back with the game's own scenario
 * reader.
 */
public final class MapEditorPage extends AbstractScreen {

    public MapEditorPage(final TestContext context) {
        super(context);
    }

    public void openFromMenu() {
        clickOnButtonWithId("buttonMapEditor");
    }

    /** Ask for the editor through the screen route instead of the menu. */
    public void requestEditorDirectly() {
        getRobot().interact(() -> getContext().getClassFromGuice(EventBus.class)
                .post(new ShowScreenEvent(Screen.EDITOR)));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void pressNew() {
        clickOnButtonWithId("editorNew");
    }

    public void pressOpen() {
        clickOnButtonWithId("editorOpen");
    }

    public void pressSave() {
        clickOnButtonWithId("editorSave");
    }

    public void pressPlay() {
        clickOnButtonWithId("editorPlay");
    }

    public void pressBack() {
        clickOnButtonWithId("editorBack");
    }

    public void pressEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void typeDimensions(final String width, final String height) {
        type("#editorWidth", width);
        type("#editorHeight", height);
    }

    private void type(final String query, final String text) {
        final TextField field = getNodeFinder().lookup(query)
                .queryAs(TextField.class);
        getRobot().interact(() -> field.setText(""));
        getRobot().clickOn(field).write(text);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmDimensions() {
        clickOnButtonWithId("editorDimensionConfirm");
    }

    public void cancelDimensions() {
        clickOnButtonWithId("editorDimensionCancel");
    }

    /** Answer the unsaved-changes question: Save, Discard or Cancel. */
    public void answerUnsavedSave() {
        clickOnButtonWithId("saveConfirm");
    }

    public void answerUnsavedDiscard() {
        clickOnButtonWithId("saveDiscard");
    }

    public void answerUnsavedCancel() {
        clickOnButtonWithId("saveCancel");
    }

    /** The next Save chooses this file; <code>null</code> cancels. */
    public void chooseSaveFile(final File file) {
        Mockito.when(chooser().saveScenario(ArgumentMatchers.any(File.class),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(file);
    }

    /** The next Open chooses this file; <code>null</code> cancels. */
    public void chooseOpenFile(final File file) {
        Mockito.when(chooser().openScenario(ArgumentMatchers.any(File.class),
                ArgumentMatchers.anyString())).thenReturn(file);
    }

    private FileSelectingService chooser() {
        return getContext().getClassFromGuice(FileSelectingService.class);
    }

    public void chooseTool(final String toolId) {
        clickOnButtonWithId(toolId);
    }

    /** Activate the terrain tool and choose the terrain it paints. */
    public void chooseTerrain(final TerrainType terrain) {
        clickOnButtonWithId("editorToolTerrain");
        choose("#editorTerrainChoice", terrain);
    }

    /** Activate the river tool and choose the river size. */
    public void chooseRiver(final RiverSize size) {
        clickOnButtonWithId("editorToolRiver");
        choose("#editorRiverChoice", size);
    }

    /** Activate the bonus tool and choose the bonus. */
    public void chooseBonus(final ResourceOverlay bonus) {
        clickOnButtonWithId("editorToolBonus");
        choose("#editorBonusChoice", bonus);
    }

    /** Activate the settlement tool and choose the tribe and capital. */
    public void chooseTribe(final NativeNation tribe, final boolean capital) {
        clickOnButtonWithId("editorToolSettlement");
        choose("#editorTribeChoice", tribe);
        check("#editorCapitalChoice", capital);
    }

    /** Activate the unit tool and choose the unit type. */
    public void chooseUnit(final UnitType type) {
        clickOnButtonWithId("editorToolUnit");
        choose("#editorUnitChoice", type);
    }

    public void chooseNation(final EuropeanNation nation) {
        choose("#editorNation", nation);
    }

    public void chooseSettlementForm(final NativeNation tribe,
            final boolean capital, final UnitType skill) {
        choose("#editorSettlementTribe", tribe);
        choose("#editorSettlementSkill", skill);
        check("#editorSettlementCapital", capital);
        clickOnButtonWithId("editorSettlementApply");
    }

    public void choosePassenger(final int shipId, final UnitType type) {
        choose("#editorPassengerType-" + shipId, type);
        clickOnButtonWithId("editorAddPassenger-" + shipId);
    }

    /** Open the list as a player does and pick the item. */
    @SuppressWarnings("unchecked")
    private <T> void choose(final String query, final T value) {
        final ComboBox<T> combo = getNodeFinder().lookup(query)
                .queryAs(ComboBox.class);
        revealGameContent(combo);
        getRobot().clickOn(combo);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(() -> {
            combo.getSelectionModel().select(value);
            combo.hide();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void check(final String query, final boolean selected) {
        final javafx.scene.control.CheckBox box = getNodeFinder().lookup(query)
                .queryAs(javafx.scene.control.CheckBox.class);
        if (box.isSelected() != selected) {
            revealGameContent(box);
            getRobot().clickOn(box);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    public void clickRemove(final String removeId) {
        clickOnButtonWithId(removeId);
    }

    public void clickTile(final Location location) {
        getRobot().clickOn(screenPoint(location), MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Paint by dragging from one square to another along a row. */
    public void dragTiles(final Location from, final Location to) {
        getRobot().drag(screenPoint(from), MouseButton.PRIMARY)
                .moveTo(screenPoint(to)).drop();
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Select a square with the arrow keys from the selected one. */
    public void pressMapKeys(final KeyCode... keys) {
        for (final KeyCode key : keys) {
            getRobot().push(key);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    private javafx.geometry.Point2D screenPoint(final Location location) {
        final VisibleAreaService area = getContext().getAkropia().getInjector()
                .getInstance(Key.get(VisibleAreaService.class,
                        Names.named("editor")));
        final Point point = new Area(area).convertToCanvasPoint(location);
        final Canvas canvas = getNodeFinder().lookup("#mapEditor")
                .lookup((Node node) -> node instanceof Canvas)
                .queryAs(Canvas.class);
        return canvas.localToScreen(point.getX() + Tile.TILE_WIDTH_IN_PX / 2,
                point.getY() + Tile.TILE_WIDTH_IN_PX / 2);
    }

    private static ModelPo read(final File file) {
        return ScenarioDraft.of(new GameModelPoDao().loadScenarioFromFile(file))
                .toScenario().getModel();
    }

    private String text(final String id) {
        return getLabeledById(id).getText();
    }

    public void verifyMenuEntry(final boolean shown, final String label) {
        final List<Node> found = getNodeFinder().lookup("#buttonMapEditor")
                .queryAll().stream().filter(Node::isVisible)
                .collect(Collectors.toList());
        assertEquals(shown, !found.isEmpty());
        if (shown) {
            assertEquals(label, ((Labeled) found.get(0)).getText());
            final Bounds editor = found.get(0)
                    .localToScene(found.get(0).getBoundsInLocal());
            final Node setting = getNodeFinder().lookup("#buttonSetting")
                    .query();
            final Bounds settings = setting
                    .localToScene(setting.getBoundsInLocal());
            assertTrue(editor.getMinX() >= settings.getMaxX(),
                    "Map editor is right of Settings");
            assertEquals(settings.getMinY(), editor.getMinY(), 0.5);
        }
    }

    public void verifyEditorShown(final boolean shown) {
        assertEquals(shown, isCssIdShown("mapEditor"));
    }

    public void verifyTitle(final String expected) {
        assertEquals(expected, text("editorTitle"));
    }

    public void verifyMessage(final String expected) {
        assertEquals(expected, text("editorMessage"));
    }

    public void verifyMessageContains(final String expected) {
        assertTrue(text("editorMessage").contains(expected),
                text("editorMessage"));
    }

    public void verifyDimensionDialog(final String error) {
        assertTrue(isCssIdVisible("editorWidth"), "Dimension dialog is open");
        assertEquals(error, text("editorDimensionError"));
    }

    public void verifyNoDimensionDialog() {
        assertTrue(getNodeFinder().lookup("#editorWidth").queryAll().isEmpty(),
                "Dimension dialog is closed");
    }

    public void verifyUnsavedQuestionShown() {
        assertFalse(getNodeFinder().lookup("#saveDiscard").queryAll().isEmpty(),
                "Save / Discard / Cancel is offered");
    }

    public void verifyTile(final String expected) {
        assertEquals(expected, text("editorTileLabel"));
    }

    public void verifyControl(final String id, final boolean shown) {
        assertEquals(shown,
                !getNodeFinder().lookup("#" + id).queryAll().isEmpty());
    }

    public void verifyUnitLabel(final int unitId, final String expected) {
        assertEquals(expected, text("editorUnit-" + unitId));
    }

    public void verifySavedOcean(final File file, final int width,
            final int height) {
        final ModelPo model = read(file);
        assertEquals(width, model.getMap().getMaxX());
        assertEquals(height, model.getMap().getMaxY());
        assertTrue(model.getMap().getTerrainMap().isEmpty(), "Only ocean");
        assertTrue(model.getUnits().isEmpty());
        assertTrue(model.getNativeSettlements().isEmpty());
    }

    public void verifySavedTerrain(final File file, final Location location,
            final TerrainType expected) {
        assertEquals(expected, read(file).getMap().getTerrainMap()
                .getOrDefault(location, TerrainType.OCEAN));
    }

    public void verifySavedForest(final File file, final Location location,
            final boolean expected) {
        assertEquals(expected,
                read(file).getMap().getTreeSet().contains(location));
    }

    public void verifySavedRiver(final File file, final Location location,
            final RiverSize expected) {
        assertEquals(expected, read(file).getMap().getRiverMap()
                .getOrDefault(location, RiverSize.NONE));
    }

    public void verifySavedBonus(final File file, final Location location,
            final ResourceOverlay expected) {
        assertEquals(expected, read(file).getMap().getResourceMap()
                .getOrDefault(location, ResourceOverlay.NONE));
    }

    public void verifySavedSettlement(final File file, final Location location,
            final NativeNation tribe, final boolean capital,
            final UnitType skill) {
        final NativeSettlementPo settlement = read(file).getNativeSettlements()
                .stream().filter(found -> found.getLocation().equals(location))
                .findFirst().orElseThrow();
        assertEquals(tribe, settlement.getNation());
        assertEquals(capital, settlement.getCapital());
        assertEquals(skill.name(), settlement.getTeachableSkill());
    }

    public void verifySavedSettlementCount(final File file,
            final int expected) {
        assertEquals(expected, read(file).getNativeSettlements().size());
    }

    public void verifySavedExpedition(final File file,
            final EuropeanNation nation, final Location shipAt,
            final UnitType ship, final List<UnitType> passengers,
            final Location landAt, final UnitType land) {
        final ModelPo model = read(file);
        assertTrue(model.getUnits().stream().allMatch(
                unit -> nation.getPlayerName().equals(unit.getOwnerId())));
        assertEquals(List.of(ship), types(model, shipAt));
        assertEquals(List.of(land), types(model, landAt));
        final UnitPo carrier = model.getUnits().stream()
                .filter(unit -> unit.getPlaceMap() != null
                        && shipAt.equals(unit.getPlaceMap().getLocation()))
                .findFirst().orElseThrow();
        assertEquals(passengers, carrier.getCargo().getSlots().stream()
                .map(slot -> model.getUnits().stream()
                        .filter(unit -> unit.getId().equals(slot.getUnitId()))
                        .findFirst().orElseThrow().getType())
                .collect(Collectors.toList()));
    }

    private static List<UnitType> types(final ModelPo model,
            final Location location) {
        return model.getUnits().stream()
                .filter(unit -> unit.getPlaceMap() != null
                        && location.equals(unit.getPlaceMap().getLocation()))
                .map(UnitPo::getType).collect(Collectors.toList());
    }

    public void verifyFileContent(final File file, final String expected)
            throws Exception {
        assertEquals(expected,
                Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    /**
     * The running game is exactly the authored scenario.
     */
    public void verifyStartedGame(final EuropeanNation nation, final int width,
            final int height, final Location shipAt, final UnitType ship,
            final List<UnitType> passengers, final Location landAt,
            final UnitType land, final Location settlementAt,
            final NativeNation tribe) {
        final ModelPo[] started = new ModelPo[1];
        getRobot().interact(() -> started[0] = getModel().save());
        final ModelPo model = started[0];
        assertEquals(width, model.getMap().getMaxX());
        assertEquals(height, model.getMap().getMaxY());
        assertEquals(nation.getPlayerName(),
                model.getGameManager().getCurrentPlayer());
        assertEquals(List.of(ship), types(model, shipAt));
        assertEquals(List.of(land), types(model, landAt));
        final UnitPo carrier = model.getUnits().stream()
                .filter(unit -> unit.getPlaceMap() != null
                        && shipAt.equals(unit.getPlaceMap().getLocation()))
                .findFirst().orElseThrow();
        assertEquals(passengers, carrier.getCargo().getSlots().stream()
                .filter(slot -> slot.getUnitId() != null)
                .map(slot -> model.getUnits().stream()
                        .filter(unit -> unit.getId().equals(slot.getUnitId()))
                        .findFirst().orElseThrow().getType())
                .collect(Collectors.toList()));
        final Optional<NativeSettlementPo> settlement = model
                .getNativeSettlements().stream()
                .filter(found -> found.getLocation().equals(settlementAt))
                .findFirst();
        assertEquals(tribe, settlement.orElseThrow().getNation());
        assertEquals(1, model.getNativeSettlements().size());
        assertEquals(1 + 2 + 1, model.getUnits().size(),
                "No generated units are added");
    }

    /** Resize the main window, for example to a compact screen. */
    public void resizeWindow(final double width, final double height) {
        getRobot().interact(() -> {
            getPrimaryStage().setWidth(width);
            getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * The top menu, the right panel and a usable map view fit the window, and
     * every shown text of the editor is whole: no label, command, tool, check
     * box or list value is cut or shortened with an ellipsis. Panel content
     * below the visible part is reached by vertical scrolling.
     */
    public void verifyLayoutFitsWindow(final double minimumMapWidth) {
        // Check scrollable controls after revealing each, just as a player
        // reaches them; off-screen rows are not a layout failure.
        for (final String id : List.of("editorNew", "editorOpen", "editorSave",
                "editorPlay", "editorBack", "editorTitle", "editorNation",
                "editorToolUnit")) {
            final Node node = getNodeFinder().lookup("#" + id).query();
            revealGameContent(node);
            getRobot().interact(() -> {
                final javafx.scene.Scene scene = getPrimaryStage().getScene();
                final Bounds bounds = node
                        .localToScene(node.getBoundsInLocal());
                assertTrue(node.isVisible(), id + " is shown");
                assertTrue(
                        bounds.getMinX() >= 0
                                && bounds.getMaxX() <= scene.getWidth() + 0.5
                                && bounds.getMinY() >= 0
                                && bounds.getMaxY() <= scene.getHeight() + 0.5,
                        id + " fits after scrolling: " + bounds);
            });
        }
        getRobot().interact(() -> {
            final javafx.scene.Scene scene = getPrimaryStage().getScene();
            final Node editor = getNodeFinder().lookup("#mapEditor").query();
            final List<Labeled> texts = getNodeFinder().from(editor)
                    .lookup((Node node) -> node instanceof Labeled
                            && node.isVisible())
                    .queryAllAs(Labeled.class).stream()
                    .filter(labeled -> labeled.getText() != null
                            && !labeled.getText().isBlank())
                    .collect(Collectors.toList());
            assertTrue(texts.size() > 10, "Editor texts found: " + texts);
            for (final Labeled labeled : texts) {
                final Node shown = labeled.lookup(".text");
                final String displayed = shown instanceof javafx.scene.text.Text
                        ? ((javafx.scene.text.Text) shown).getText()
                        : null;
                assertEquals(labeled.getText(), displayed,
                        "Whole text of " + labeled);
                final Bounds bounds = labeled
                        .localToScene(labeled.getBoundsInLocal());
                assertTrue(
                        bounds.getMinX() >= -0.5
                                && bounds.getMaxX() <= scene.getWidth() + 0.5,
                        labeled.getText() + " fits horizontally: " + bounds);
            }
            final Canvas canvas = getNodeFinder().lookup("#mapEditor")
                    .lookup((Node node) -> node instanceof Canvas)
                    .queryAs(Canvas.class);
            final Bounds map = canvas.localToScene(canvas.getBoundsInLocal());
            final Node panel = getNodeFinder().lookup("#editorNation").query();
            assertTrue(
                    Math.min(map.getMaxX(), scene.getWidth())
                            - Math.max(map.getMinX(), 0) >= minimumMapWidth,
                    "Usable map view: " + map);
            assertTrue(
                    panel.localToScene(panel.getBoundsInLocal())
                            .getMinX() >= Math.min(map.getMaxX(),
                                    scene.getWidth()) - 0.5,
                    "The right panel stands beside the map");
        });
    }

    /**
     * After whole turns the game still has the authored expedition and
     * settlement, and it is the player's turn again.
     */
    public void verifyGameAfterTurns(final EuropeanNation nation,
            final int minimumTurns, final Location shipAt,
            final List<UnitType> passengers, final int expeditionSize,
            final Location settlementAt, final NativeNation tribe) {
        final ModelPo[] started = new ModelPo[1];
        final int[] turns = new int[1];
        getRobot().interact(() -> {
            started[0] = getModel().save();
            turns[0] = getModel().getCalendar().getNumberOfPlayedTurns();
        });
        final ModelPo model = started[0];
        assertTrue(turns[0] >= minimumTurns, "Played turns: " + turns[0]);
        assertEquals(nation.getPlayerName(),
                model.getGameManager().getCurrentPlayer());
        assertEquals(expeditionSize, model.getUnits().stream().filter(
                unit -> nation.getPlayerName().equals(unit.getOwnerId()))
                .count());
        final UnitPo carrier = model.getUnits().stream()
                .filter(unit -> unit.getPlaceMap() != null
                        && shipAt.equals(unit.getPlaceMap().getLocation()))
                .findFirst().orElseThrow();
        assertEquals(passengers, carrier.getCargo().getSlots().stream()
                .filter(slot -> slot.getUnitId() != null)
                .map(slot -> model.getUnits().stream()
                        .filter(unit -> unit.getId().equals(slot.getUnitId()))
                        .findFirst().orElseThrow().getType())
                .collect(Collectors.toList()));
        assertEquals(tribe, model.getNativeSettlements().stream()
                .filter(found -> found.getLocation().equals(settlementAt))
                .findFirst().orElseThrow().getNation());
    }

}
