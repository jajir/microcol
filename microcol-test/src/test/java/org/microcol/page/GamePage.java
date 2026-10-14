package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.microcol.gui.Tile.TILE_CENTER;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.microcol.gui.Point;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.gui.screen.game.components.StatusBarView;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

/**
 * Represents main game page with world map.
 */
public class GamePage extends AbstractScreen {

    private final Logger logger = LoggerFactory.getLogger(GamePage.class);

    public static GamePage of(final TestContext context) {
        return new GamePage(context);
    }

    private GamePage(final TestContext context) {
        super(context);
        WaitForAsyncUtils.waitForFxEvents();
        verifyThatStatusBarIsVisible();
    }

    public void moveMouseAtLocation(final Location location) {
        verifyThatTileIsVisible(location);
        final Point p = getContext().getArea().convertToCanvasPoint(location)
                .add(getPrimaryStage().getX(), getPrimaryStage().getY())
                .add(TILE_CENTER);
        getRobot().moveTo(p.getX(), p.getY(), Motion.DEFAULT);
        verifyThatStatusBarContains(String.valueOf(location.getX()));
        verifyThatStatusBarContains(String.valueOf(location.getY()));
    }

    public void dragMouseAtLocation(final Location location) throws Exception {
        verifyThatTileIsVisible(location);
        final Point p = getContext().getArea().convertToCanvasPoint(location)
                .add(getPrimaryStage().getX(), getPrimaryStage().getY())
                .add(TILE_CENTER);
        getRobot().drag(MouseButton.PRIMARY).drag(p.getX(), p.getY())
                .release(MouseButton.PRIMARY);
        waitWhileMoving();
        verifyThatStatusBarContains(String.valueOf(location.getX()));
        verifyThatStatusBarContains(String.valueOf(location.getY()));
    }

    public void dragMouseAtLocationAndClosePossibleDialog(
            final Location location) throws Exception {
        dragMouseAtLocation(location);
        if (isCssIdVisible("buttonOk")) {
            clickOnButtonWithId("buttonOk");
        }
    }

    public void verifyThatStatusBarContains(final String string) {
        final Labeled label = getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID);
        logger.info("Status bar: " + label.getText());
        assertTrue(label.getText().contains(string), String.format(
                "Text '%s' should appear in status bar. But status bar contains text '%s'.",
                string, label.getText()));
    }

    private void verifyThatStatusBarIsVisible() {
        assertTrue(isCssIdVisible(StatusBarView.STATUS_BAR_LABEL_ID),
                String.format("Status bar is not visible."));
        assertTrue(isCssIdVisible(ButtonsGamePanel.BUTTON_NEXT_TURN_ID),
                String.format("Next turn button is not visible."));
    }

    public void verifyThatTileIsVisible(final Location location) {
        assertTrue(getContext().getArea().isLocationVisible(location));
    }

    public void selectTile(final Location location) {
        Preconditions.checkNotNull(location);
        moveMouseAtLocation(location);
        getRobot().clickOn(MouseButton.PRIMARY);
    }

    public void saveGame() {
        getRobot().push(KeyCode.CONTROL, KeyCode.S);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public ColonyNameDialog foundColony() {
        getRobot().push(KeyCode.B);
        WaitForAsyncUtils.waitForFxEvents();
        return new ColonyNameDialog(getContext());
    }

    public WelcomePage exitToMenu() {
        clickOnButtonWithId("exit");
        return WelcomePage.of(getContext());
    }

    public Optional<ScreenTurnReport> nextTurn() throws Exception {
        final Button buttonNextTurn = getButtonById(
                ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
        getRobot().clickOn(buttonNextTurn);
        waitWhileMoving();
        WaitForAsyncUtils.waitForFxEvents();
        if (isButtonBackVisible()) {
            return Optional.of(ScreenTurnReport.of(getContext()));
        } else {
            return Optional.empty();
        }
    }

    public void nextTurnAndCloseDialogs() throws Exception {
        final Optional<ScreenTurnReport> oTurnEvents = nextTurn();
        if (oTurnEvents.isPresent()) {
            final ScreenTurnReport dialog = oTurnEvents.get();
            dialog.close();
        }
    }

    public DialogMessagePage declareIndependence() throws Exception {
        final Button buttonNextTurn = getButtonById(
                ButtonsGamePanel.BUTTON_DECLARE_INDEPENDENCE_ID);
        getRobot().clickOn(buttonNextTurn);
        WaitForAsyncUtils.waitForFxEvents();
        return DialogMessagePage.of(getContext());
    }

    public ScreenTurnReport openTurnReport() throws Exception {
        final Button buttonNextTurn = getButtonById(
                ButtonsGamePanel.BUTTON_TURN_REPORT_ID);
        getRobot().clickOn(buttonNextTurn);
        WaitForAsyncUtils.waitForFxEvents();
        return ScreenTurnReport.of(getContext());
    }

    public ColonyScreen openColonyAt(final Location colonyLocation,
            final String expectedNamePart) {
        moveMouseAtLocation(colonyLocation);
        getRobot().clickOn(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        return ColonyScreen.of(getContext(), expectedNamePart);
    }

    public GamePage verifyMapRemainsVisibleAt(final Location colonyLocation) {
        verifyThatStatusBarContains(String.valueOf(colonyLocation.getX()));
        verifyThatStatusBarContains(String.valueOf(colonyLocation.getY()));
        return this;
    }

    public EuropePortScreen openEuroperPort() {
        return openEuroperPort("Europe");
    }

    public EuropePortScreen openEuroperPort(final String expectedName) {
        final Button buttonNextTurn = getButtonById(
                ButtonsGamePanel.BUTTON_EUROPE_ID);
        getRobot().clickOn(buttonNextTurn);
        WaitForAsyncUtils.waitForFxEvents();
        return EuropePortScreen.of(getContext(), expectedName);
    }

    public EuropePortScreen openEuroperPortByPressingKey() {
        getRobot().press(KeyCode.E).sleep(200, TimeUnit.MILLISECONDS)
                .release(KeyCode.E);
        WaitForAsyncUtils.waitForFxEvents();
        return EuropePortScreen.of(getContext());
    }

    /**
     * Wait until the current animation releases the next turn button. The
     * button may briefly become available between units in a multi-unit action.
     *
     * @throws TimeoutException if the next turn button stays disabled
     */
    public void waitWhileMoving() throws TimeoutException {
        final String id = ButtonsGamePanel.BUTTON_NEXT_TURN_ID;
        // Query on the FX thread while the scene graph may be changing.
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> getNodeFinder()
                        .lookup("#" + id).<Button>tryQuery()
                        .map(button -> !button.isDisabled()).orElse(true))
                        .get(60, TimeUnit.SECONDS));
    }

    /**
     * Wait for an expected unit count after an asynchronous map action.
     *
     * @param location      map tile whose units are counted
     * @param expectedCount unit count to wait for
     * @throws TimeoutException if the unit count or movement does not settle
     */
    public void waitForNumberOfUnitsAt(final Location location,
            final int expectedCount) throws TimeoutException {
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS,
                () -> getModel().getUnitsAt(location).size() == expectedCount);
        waitWhileMoving();
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyNumberOfUnitInRightPanel(int expectedUnitsInRightPanel) {
        final List<VBox> set = getListOfUnitsInRightPanel();
        assertNotNull(set);
        assertEquals(expectedUnitsInRightPanel, set.size(), String.format(
                "Expected number of unit in right panel is '%s' but there are '%s'",
                expectedUnitsInRightPanel, set.size()));
    }

    public void pressTab() {
        getRobot().push(KeyCode.TAB);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void pressP() {
        getRobot().push(KeyCode.P);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Assert that just unit at specific index is selected and other units are
     * unselected.
     *
     * @param unitIndex required unit index
     */
    public void verifyThatUnitIsSelected(final int unitIndex) {
        final int numberOfUnitsInRightPanel = getListOfUnitsInRightPanel()
                .size();
        for (int i = 0; i < numberOfUnitsInRightPanel; i++) {
            final RightPanelUnit rightPanelUnit = getRightPanelUnit(i);
            if (i == unitIndex) {
                assertTrue(rightPanelUnit.isSelected());
            } else {
                assertFalse(rightPanelUnit.isSelected());
            }
        }
    }

    public void verifyThatSelectedTileIs(final Location expectedSelectedTile) {
        final SelectedTileManager selectedTileManager = getContext()
                .getClassFromGuice(SelectedTileManager.class);
        Preconditions.checkNotNull(selectedTileManager);

        assertTrue(selectedTileManager.getSelectedTile().isPresent(),
                "No tile is selected");
        assertEquals(expectedSelectedTile,
                selectedTileManager.getSelectedTile().get(),
                String.format(
                        "Tile at %s should be selected but tile %s is really selected.",
                        expectedSelectedTile,
                        selectedTileManager.getSelectedTile().get()));
    }

    public void verifyThatThereIsNoColonyAt(final Location colonyLocation) {
        assertTrue(getModel().getColonyAt(colonyLocation).isEmpty(), String
                .format("At %s should not be any colony.", colonyLocation));
    }

    public RightPanelUnit getRightPanelUnit(final int unitIndexInRightPanel) {
        final VBox unitBox = getListOfUnitsInRightPanel()
                .get(unitIndexInRightPanel);
        return RightPanelUnit.of(getContext(), unitBox);
    }

    private List<VBox> getListOfUnitsInRightPanel() {
        return Lists.newArrayList(
                getNodeFinder().lookup(".unitPanel").queryAllAs(VBox.class));
    }

    public void verifyThatBuildColonyButtonIsHidden() {
        assertFalse(
                getNodeFinder().lookup("#buildColony").tryQuery().isPresent());
    }

    public void verifyThatPlowFieldButtonIsHidden() {
        assertFalse(
                getNodeFinder().lookup("#plowField").tryQuery().isPresent());
    }

    public void verifyThatPlowFieldButtonIsVisible() {
        assertFalse(getNodeFinder().lookup("#plowField").tryQuery().isEmpty());
    }

    public void verifyShipPresentAt(final Location location) {
        assertFalse(getModel().getUnitsAt(location).isEmpty(), String
                .format("There should be at leat on ship at %s.", location));
    }

    public ShipWrapper getShipAt(final Location location) {
        return new ShipWrapper((Ship) getModel().getUnitsAt(location).get(0));
    }

    public void verifyNumberOfUnitsAt(final Location location,
            final int expectedCount) {
        assertEquals(expectedCount, getModel().getUnitsAt(location).size());
    }

    public void verifyUnitAction(final Unit unit,
            final UnitActionType expectedAction) {
        assertEquals(expectedAction, unit.getAction().getType(),
                "Unexpected unit action");
    }

    public void verifyUnitActionPoints(final Unit unit,
            final int expectedPoints) {
        assertEquals(expectedPoints, unit.getActionPoints(),
                "Unexpected unit action points");
    }

    public void verifyUnitLocation(final Unit unit,
            final Location expectedLocation) {
        assertEquals(expectedLocation, unit.getLocation(),
                "Unexpected unit location");
    }

    public void verifyMinimumArmedKingSoldiersOnMap(final int expectedMinimum) {
        final long count = getModel().getAllUnits().stream()
                .filter(unit -> unit.getOwner().isKing())
                .filter(unit -> unit.isAtPlaceLocation() && unit.canAttack()
                        && !unit.getType().isShip())
                .count();
        assertTrue(count >= expectedMinimum,
                "Expected at least " + expectedMinimum
                        + " armed royal soldiers on land, found " + count);
    }

    public void verifyNumberOfHumanUnitsAt(final List<Location> locations,
            final int expectedCount) {
        final long count = locations.stream()
                .flatMap(location -> getModel().getUnitsAt(location).stream())
                .filter(unit -> unit.getOwner().isHuman()).count();
        assertEquals(expectedCount, count,
                "Unexpected number of escaped human units");
    }

    public void verifyTurnReportPresent(
            final Optional<ScreenTurnReport> report) {
        assertTrue(report.isPresent(), "The turn report must be shown");
    }

    public void verifySavedGame(final File savedFile) {
        assertTrue(savedFile.isFile(), "The saved game must exist");
        assertTrue(savedFile.length() > 0, "The saved game must not be empty");
    }

}
