package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.akropia.model.Colony;
import org.akropia.model.TradeRoute;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRouteStop;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 * Trade route editor opened from the colony screen. Choices are made through
 * the visible combo boxes with the keyboard, as a player would.
 */
public final class TradeRouteDialog extends AbstractScreen {

    public TradeRouteDialog(final TestContext context) {
        super(context);
    }

    public void open() {
        clickOnButtonWithId("tradeRouteOpen");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void enterName(final String value) {
        final TextField input = name();
        getRobot().clickOn(input).push(KeyCode.END)
                .eraseText(input.getText().length()).write(value);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseKind(final TradeRouteKind kind) {
        getRobot().clickOn(kindBox()).push(KeyCode.HOME);
        if (kind == TradeRouteKind.SEA) {
            getRobot().push(KeyCode.DOWN);
        }
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseStop(final int row, final TradeRouteStop stop) {
        final ComboBox<TradeRouteStop> combo = stopBox(row);
        final int index = combo.getItems().indexOf(stop);
        if (index < 0) {
            throw new IllegalStateException("Stop is not offered: " + stop);
        }
        getRobot().clickOn(combo).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Chooses the starting colony with the keyboard, as offered when the editor
     * opens from the map planner.
     */
    public void chooseStartColony(final String colonyName) {
        final ComboBox<Colony> combo = startBox();
        int index = -1;
        for (int i = 0; i < combo.getItems().size(); i++) {
            if (combo.getItems().get(i).getName().equals(colonyName)) {
                index = i;
            }
        }
        if (index < 0) {
            throw new IllegalStateException(
                    "Colony is not offered: " + colonyName);
        }
        getRobot().clickOn(combo).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void create() {
        clickOnButtonWithId("tradeRouteCreate");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancel() {
        clickOnButtonWithId("tradeRouteCancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    private TextField name() {
        return getNodeFinder().lookup("#tradeRouteName")
                .queryAs(TextField.class);
    }

    private ComboBox<Colony> startBox() {
        return getNodeFinder().lookup("#tradeRouteStartColony").queryComboBox();
    }

    private ComboBox<TradeRouteKind> kindBox() {
        return getNodeFinder().lookup("#tradeRouteKind").queryComboBox();
    }

    private ComboBox<TradeRouteStop> stopBox(final int row) {
        return getNodeFinder().lookup("#tradeRouteStop" + row).queryComboBox();
    }

    private String label(final TradeRouteStop stop) {
        return stop.isEurope() ? "Home port"
                : getModel().getColonyById(stop.getColonyId()).orElseThrow()
                        .getName();
    }

    private List<TradeRoute> routesOfOwner() {
        return getModel().getTradeRoutes(
                getModel().getColonyById(0).orElseThrow().getOwner());
    }

    public void verifyOpenedFor(final String colonyName) {
        assertEquals("Trade route from " + colonyName,
                getLabeledById("tradeRouteTitle").getText());
    }

    public void verifyTitle(final String expected) {
        assertEquals(expected, getLabeledById("tradeRouteTitle").getText());
    }

    public void verifyNameAppearance(final boolean focused) {
        final TextField input = name();
        assertEquals(focused, input.isFocused());
        assertEquals(
                javafx.scene.paint.Color.web(focused ? "#E7E7D0" : "#FFF9EC"),
                input.getBackground().getFills().get(0).getFill());
        assertEquals(1, input.getBorder().getImages().size());
        assertTrue(input.getBorder().getImages().get(0).getImage().getUrl()
                .endsWith("/trade-route-name-frame.png"));
        assertEquals("Playpen Sans", input.getFont().getFamily());
    }

    public void verifyKind(final TradeRouteKind expected) {
        assertEquals(expected, kindBox().getValue());
    }

    /**
     * @param expectedChosen  colony chosen to start the route
     * @param expectedOffered every owned colony offered, in order
     */
    public void verifyStartColony(final String expectedChosen,
            final String... expectedOffered) {
        assertTrue(startBox().isVisible());
        assertEquals(expectedChosen, startBox().getValue().getName());
        assertEquals(List.of(expectedOffered), startBox().getItems().stream()
                .map(Colony::getName).collect(Collectors.toList()));
    }

    public void verifyStopOptions(final int row, final String... expected) {
        assertEquals(List.of(expected), stopBox(row).getItems().stream()
                .map(this::label).collect(Collectors.toList()));
    }

    /** A reopened editor starts a new draft with no stop chosen. */
    public void verifyNoStopsChosen() {
        for (int row = 0; row < 4; row++) {
            assertNull(stopBox(row).getValue(), "stop row " + row);
        }
        assertEquals("", name().getText());
    }

    public void verifyMessage(final String expected) {
        assertEquals(expected, getLabeledById("tradeRouteError").getText());
    }

    public void verifyCreateEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById("tradeRouteCreate").isDisabled());
    }

    public void verifyExistingRoutes(final String expected) {
        assertEquals(expected, getLabeledById("tradeRouteExisting").getText());
    }

    public void verifyDialogClosed() {
        assertTrue(getNodeFinder().lookup("#tradeRouteDialog").tryQuery()
                .isEmpty());
    }

    public void verifyStoredRoutes(final int expectedCount) {
        assertEquals(expectedCount, routesOfOwner().size());
    }

    public void verifyStoredRoute(final String expectedName,
            final TradeRouteKind expectedKind, final String... expectedStops) {
        final List<TradeRoute> matching = routesOfOwner().stream()
                .filter(route -> route.getName().equals(expectedName))
                .collect(Collectors.toList());
        assertEquals(1, matching.size());
        assertEquals(expectedKind, matching.get(0).getKind());
        assertEquals(List.of(expectedStops), matching.get(0).getStops().stream()
                .map(this::label).collect(Collectors.toList()));
    }
}
