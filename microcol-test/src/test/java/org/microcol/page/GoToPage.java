package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.gamepanel.GamePanelController;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.input.KeyCode;

/**
 * Go-to order controls, the colony chooser and the status of the selected unit.
 * Waits follow the model's own thread: an order walks its first steps, and an
 * owner turn walks the next ones, after the click or Next turn returned.
 */
public final class GoToPage extends AbstractScreen {
    public GoToPage(final TestContext context) {
        super(context);
    }

    /** Starts the order; the next map click chooses its destination. */
    public void startOrder() {
        clickOnButtonWithId("goToUnit");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Cancels the shown order with the same control. */
    public void cancelOrder() {
        clickOnButtonWithId("goToUnit");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Leaves the destination choice with Escape. */
    public void cancelPickWithEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Opens the list of colonies the model offers as destinations. */
    public void openColonyChoice() {
        clickOnButtonWithId("goToColonyUnit");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseColony(final int colonyId) {
        getRobot().clickOn("#goto-colony-" + colonyId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmColonyChoice() {
        clickOnButtonWithId("goto-colony-confirm");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelColonyChoice() {
        clickOnButtonWithId("goto-colony-cancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Wait until the unit stands on the square and the walk is over.
     *
     * @param id       unit identity
     * @param location square the unit is expected to reach
     */
    public void waitUntilUnitIsAt(final int id, final Location location)
            throws Exception {
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS,
                () -> isAt(getModel().getUnitById(id), location));
        waitUntilWalkIsOver();
    }

    /** Wait until the unit has a go-to order, for a first step that waits. */
    public void waitUntilUnitHasOrder(final int id) throws Exception {
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS, () -> getModel()
                .getGoToDestination(getModel().getUnitById(id)).isPresent());
        waitUntilWalkIsOver();
    }

    private void waitUntilWalkIsOver() throws Exception {
        final GamePanelController controller = getContext()
                .getClassFromGuice(GamePanelController.class);
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS,
                () -> !controller.isUnitMoving());
        WaitForAsyncUtils.waitForFxEvents();
    }

    private static boolean isAt(final Unit unit, final Location location) {
        return unit.isAtPlaceLocation() && unit.getLocation().equals(location);
    }

    /** Close the turn report if the turn produced one. */
    public void closeReport(final Optional<ScreenTurnReport> report) {
        report.ifPresent(ScreenTurnReport::close);
    }

    public void verifyButton(final String text, final boolean enabled) {
        final Button button = getButtonById("goToUnit");
        assertEquals(text, button.getText());
        assertEquals(enabled, !button.isDisabled());
        assertTrue(button.getWidth() + 0.5 >= button.prefWidth(-1));
    }

    /**
     * The go-to controls stay in the panel and are hidden, unlike buttons that
     * the panel removes, so presence alone does not mean they are shown.
     */
    private boolean isShown(final String cssId) {
        return getNodeFinder().lookup("#" + cssId).<javafx.scene.Node>tryQuery()
                .map(node -> node.isVisible() && node.isManaged())
                .orElse(false);
    }

    public void verifyColonyButton(final boolean shown, final boolean enabled) {
        assertEquals(shown, isShown("goToColonyUnit"));
        if (shown) {
            final Button button = getButtonById("goToColonyUnit");
            assertEquals("Go to colony", button.getText());
            assertEquals(enabled, !button.isDisabled());
        }
    }

    public void verifyMoveButtonShown(final boolean shown) {
        assertEquals(shown, isCssIdVisible("move"));
    }

    public void verifyStatus(final String text) {
        assertTrue(isShown("goToStatus"), "The go-to status is shown");
        final Labeled status = getLabeledById("goToStatus");
        assertEquals(text, status.getText());
    }

    public void verifyNoStatus() {
        assertFalse(isShown("goToStatus"), "No go-to status is shown");
    }

    public void verifyLocation(final int id, final Location location) {
        assertEquals(location, getModel().getUnitById(id).getLocation());
    }

    public void verifyMovement(final int id, final int movementThirds) {
        assertEquals(movementThirds,
                getModel().getUnitById(id).getMovementThirds());
    }

    /** Checks the saved destination and whether the route is paused. */
    public void verifyDestination(final int id, final Location target,
            final boolean paused) {
        final Unit unit = getModel().getUnitById(id);
        assertEquals(Optional.of(target), getModel().getGoToDestination(unit));
        assertEquals(paused, getModel().isGoToPaused(unit));
    }

    public void verifyNoDestination(final int id) {
        final Unit unit = getModel().getUnitById(id);
        assertEquals(Optional.empty(), getModel().getGoToDestination(unit));
        assertFalse(getModel().isGoToPaused(unit));
    }

    /** The chooser lists exactly the colonies the model offers, by name. */
    public void verifyColonyChoices(final String... names) {
        final List<String> shown = this
                .<RadioButton>getListOfNodes(".radio-button").stream()
                .filter(choice -> choice.getId() != null
                        && choice.getId().startsWith("goto-colony-"))
                .map(RadioButton::getText).sorted()
                .collect(Collectors.toList());
        assertEquals(
                List.of(names).stream().sorted().collect(Collectors.toList()),
                shown);
    }

    /** No turn report, or a report that does not repeat the pause notice. */
    public void verifyNoPauseNotice(final Optional<ScreenTurnReport> report) {
        report.ifPresent(
                shown -> shown.verifyEventMessageCount("no route to", 0));
    }

    public void verifyPauseNotice(final Optional<ScreenTurnReport> report,
            final String destination) {
        assertTrue(report.isPresent(), "The pause must be reported");
        report.get().verifyEventMessageCount("has no route to " + destination
                + "; its go-to order is paused and will be retried next turn.",
                1);
    }
}
