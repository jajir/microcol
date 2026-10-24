package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.gamepanel.OneTurnMoveHighlighter;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.ConcurrentModelEvents;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

/**
 * Read-only inspection through the right panel (R-0205). The page reads the
 * panel and the model. It presses only the keyboard selection key, and removes
 * a unit only through the concurrent model event used for open dialogs, so no
 * check here moves a unit or changes goods.
 */
public final class InspectionPage extends AbstractScreen {

    public InspectionPage(final TestContext context) {
        super(context);
    }

    /** Tab selects the next own unit, the keyboard route into the panel. */
    public void pressTab() {
        getRobot().push(KeyCode.TAB);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private boolean isSelected(final int unitId) {
        return selectedUnit().map(unit -> unit.getId() == unitId).orElse(false);
    }

    private Optional<Unit> selectedUnit() {
        return getContext().getClassFromGuice(SelectedUnitManager.class)
                .getSelectedUnit();
    }

    /**
     * A neighbouring square the model lets the unit simply move to this turn:
     * reachable, explored, empty, and neither an attack, embark or disembark
     * target, so the preview of it is the plain move preview. Reading it
     * changes nothing.
     *
     * @param unitId id of the own unit
     * @return the first such neighbouring square
     */
    public Location plainMoveStepOf(final int unitId) {
        final Unit unit = getModel().getUnitById(unitId);
        final List<Location> reach = unit.getAvailableLocations();
        return unit.getLocation().getNeighbors().stream()
                .filter(reach::contains)
                .filter(location -> unit.getOwner().isVisible(location))
                .filter(location -> getModel().getUnitsAt(location).isEmpty())
                .filter(location -> unit.prepareAttack(location, true)
                        .isEmpty())
                .filter(location -> !unit.isPossibleToEmbarkAt(location))
                .filter(location -> !(unit instanceof UnitWithCargo)
                        || !((UnitWithCargo) unit)
                                .isPossibleToDisembarkAt(location, true))
                .findFirst().orElseThrow();
    }

    /** Squares within a Chebyshev radius that exist on the map. */
    private List<Location> squaresAround(final Location center,
            final int radius) {
        final List<Location> squares = new ArrayList<>();
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY()
                    + radius; y++) {
                final Location square = Location.of(x, y);
                if (getModel().getMap().isValid(square)) {
                    squares.add(square);
                }
            }
        }
        return squares;
    }

    /**
     * Square, order, movement and occupied holds of every unit, together with
     * the year and the gold of every colonial player. Reading it changes
     * nothing.
     *
     * @return observable facts keyed by name
     */
    public Map<String, String> worldFacts() {
        final Map<String, String> facts = new TreeMap<>();
        facts.put("year",
                String.valueOf(getModel().getCalendar().getCurrentYear()));
        getModel().getPlayers()
                .forEach(player -> facts.put("gold " + player.getName(),
                        String.valueOf(player.getGold())));
        for (final Unit unit : getModel().getAllUnits()) {
            facts.put("unit " + unit.getId(), describe(unit));
        }
        return facts;
    }

    private static String describe(final Unit unit) {
        final long occupiedHolds = unit instanceof UnitWithCargo
                ? ((UnitWithCargo) unit).getCargo().getSlots().stream()
                        .filter(slot -> !slot.isEmpty()).count()
                : 0;
        final String square = unit.isAtPlaceLocation()
                ? unit.getLocation().toString()
                : "off the map";
        return square + " moves " + unit.getMovementThirds() + " order "
                + unit.getAction().getType() + " holds " + occupiedHolds;
    }

    private List<String> panelTexts(final int unitId) {
        final Node panel = getNodeFinder().lookup("#unitPanel-" + unitId)
                .query();
        return getNodeFinder().from(panel).lookup(".label")
                .queryAllAs(Label.class).stream().map(Label::getText)
                .collect(Collectors.toList());
    }

    /**
     * Removes a unit from the live model while the panel may still show it.
     * This is the concurrent model event the architecture allows in tests.
     *
     * @param unitId id of the unit to remove
     */
    public void removeUnitWhileShown(final int unitId) {
        getRobot().interact(() -> ConcurrentModelEvents.destroyUnit(getModel(),
                getModel().getUnitById(unitId)));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void captureRightPanel(final String fileName) throws IOException {
        captureDialog("rightPanel", fileName);
    }

    /**
     * The right panel of the window, measured in scene coordinates, lies
     * completely inside the window.
     */
    public void verifyRightPanelFitsWindow() {
        final Node panel = getNodeFinder().lookup("#rightPanel").query();
        final Bounds bounds = panel.localToScene(panel.getBoundsInLocal());
        final javafx.scene.Scene scene = getPrimaryStage().getScene();
        assertTrue(
                bounds.getMinX() >= 0 && bounds.getMinY() >= 0
                        && bounds.getMaxX() <= scene.getWidth()
                        && bounds.getMaxY() <= scene.getHeight(),
                "Right panel does not fit the window: " + bounds);
    }

    /**
     * @param unitId          id of the unit whose panel is looked for
     * @param expectedPresent whether its panel is shown
     */
    public void verifyUnitPanelPresent(final int unitId,
            final boolean expectedPresent) {
        assertEquals(
                expectedPresent, getNodeFinder().lookup("#unitPanel-" + unitId)
                        .tryQuery().isPresent(),
                "Unexpected panel of unit " + unitId);
    }

    /**
     * The foreign ship shows the number of its occupied holds, and only that.
     *
     * @param unitId           id of the visible foreign ship
     * @param expectedLineText the whole line, such as Occupied holds: 5
     */
    public void verifyOccupiedHoldsLine(final int unitId,
            final String expectedLineText) {
        assertEquals(expectedLineText,
                getLabeledById("unitHolds-" + unitId).getText(),
                "Unexpected occupied holds line of unit " + unitId);
    }

    /**
     * The panel of the foreign ship names none of its passengers or goods.
     *
     * @param unitId      id of the visible foreign ship
     * @param hiddenTexts words that must not appear in any label of its panel
     */
    public void verifyHiddenContentsNotShown(final int unitId,
            final String... hiddenTexts) {
        final List<String> texts = panelTexts(unitId);
        for (final String hidden : hiddenTexts) {
            assertTrue(texts.stream().noneMatch(text -> text.contains(hidden)),
                    "Hidden contents shown in the panel of unit " + unitId
                            + ": " + hidden + " in " + texts);
        }
    }

    /**
     * The move-mode overlay highlights exactly the squares the model says the
     * unit can reach this turn. Every reachable square is highlighted, and no
     * other square near the unit is: not the unit's own square, not terrain it
     * cannot enter, and not squares beyond its movement.
     *
     * @param unitId id of the own unit in move mode
     * @param radius squares around the unit that must be at least as far as its
     *               movement can carry it
     */
    public void verifyRangeOverlayMatchesModel(final int unitId,
            final int radius) {
        final Unit unit = getModel().getUnitById(unitId);
        final OneTurnMoveHighlighter overlay = getContext()
                .getClassFromGuice(OneTurnMoveHighlighter.class);
        final List<Location> reach = unit.getAvailableLocations();
        assertFalse(reach.isEmpty(), "The unit has no reachable square");
        for (final Location square : reach) {
            assertTrue(
                    square.getDistanceChebyshev(unit.getLocation()) <= radius,
                    "Reach is wider than the checked radius: " + square);
            assertTrue(overlay.isItHighlighted(square),
                    "Reachable square is not highlighted: " + square);
        }
        for (final Location square : squaresAround(unit.getLocation(),
                radius)) {
            if (!reach.contains(square)) {
                assertFalse(overlay.isItHighlighted(square),
                        "Unreachable square is highlighted: " + square);
            }
        }
    }

    /**
     * No square near the unit is highlighted any more, so a cancelled preview
     * leaves no range on the map.
     *
     * @param unitId id of the own unit that was in move mode
     * @param radius squares around the unit to check
     */
    public void verifyRangeOverlayCleared(final int unitId, final int radius) {
        final OneTurnMoveHighlighter overlay = getContext()
                .getClassFromGuice(OneTurnMoveHighlighter.class);
        final Location center = getModel().getUnitById(unitId).getLocation();
        for (final Location square : squaresAround(center, radius)) {
            assertFalse(overlay.isItHighlighted(square),
                    "Square is still highlighted: " + square);
        }
    }

    /**
     * Every unit keeps its square, order, movement and occupied holds, and the
     * year and gold are unchanged, so inspection spent nothing and moved
     * nothing.
     *
     * @param expectedFacts facts captured before the inspection
     */
    public void verifyWorldFactsUnchanged(
            final Map<String, String> expectedFacts) {
        assertEquals(expectedFacts, worldFacts(),
                "Inspection changed a unit, its order, its holds, the date or the gold");
    }

    /**
     * @param unitId id of the own unit that keyboard selection should reach
     */
    public void verifySelectedUnitId(final int unitId) {
        assertTrue(isSelected(unitId), "Expected selected unit " + unitId
                + " but was " + selectedUnit().map(Unit::getId).orElse(null));
    }

    /**
     * @param removedUnitId id of the unit removed from the live model
     */
    public void verifyStaleSelectionNotDisclosed(final int removedUnitId) {
        final Optional<Unit> selected = selectedUnit();
        assertTrue(
                selected.isEmpty() || getModel().getAllUnits().stream()
                        .noneMatch(unit -> unit == selected.get()),
                "A removed unit is still selected and live");
        assertTrue(getModel().tryGetUnitById(removedUnitId).isEmpty(),
                "Removed unit is still in the model");
    }

    /**
     * @param expectedOwnerName owner name of the unit that keyboard selection
     *                          reached
     */
    public void verifySelectedUnitOwner(final String expectedOwnerName) {
        final Unit selected = selectedUnit().orElseThrow();
        assertEquals(expectedOwnerName, selected.getOwner().getName(),
                "Unexpected owner of the keyboard selected unit");
    }
}
