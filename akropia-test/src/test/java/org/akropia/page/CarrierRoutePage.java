package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;
import org.akropia.model.TradeRoute;
import org.akropia.model.Unit;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

/** Player actions and explicit assertions for the carrier assignment dialog. */
public final class CarrierRoutePage extends AbstractScreen {
    private String saved;
    private int movement;
    private org.akropia.model.Location location;

    public CarrierRoutePage(final TestContext context) {
        super(context);
    }

    public void openFromPanel() {
        // The localized action can extend below the unit panel viewport.
        getRobot().moveTo("#carrierRouteStatus");
        getRobot().scroll(3, javafx.geometry.VerticalDirection.DOWN);
        getRobot().clickOn("#changeCarrierRoute");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openFromContext() {
        getRobot().clickOn("#changeCarrierRouteContext");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @SuppressWarnings("unchecked")
    private ListView<TradeRoute> choices() {
        return getRobot().lookup("#carrierRouteChoices")
                .queryAs(ListView.class);
    }

    public void choose(final String name) {
        final int index = java.util.stream.IntStream
                .range(0, choices().getItems().size())
                .filter(i -> choices().getItems().get(i).getName().equals(name))
                .findFirst().orElseThrow();
        getRobot().clickOn("#carrierRouteChoices");
        getRobot().push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        WaitForAsyncUtils.waitForFxEvents();
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

    public void verifyPanel(final String text, final String action) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(text, getRobot().lookup("#carrierRouteStatus")
                .queryAs(Label.class).getText());
        assertEquals(action, getRobot().lookup("#changeCarrierRoute")
                .queryAs(Button.class).getText());
    }

    public void verifyDialog(final String current,
            final java.util.List<String> names, final String selected) {
        assertEquals(current, getRobot().lookup("#carrierRouteCurrent")
                .queryAs(Label.class).getText());
        assertEquals(names,
                choices().getItems().stream().map(TradeRoute::getName)
                        .collect(java.util.stream.Collectors.toList()));
        assertEquals(selected,
                choices().getSelectionModel().getSelectedItem() == null ? null
                        : choices().getSelectionModel().getSelectedItem()
                                .getName());
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
