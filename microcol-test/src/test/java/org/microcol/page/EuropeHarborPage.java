package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.microcol.gui.image.ImageProvider;
import org.microcol.model.Goods;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/** The illustrated harbor keeps every unit reachable at its native size. */
public final class EuropeHarborPage extends AbstractScreen {
    private static final List<String> GROUPS = List.of("incoming", "outgoing",
            "dock", "pier");

    public EuropeHarborPage(final TestContext context) {
        super(context);
    }

    private ScrollPane group(final String name) {
        return getNodeFinder().lookup("#europe-" + name + "-scroll")
                .queryAs(ScrollPane.class);
    }

    public void resize(final double width, final double height) {
        getRobot().interact(() -> {
            getContext().getPrimaryStage().setWidth(width);
            getContext().getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void scrollGroup(final String name, final double position) {
        getRobot().interact(() -> group(name).setHvalue(position));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void selectShip(final int id, final boolean keyboard) {
        final ToggleButton button = getNodeFinder()
                .lookup("#europe-dock-ship-" + id).queryAs(ToggleButton.class);
        revealGameContent(button);
        if (keyboard) {
            getRobot().interact(button::requestFocus);
            getRobot().push(KeyCode.SPACE);
        } else {
            getRobot().clickOn(button);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void selectVoyage(final int id) {
        final Node ship = getNodeFinder().lookup("#voyage-ship-" + id).query();
        revealGameContent(ship);
        getRobot().clickOn(ship);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyVoyageActions(final int id) {
        assertTrue(getNodeFinder().lookup("#voyage-ship-" + id)
                .queryAs(ToggleButton.class).isSelected());
        assertNotNull(getNodeFinder().lookup("#voyage-reverse-" + id).query());
        assertNotNull(getNodeFinder().lookup("#voyage-jettison-" + id).query());
        assertTrue(getLabeledById("voyage-identity-" + id).getText()
                .endsWith("#" + id));
    }

    public void verifyGroups(final int expectedCount) {
        getRobot().interact(() -> {
            for (final String name : GROUPS) {
                final ScrollPane scroll = group(name);
                final Pane content = (Pane) scroll.getContent();
                assertEquals(expectedCount, content.getChildren().size(), name);
                assertEquals(ScrollPane.ScrollBarPolicy.ALWAYS,
                        scroll.getHbarPolicy());
                assertEquals(ScrollPane.ScrollBarPolicy.NEVER,
                        scroll.getVbarPolicy());
                assertTrue(scroll.isVisible(),
                        name + " remains available when empty");
                if (expectedCount > 20) {
                    assertTrue(content.getWidth() > scroll.getViewportBounds()
                            .getWidth(), name);
                }
            }
            assertTrue(getLabeledById("europe-dock-count").getText()
                    .endsWith("· " + expectedCount));
            assertTrue(getLabeledById("europe-pier-count").getText()
                    .endsWith("· " + expectedCount));
        });
    }

    public void verifyScrollPositions(final double incoming,
            final double outgoing, final double dock, final double pier) {
        final double[] values = { incoming, outgoing, dock, pier };
        for (int i = 0; i < GROUPS.size(); i++) {
            assertEquals(values[i], group(GROUPS.get(i)).getHvalue(), 0.001,
                    GROUPS.get(i));
        }
    }

    public void verifyLastItemVisible(final String name) {
        getRobot().interact(() -> {
            final ScrollPane scroll = group(name);
            final Pane content = (Pane) scroll.getContent();
            final Node last = content.getChildren()
                    .get(content.getChildren().size() - 1);
            final Bounds item = last.localToScene(last.getBoundsInLocal());
            final Node viewport = scroll.lookup(".viewport");
            final Bounds visible = viewport
                    .localToScene(viewport.getBoundsInLocal());
            assertTrue(item.getMinX() >= visible.getMinX() - 1, name);
            assertTrue(item.getMaxX() <= visible.getMaxX() + 1, name);
        });
    }

    public void verifySelectedCargo(final int id, final int slot,
            final Goods expected) {
        final ToggleButton selected = getNodeFinder()
                .lookup("#europe-dock-ship-" + id).queryAs(ToggleButton.class);
        assertTrue(selected.isSelected());
        assertTrue(getLabeledById("europe-cargo-title").getText()
                .endsWith("#" + id));
        new ShipWrapper((UnitWithCargo) getModel().getUnitById(id))
                .verifyCargoGoods(slot, expected);
        final javafx.scene.layout.StackPane crate = PageComponentPanelDock
                .of(getContext()).getListOfCrates().get(slot);
        final ImageView cargo = (ImageView) crate.lookup(".cargo");
        assertNotNull(cargo);
        final Label amount = (Label) crate.lookup(".cargo-amount");
        assertTrue(amount.isVisible());
        assertTrue(
                crate.getChildren().indexOf(amount) > crate.getChildren()
                        .indexOf(cargo),
                "Quantity is painted above the opaque goods artwork");
        assertSame(
                getContext().getClassFromGuice(ImageProvider.class)
                        .getGoodsTypeImage(expected.getType()),
                cargo.getImage());
        assertTrue(crate.lookupAll(".label").stream()
                .map(node -> ((Label) node).getText()).anyMatch(text -> text
                        .equals(Integer.toString(expected.getAmount()))));
    }

    public void verifyLayoutAndNativeImages() {
        getRobot().interact(() -> {
            final Region scene = getNodeFinder().lookup("#europe-harbor-scene")
                    .queryAs(Region.class);
            final Bounds incoming = group("incoming")
                    .localToScene(group("incoming").getBoundsInLocal());
            final Bounds outgoing = group("outgoing")
                    .localToScene(group("outgoing").getBoundsInLocal());
            assertTrue(incoming.getMaxX() < outgoing.getMinX());
            assertEquals(incoming.getMinY(), outgoing.getMinY(), 1);
            final Region cargo = getNodeFinder().lookup(".harbor-cargo")
                    .queryAs(Region.class);
            final Bounds cargoBounds = scene
                    .sceneToLocal(cargo.localToScene(cargo.getBoundsInLocal()));
            assertTrue(cargoBounds.getMinY() >= scene.getHeight() - 112);
            assertTrue(cargoBounds.getMaxY() <= scene.getHeight());
            final ImageProvider images = getContext()
                    .getClassFromGuice(ImageProvider.class);
            for (final Node node : ((Pane) group("dock").getContent())
                    .getChildren()) {
                final ToggleButton button = (ToggleButton) node;
                final ImageView icon = (ImageView) button.getGraphic();
                assertEquals(45, icon.getFitWidth());
                assertEquals(45, icon.getFitHeight());
                assertSame(
                        images.getUnitImage(
                                (UnitWithCargo) button.getUserData()),
                        icon.getImage());
            }
            for (final String name : List.of("incoming", "outgoing")) {
                for (final Node card : ((Pane) group(name).getContent())
                        .getChildren()) {
                    final ImageView icon = (ImageView) ((ToggleButton) card)
                            .getGraphic();
                    assertEquals(45, icon.getBoundsInLocal().getWidth());
                    assertEquals(45, icon.getBoundsInLocal().getHeight());
                    final Bounds picture = icon
                            .localToScene(icon.getBoundsInLocal());
                    final Node viewport = group(name).lookup(".viewport");
                    final Bounds visible = viewport
                            .localToScene(viewport.getBoundsInLocal());
                    assertTrue(picture.getMinY() >= visible.getMinY() - 1,
                            name + " full ship artwork: " + picture
                                    + " viewport: " + visible);
                    assertTrue(picture.getMaxY() <= visible.getMaxY() + 1);
                }
            }
            for (final Node person : ((Pane) group("pier").getContent())
                    .getChildren()) {
                final ImageView icon = (ImageView) ((Pane) person).getChildren()
                        .get(0).lookup("ImageView");
                assertEquals(45, icon.getBoundsInLocal().getWidth());
                assertEquals(45, icon.getBoundsInLocal().getHeight());
                final Bounds feet = scene.sceneToLocal(
                        icon.localToScene(icon.getBoundsInLocal()));
                assertTrue(feet.getMaxY() >= scene.getHeight() - 138,
                        "People stand on the quay: " + feet + " scene: "
                                + scene.getHeight());
                assertTrue(feet.getMaxY() <= scene.getHeight() - 114);
            }
            assertEquals(16, getListOfNodes(".panelGoods").size());
        });
    }

    public void showGoods() {
        final ScrollPane page = getNodeFinder().lookup(".screen-scroll")
                .queryAs(ScrollPane.class);
        getRobot().interact(() -> page.setVvalue(page.getVmax()));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyAllGoodsVisible() {
        getRobot().interact(() -> {
            final ScrollPane page = getNodeFinder().lookup(".screen-scroll")
                    .queryAs(ScrollPane.class);
            final Node viewport = page.lookup(".viewport");
            final Bounds visible = viewport
                    .localToScene(viewport.getBoundsInLocal());
            for (final Node node : getListOfNodes(".panelGoods")) {
                final Bounds item = node.localToScene(node.getBoundsInLocal());
                assertTrue(item.getMinX() >= visible.getMinX() - 1);
                assertTrue(item.getMaxX() <= visible.getMaxX() + 1);
                assertTrue(item.getMinY() >= visible.getMinY() - 1);
                assertTrue(item.getMaxY() <= visible.getMaxY() + 1);
            }
        });
    }
}
