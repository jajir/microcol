package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.TradeRoute;
import org.microcol.model.TradeRouteAssignment;
import org.microcol.model.TradeRouteStop;
import org.microcol.model.Unit;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * Cargo dialog of trade route stops, opened from the colony screen. Choices are
 * made through the visible combo boxes, lists and buttons as a player would;
 * the model is only read to verify the outcome.
 */
public final class TradeRouteCargoDialog extends AbstractScreen {

    private static final String UNLOAD = "tradeRouteUnload";
    private static final String LOAD = "tradeRouteLoad";

    public TradeRouteCargoDialog(final TestContext context) {
        super(context);
    }

    public void open() {
        press("tradeRouteCargoOpen");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseRoute(final String name) {
        final ComboBox<TradeRoute> combo = routeBox();
        int index = -1;
        for (int i = 0; i < combo.getItems().size(); i++) {
            if (combo.getItems().get(i).getName().equals(name)) {
                index = i;
            }
        }
        if (index < 0) {
            throw new IllegalStateException("Route is not offered: " + name);
        }
        choose(combo, index);
    }

    /** @param index zero-based stop index of the chosen route */
    public void chooseStop(final int index) {
        choose(stopBox(), index);
    }

    public void addUnload(final GoodsType type) {
        addGoods(UNLOAD, type);
    }

    public void addLoad(final GoodsType type) {
        addGoods(LOAD, type);
    }

    public void selectUnload(final int index) {
        select(UNLOAD, index);
    }

    public void selectLoad(final int index) {
        select(LOAD, index);
    }

    public void moveUnloadUp() {
        press(UNLOAD + "Up");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveUnloadDown() {
        press(UNLOAD + "Down");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveLoadUp() {
        press(LOAD + "Up");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveLoadDown() {
        press(LOAD + "Down");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void removeLoad() {
        press(LOAD + "Remove");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void save() {
        press("tradeRouteCargoSave");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void process() {
        press("tradeRouteCargoProcess");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void close() {
        press("tradeRouteCargoClose");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** @param index zero-based position in the list of carriers at the stop */
    public void chooseCarrier(final int index) {
        choose(getNodeFinder().lookup("#tradeRouteCargoCarrier")
                .queryComboBox(), index);
    }

    /** @param index zero-based stop index of the chosen route */
    public void chooseFirstStop(final int index) {
        choose(firstStopBox(), index);
    }

    public void assign() {
        press("tradeRouteAssign");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Moves the focus to the assign button and presses it with the Space key.
     */
    public void assignWithKeyboard() {
        pressWithKeyboard("tradeRouteAssign");
    }

    /**
     * Picks the carrier to release among every carrier that follows the chosen
     * route, wherever it is, not only among those at the stop.
     *
     * @param index zero-based position in the list of assigned carriers
     */
    public void chooseAssignedCarrier(final int index) {
        choose(getNodeFinder().lookup("#tradeRouteAssignedCarrier")
                .queryComboBox(), index);
    }

    /** @param name the colony name, or the text of Europe, as listed */
    public void chooseReplacement(final String name) {
        final ComboBox<TradeRouteStop> combo = replacementBox();
        int index = -1;
        for (int i = 0; i < combo.getItems().size(); i++) {
            if (combo.getConverter().toString(combo.getItems().get(i))
                    .equals(name)) {
                index = i;
            }
        }
        if (index < 0) {
            throw new IllegalStateException("Stop is not offered: " + name);
        }
        choose(combo, index);
    }

    public void replaceStop() {
        press("tradeRouteReplaceButton");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void replaceStopWithKeyboard() {
        pressWithKeyboard("tradeRouteReplaceButton");
    }

    public void cancelAssignment() {
        press("tradeRouteCancelAssignment");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelAssignmentWithKeyboard() {
        pressWithKeyboard("tradeRouteCancelAssignment");
    }

    /** First press: names the affected carriers, deletes nothing yet. */
    public void askToDeleteRoute() {
        press("tradeRouteDelete");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Second press on the same route: now the route is deleted. */
    public void confirmDeleteRoute() {
        press("tradeRouteDelete");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void askToDeleteRouteWithKeyboard() {
        pressWithKeyboard("tradeRouteDelete");
    }

    public void confirmDeleteRouteWithKeyboard() {
        pressWithKeyboard("tradeRouteDelete");
    }

    /**
     * Closes the dialog with the Escape key, as a player abandoning it. The
     * focus is first moved off the last used choice box to the Close button,
     * because an open or focused choice box may take Escape for itself.
     */
    public void closeWithEscape() {
        final javafx.scene.control.Button button = getButtonById(
                "tradeRouteCargoClose");
        reveal(button);
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void pressWithKeyboard(final String buttonId) {
        final javafx.scene.control.Button button = getButtonById(buttonId);
        reveal(button);
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void addGoods(final String prefix, final GoodsType type) {
        final ComboBox<GoodsType> combo = getNodeFinder()
                .lookup("#" + prefix + "Choice").queryComboBox();
        final int index = combo.getItems().indexOf(type);
        if (index < 0) {
            throw new IllegalStateException("Goods is not offered: " + type);
        }
        choose(combo, index);
        press(prefix + "Add");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Focuses the list, goes to its top entry and steps down to the index. */
    private void select(final String prefix, final int index) {
        reveal(list(prefix));
        getRobot().clickOn(list(prefix)).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Presses a button of the dialog after scrolling it into view, so a dialog
     * taller than a small screen is operated the same way a player would.
     */
    private void press(final String buttonId) {
        reveal(getButtonById(buttonId));
        clickOnButtonWithId(buttonId);
    }

    /** Scrolls the dialog, when it has a scroll bar, until the node shows. */
    private void reveal(final Node node) {
        getNodeFinder().lookup("#tradeRouteCargoScroll").tryQuery()
                .ifPresent(found -> {
                    final ScrollPane scroll = (ScrollPane) found;
                    getRobot().interact(() -> {
                        final Node content = scroll.getContent();
                        final double whole = content.getLayoutBounds()
                                .getHeight();
                        final double view = scroll.getViewportBounds()
                                .getHeight();
                        if (whole > view) {
                            final Bounds inside = content.sceneToLocal(
                                    node.localToScene(node.getBoundsInLocal()));
                            final double top = inside.getMinY()
                                    - (view - inside.getHeight()) / 2;
                            scroll.setVvalue(Math.max(0,
                                    Math.min(1, top / (whole - view))));
                        }
                    });
                });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private <T> void choose(final ComboBox<T> combo, final int index) {
        reveal(combo);
        getRobot().clickOn(combo).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private ComboBox<TradeRoute> routeBox() {
        return getNodeFinder().lookup("#tradeRouteCargoRoute").queryComboBox();
    }

    private ComboBox<Integer> stopBox() {
        return getNodeFinder().lookup("#tradeRouteCargoStop").queryComboBox();
    }

    private ComboBox<TradeRouteStop> replacementBox() {
        return getNodeFinder().lookup("#tradeRouteReplaceStop").queryComboBox();
    }

    private ComboBox<Integer> firstStopBox() {
        return getNodeFinder().lookup("#tradeRouteAssignFirstStop")
                .queryComboBox();
    }

    @SuppressWarnings("unchecked")
    private ListView<GoodsType> list(final String prefix) {
        return getNodeFinder().lookup("#" + prefix + "List")
                .queryAs(ListView.class);
    }

    private ComboBox<GoodsType> choice(final String prefix) {
        return getNodeFinder().lookup("#" + prefix + "Choice").queryComboBox();
    }

    private Unit carrier(final int unitId) {
        // A ship on the Atlantic is not on the map, so every unit is searched.
        return getModel().getColonyById(0).orElseThrow().getOwner()
                .getAllUnits().stream().filter(unit -> unit.getId() == unitId)
                .findFirst().orElseThrow();
    }

    public void verifyOpened() {
        assertTrue(getNodeFinder().lookup("#tradeRouteCargoDialog").tryQuery()
                .isPresent());
    }

    public void verifyDialogClosed() {
        assertTrue(getNodeFinder().lookup("#tradeRouteCargoDialog").tryQuery()
                .isEmpty());
    }

    public void verifyTitle(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteCargoTitle").getText());
    }

    public void verifyUnloadOrder(final GoodsType... expected) {
        assertEquals(List.of(expected), list(UNLOAD).getItems());
    }

    public void verifyLoadOrder(final GoodsType... expected) {
        assertEquals(List.of(expected), list(LOAD).getItems());
    }

    public void verifyUnloadAddEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById(UNLOAD + "Add").isDisabled());
    }

    public void verifyLoadAddEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById(LOAD + "Add").isDisabled());
    }

    /** A goods already in the list is no longer offered, so no duplicate. */
    public void verifyUnloadChoiceOffers(final GoodsType type,
            final boolean expected) {
        assertEquals(expected, choice(UNLOAD).getItems().contains(type));
    }

    public void verifySaveEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("tradeRouteCargoSave").isDisabled());
    }

    public void verifyProcessEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("tradeRouteCargoProcess").isDisabled());
    }

    public void verifyCarrierStatus(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteCargoCarrierStatus").getText());
    }

    public void verifyMessage(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteCargoMessage").getText());
    }

    /** @param expected the result lines, one per configured goods entry */
    public void verifyResult(final String... expected) {
        assertEquals(String.join("\n", expected),
                getLabeledById("tradeRouteCargoResult").getText());
    }

    public void verifyStoredCargo(final String routeName, final int stopIndex,
            final List<GoodsType> expectedUnload,
            final List<GoodsType> expectedLoad) {
        final List<TradeRoute> matching = getModel()
                .getTradeRoutes(
                        getModel().getColonyById(0).orElseThrow().getOwner())
                .stream().filter(route -> route.getName().equals(routeName))
                .collect(Collectors.toList());
        assertEquals(1, matching.size());
        assertEquals(expectedUnload,
                matching.get(0).getStops().get(stopIndex).getUnloadGoods());
        assertEquals(expectedLoad,
                matching.get(0).getStops().get(stopIndex).getLoadGoods());
    }

    public void verifyHold(final int unitId, final int slotIndex,
            final Goods expected) {
        assertEquals(expected,
                ((org.microcol.model.unit.UnitWithCargo) carrier(unitId))
                        .getCargo().getSlotByIndex(slotIndex).getGoods()
                        .orElse(null));
    }

    public void verifyHoldEmpty(final int unitId, final int slotIndex) {
        assertTrue(((org.microcol.model.unit.UnitWithCargo) carrier(unitId))
                .getCargo().getSlotByIndex(slotIndex).getGoods().isEmpty());
    }

    public void verifyWarehouse(final int colonyId, final Goods expected) {
        assertEquals(expected.getAmount(),
                getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                        .getGoods(expected.getType()).getAmount());
    }

    public void verifyVisitProcessed(final int unitId, final boolean expected) {
        assertEquals(expected,
                carrier(unitId).getProcessedTradeRouteVisit().isPresent());
    }

    public void verifyAssignEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById("tradeRouteAssign").isDisabled());
    }

    /** @param expected the model's refusal text, or empty when accepted */
    public void verifyAssignStatus(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteAssignStatus").getText());
    }

    /** @param expected the list of assigned carriers, one line per carrier */
    public void verifyAssigned(final String... expected) {
        assertEquals(String.join("\n", expected),
                getLabeledById("tradeRouteAssigned").getText());
    }

    public void verifyAssignmentHeading(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteAssignedHeading").getText());
    }

    public void verifyAssignButtonText(final String expected) {
        assertEquals(expected, getButtonById("tradeRouteAssign").getText());
    }

    /**
     * Reads the saved assignment of a carrier: its route, the stop it heads to
     * and whether it waits.
     */
    public void verifyAssignment(final int unitId, final String routeName,
            final int stopIndex, final boolean paused) {
        final TradeRouteAssignment assignment = getModel()
                .getTradeRouteAssignment(carrier(unitId)).orElseThrow();
        final TradeRoute route = getModel()
                .getTradeRoutes(
                        getModel().getColonyById(0).orElseThrow().getOwner())
                .stream().filter(stored -> stored.getName().equals(routeName))
                .findFirst().orElseThrow();
        assertEquals(route.getId(), assignment.getRouteId());
        assertEquals(stopIndex, assignment.getStopIndex());
        assertEquals(paused, assignment.getPause().isPresent());
    }

    public void verifyNotAssigned(final int unitId) {
        assertTrue(
                getModel().getTradeRouteAssignment(carrier(unitId)).isEmpty());
    }

    public void verifyReplaceEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("tradeRouteReplaceButton").isDisabled());
    }

    /** @param expected the model's refusal, or the carriers it will reach */
    public void verifyReplaceStatus(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteReplaceStatus").getText());
    }

    public void verifyCancelAssignmentEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("tradeRouteCancelAssignment").isDisabled());
    }

    public void verifyDeleteEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById("tradeRouteDelete").isDisabled());
    }

    /** The button asks for confirmation after its first press. */
    public void verifyDeleteButtonText(final String expected) {
        assertEquals(expected, getButtonById("tradeRouteDelete").getText());
    }

    /** @param expected the carriers the delete would release or released */
    public void verifyDeleteInfo(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteDeleteInfo").getText());
    }

    public void verifyCancelAssignmentButtonText(final String expected) {
        assertEquals(expected,
                getButtonById("tradeRouteCancelAssignment").getText());
    }

    public void verifyReplaceButtonText(final String expected) {
        assertEquals(expected,
                getButtonById("tradeRouteReplaceButton").getText());
    }

    /**
     * @param expected the carriers offered for release, as the combo box shows
     *                 them, wherever those carriers are
     */
    public void verifyAssignedChoices(final String... expected) {
        final ComboBox<org.microcol.model.unit.UnitWithCargo> combo = getNodeFinder()
                .lookup("#tradeRouteAssignedCarrier").queryComboBox();
        assertEquals(List.of(expected),
                combo.getItems().stream()
                        .map(item -> combo.getConverter().toString(item))
                        .collect(Collectors.toList()));
    }

    /** @param expected the name as shown, empty when nothing is picked */
    public void verifyAssignedChoice(final String expected) {
        final ComboBox<org.microcol.model.unit.UnitWithCargo> combo = getNodeFinder()
                .lookup("#tradeRouteAssignedCarrier").queryComboBox();
        assertEquals(expected, combo.getConverter().toString(combo.getValue()));
    }

    public void verifyRouteOffered(final String name, final boolean expected) {
        assertEquals(expected, routeBox().getItems().stream()
                .anyMatch(route -> route.getName().equals(name)));
    }

    public void verifyRouteStored(final String name, final boolean expected) {
        assertEquals(expected, getModel()
                .getTradeRoutes(
                        getModel().getColonyById(0).orElseThrow().getOwner())
                .stream().anyMatch(route -> route.getName().equals(name)));
    }

    /**
     * @param expected the colony name, {@code Europe}, or {@code lost <id>} for
     *                 a stop whose colony no longer exists
     */
    public void verifyStoredStop(final String routeName, final int stopIndex,
            final String expected) {
        final TradeRouteStop stop = getModel()
                .getTradeRoutes(
                        getModel().getColonyById(0).orElseThrow().getOwner())
                .stream().filter(route -> route.getName().equals(routeName))
                .findFirst().orElseThrow().getStops().get(stopIndex);
        assertEquals(expected,
                stop.isEurope() ? "Europe"
                        : getModel().getColonyById(stop.getColonyId())
                                .map(colony -> colony.getName())
                                .orElse("lost " + stop.getColonyId()));
    }

    /** The stop the carrier accepted at its last boundary, not the route's. */
    public void verifyAcceptedStop(final int unitId, final int stopIndex,
            final String expected) {
        final TradeRouteStop stop = getModel()
                .getTradeRouteAssignment(carrier(unitId)).orElseThrow()
                .getStops().get(stopIndex);
        assertEquals(expected,
                stop.isEurope() ? "Europe"
                        : getModel().getColonyById(stop.getColonyId())
                                .map(colony -> colony.getName())
                                .orElse("lost " + stop.getColonyId()));
    }

    public void verifyPauseReason(final int unitId,
            final org.microcol.model.TradeRoutePause expected) {
        assertEquals(java.util.Optional.of(expected),
                getModel().getTradeRouteAssignment(carrier(unitId))
                        .orElseThrow().getPause());
    }

    public void verifyNotPaused(final int unitId) {
        assertTrue(getModel().getTradeRouteAssignment(carrier(unitId))
                .orElseThrow().getPause().isEmpty());
    }

    /** The ship is on the Atlantic with this many turns of voyage left. */
    public void verifyAtSea(final int unitId, final int remainingTurns) {
        final Unit ship = carrier(unitId);
        assertTrue(ship.isAtHighSea());
        assertEquals(remainingTurns,
                ((org.microcol.model.PlaceHighSea) ship.getPlace())
                        .getRemainigTurns());
    }

    public void verifyNotAtSea(final int unitId) {
        assertFalse(carrier(unitId).isAtHighSea());
    }

    public void verifyGold(final int expected) {
        assertEquals(expected,
                getModel().getColonyById(0).orElseThrow().getOwner().getGold());
    }

    /** The unit obeys its owner again: no route or go-to order holds it. */
    public void verifyNoStandingOrder(final int unitId) {
        assertTrue(
                getModel().getTradeRouteAssignment(carrier(unitId)).isEmpty());
        assertTrue(getModel().getGoToDestination(carrier(unitId)).isEmpty());
    }

    /**
     * @param expected movement left this turn, in thirds of a point; a release
     *                 neither refunds nor takes away movement
     */
    public void verifyMovementThirds(final int unitId, final int expected) {
        assertEquals(expected, carrier(unitId).getMovementThirds());
    }

    /** The whole dialog is inside the screen's usable area. */
    public void verifyDialogFitsScreen() {
        final Window window = getNodeFinder().lookup("#tradeRouteCargoDialog")
                .query().getScene().getWindow();
        final javafx.geometry.Rectangle2D screen = Screen.getPrimary()
                .getVisualBounds();
        assertTrue(window.getHeight() <= screen.getHeight(),
                "Dialog is taller than the screen: " + window.getHeight());
        assertTrue(window.getWidth() <= screen.getWidth(),
                "Dialog is wider than the screen: " + window.getWidth());
        assertTrue(window.getY() >= screen.getMinY() - 1,
                "Dialog starts above the screen: " + window.getY());
        assertTrue(window.getY() + window.getHeight() <= screen.getMaxY() + 1,
                "Dialog ends below the screen: " + window.getY());
    }

    public void verifyUnitAt(final int unitId, final Location expected) {
        assertEquals(expected, carrier(unitId).getLocation());
    }

    private Location rememberedLocation;

    /** Reads and keeps the unit's square, so a later check can compare it. */
    public void rememberUnitLocation(final int unitId) {
        rememberedLocation = carrier(unitId).getLocation();
    }

    public void verifyUnitAtRememberedLocation(final int unitId) {
        assertNotNull(rememberedLocation, "No location was remembered");
        assertEquals(rememberedLocation, carrier(unitId).getLocation());
    }

    /**
     * Several squares between two stops can cost the same, so this checks that
     * the unit took exactly one step: it stands next to both squares.
     */
    public void verifyUnitBetween(final int unitId, final Location from,
            final Location to) {
        final Location at = carrier(unitId).getLocation();
        assertNotEquals(from, at);
        assertNotEquals(to, at);
        assertTrue(at.isNeighbor(from), "Not next to " + from + ": " + at);
        assertTrue(at.isNeighbor(to), "Not next to " + to + ": " + at);
    }
}
