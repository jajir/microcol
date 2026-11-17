package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;
import org.akropia.model.TradeRoute;
import org.akropia.model.Unit;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;

/** Player actions and explicit assertions for the carrier assignment dialog. */
public final class CarrierRoutePage extends AbstractScreen {
    private String saved;
    private int movement;
    private org.akropia.model.Location location;

    public CarrierRoutePage(final TestContext context) {
        super(context);
    }

    public void openFromContext() {
        getRobot().clickOn("#changeCarrierRouteContext");
        WaitForAsyncUtils.waitForFxEvents();
    }

    private java.util.List<ToggleButton> choices() {
        return getRobot().lookup("#carrierRouteChoices").queryAs(VBox.class)
                .getChildren().stream().filter(ToggleButton.class::isInstance)
                .map(ToggleButton.class::cast)
                .collect(java.util.stream.Collectors.toList());
    }

    private ToggleButton choice(final String name) {
        return choices().stream()
                .filter(button -> ((TradeRoute) button.getUserData()).getName()
                        .equals(name))
                .findFirst().orElseThrow();
    }

    public void choose(final String name) {
        getRobot().clickOn(choice(name));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void addManyRoutes() {
        getRobot().interact(() -> {
            final TradeRoute template = getModel()
                    .getTradeRoutes(getModel().getUnitById(4).getOwner())
                    .stream()
                    .filter(route -> route.getName().equals("Supply circuit"))
                    .findFirst().orElseThrow();
            for (int i = 0; i < 20; i++) {
                getModel().createTradeRoute(template.getOwner(),
                        String.format("ZZ extra %02d", i), template.getKind(),
                        template.getStartColonyId(), template.getStops());
            }
        });
    }

    public void scrollToLastRoute() {
        getRobot().moveTo("#carrierRouteScroll").scroll(40,
                javafx.geometry.VerticalDirection.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyRouteLabel(final String name, final String label) {
        assertEquals(label, choice(name).getText());
    }

    public void verifyScrollableChoices(final boolean expected) {
        final ScrollPane pane = getRobot().lookup("#carrierRouteScroll")
                .queryAs(ScrollPane.class);
        assertEquals(expected, pane.lookupAll(".scroll-bar").stream()
                .map(ScrollBar.class::cast)
                .anyMatch(bar -> bar
                        .getOrientation() == javafx.geometry.Orientation.VERTICAL
                        && bar.isVisible()));
    }

    public void confirm() {
        getRobot().clickOn("#carrierRouteOk");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmWithKeyboard() {
        final Button button = getRobot().lookup("#carrierRouteOk")
                .queryAs(Button.class);
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelWithEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyDialogClosed() {
        assertTrue(
                getRobot().lookup("#carrierRouteDialog").queryAll().isEmpty());
    }

    public void remember(final int id) {
        saved = new com.google.gson.Gson().toJson(getModel().save());
        final Unit unit = getModel().getUnitById(id);
        movement = unit.getMovementThirds();
        location = unit.getLocation();
    }

    public void verifyUnchanged() {
        assertEquals(saved,
                new com.google.gson.Gson().toJson(getModel().save()));
    }

    public void verifyPanel(final String text) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(text, getRobot().lookup("#carrierRouteStatus")
                .queryAs(Label.class).getText());
        assertTrue(
                getRobot().lookup("#changeCarrierRoute").queryAll().isEmpty(),
                "The carrier card only displays route status");
    }

    public void verifyDialog(final String current,
            final java.util.List<String> names, final String selected) {
        assertEquals(current, getRobot().lookup("#carrierRouteCurrent")
                .queryAs(Label.class).getText());
        assertEquals(names,
                choices().stream().map(
                        button -> ((TradeRoute) button.getUserData()).getName())
                        .collect(java.util.stream.Collectors.toList()));
        assertEquals(selected,
                choices().stream().filter(ToggleButton::isSelected).map(
                        button -> ((TradeRoute) button.getUserData()).getName())
                        .findFirst().orElse(null));
        assertEquals(selected == null, getRobot().lookup("#carrierRouteOk")
                .queryAs(Button.class).isDisabled());
    }

    public void verifyAssigned(final int id, final String name,
            final int stop) {
        final Unit unit = getModel().getUnitById(id);
        final var assignment = getModel().getTradeRouteAssignment(unit)
                .orElseThrow();
        assertEquals(name, getModel().getTradeRoutes(unit.getOwner()).stream()
                .filter(route -> route.getId() == assignment.getRouteId())
                .findFirst().orElseThrow().getName());
        assertEquals(stop, assignment.getStopIndex());
        assertEquals(movement, unit.getMovementThirds());
        assertEquals(location, unit.getLocation());
    }
}
