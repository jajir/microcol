package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.europe.Europe;
import org.microcol.i18n.I18n;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.Goods;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

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

    private ScrollBar scrollbar(final ScrollPane scroll,
            final Orientation orientation) {
        // Node.lookup ignores pseudo-class state such as :horizontal.
        return scroll.getChildrenUnmodifiable().stream()
                .filter(ScrollBar.class::isInstance).map(ScrollBar.class::cast)
                .filter(bar -> bar.getOrientation() == orientation).findFirst()
                .orElseThrow();
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

    public void dragGroupToEnd(final String name) {
        final ScrollBar bar = scrollbar(group(name), Orientation.HORIZONTAL);
        revealGameContent(bar);
        final Node thumb = bar.lookup(".thumb");
        final Node track = bar.lookup(".track");
        final Bounds bounds = track.localToScreen(track.getLayoutBounds());
        getRobot().drag(thumb, MouseButton.PRIMARY).dropTo(bounds.getMaxX() - 2,
                (bounds.getMinY() + bounds.getMaxY()) / 2);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyHorizontalScrollbars(final boolean expectedVisible) {
        getRobot().interact(() -> {
            for (final String name : GROUPS) {
                final ScrollBar bar = scrollbar(group(name),
                        Orientation.HORIZONTAL);
                assertEquals(expectedVisible, bar.isVisible(),
                        name + " viewport=" + group(name).getViewportBounds()
                                + " content="
                                + group(name).getContent().getLayoutBounds()
                                + " scroll=" + group(name).getLayoutBounds());
                if (expectedVisible) {
                    verifyHandDrawnScrollbar(bar);
                }
            }
        });
    }

    public void verifyPageVerticalScrollbar() {
        getRobot().interact(() -> {
            final ScrollPane page = getNodeFinder().lookup(".screen-scroll")
                    .queryAs(ScrollPane.class);
            final ScrollBar bar = scrollbar(page, Orientation.VERTICAL);
            assertTrue(bar.isVisible());
            verifyHandDrawnScrollbar(bar);
        });
    }

    private void verifyHandDrawnScrollbar(final ScrollBar bar) {
        final Region track = (Region) bar.lookup(".track");
        final Region thumb = (Region) bar.lookup(".thumb");
        assertNotNull(track.getShape(), "Hand-drawn rail");
        assertNotNull(thumb.getShape(), "Hand-drawn thumb");
        assertEquals(Color.web("#FFF9EC"),
                bar.getBackground().getFills().get(0).getFill());
        assertTrue(List.of(Color.web("#DEC79B"), Color.web("#CBB17F"))
                .contains(thumb.getBackground().getFills().get(0).getFill()));
    }

    public void selectShip(final int id, final boolean keyboard) {
        final ToggleButton button = getNodeFinder()
                .lookup("#europe-dock-ship-" + id).queryAs(ToggleButton.class);
        // Activating an already selected toggle would deselect it.
        if (button.isSelected()) {
            return;
        }
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

    MenuItem voyageOrder(final int id, final String action) {
        return getNodeFinder().lookup("#voyage-ship-" + id)
                .queryAs(ToggleButton.class).getContextMenu().getItems()
                .stream().filter(item -> ("voyage-" + action + "-" + id)
                        .equals(item.getId()))
                .findFirst().orElseThrow();
    }

    public void openVoyageOrders(final int id, final boolean keyboard) {
        final ToggleButton ship = getNodeFinder().lookup("#voyage-ship-" + id)
                .queryAs(ToggleButton.class);
        revealGameContent(ship);
        if (keyboard) {
            getRobot().interact(ship::requestFocus);
            getRobot().push(KeyCode.SHIFT, KeyCode.F10);
        } else {
            getRobot().clickOn(ship, MouseButton.SECONDARY);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closeVoyageOrders() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyVoyageOrdersShowing(final int id,
            final boolean expected) {
        assertEquals(expected, getNodeFinder().lookup("#voyage-ship-" + id)
                .queryAs(ToggleButton.class).getContextMenu().isShowing());
    }

    public void verifyVoyageActions(final int id) {
        assertTrue(getNodeFinder().lookup("#voyage-ship-" + id)
                .queryAs(ToggleButton.class).isSelected());
        assertFalse(voyageOrder(id, "reverse").isDisable());
        assertFalse(voyageOrder(id, "jettison").isDisable());
        assertTrue(getLabeledById("voyage-identity-" + id).getText()
                .endsWith("#" + id));
    }

    public void verifyGroups(final int expectedCount) {
        getRobot().interact(() -> {
            for (final String name : GROUPS) {
                final ScrollPane scroll = group(name);
                final Pane content = (Pane) scroll.getContent();
                assertEquals(expectedCount, content.getChildren().size(), name);
                assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED,
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
            final I18n i18n = getContext().getClassFromGuice(I18n.class);
            assertEquals(i18n.get(Europe.harborDock),
                    getLabeledById("europe-dock-count").getText());
            assertEquals(i18n.get(Europe.harborPier),
                    getLabeledById("europe-pier-count").getText());
        });
    }

    public void departSelectedShip(final boolean keyboard) {
        final Button button = getNodeFinder().lookup("#europe-depart")
                .queryAs(Button.class);
        revealGameContent(button);
        if (keyboard) {
            getRobot().interact(button::requestFocus);
            getRobot().push(KeyCode.SPACE);
        } else {
            getRobot().clickOn(button);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyDepartureDesign() {
        getRobot().interact(() -> {
            final Button button = getNodeFinder().lookup("#europe-depart")
                    .queryAs(Button.class);
            final Button toolbar = getNodeFinder().lookup("#closeButtonId")
                    .queryAs(Button.class);
            assertEquals(toolbar.getFont(), button.getFont());
            assertEquals(toolbar.getHeight(), button.getHeight(), 0.1);
            assertEquals(toolbar.getPadding(), button.getPadding());
            assertEquals(toolbar.getGraphicTextGap(),
                    button.getGraphicTextGap());
            final ImageView icon = (ImageView) button.getGraphic();
            assertEquals(32, icon.getFitWidth());
            assertEquals(32, icon.getFitHeight());
            assertFalse(button.isDisabled());
            final Label pageTitle = getNodeFinder()
                    .lookup(".screen-toolbar .screen-title")
                    .queryAs(Label.class);
            final I18n i18n = getContext().getClassFromGuice(I18n.class);
            assertEquals(i18n.get(Europe.sailToNewWorld), button.getText());
            for (final String name : List.of("europe", "new-world")) {
                final Label heading = getNodeFinder()
                        .lookup(".to-" + name + " .screen-title")
                        .queryAs(Label.class);
                assertEquals(pageTitle.getFont(), heading.getFont());
                assertEquals(
                        i18n.get(name.equals("europe")
                                ? Europe.shipsTravelingToEurope
                                : Europe.shipsTravelingToNewWorld),
                        heading.getText());
            }
        });
    }

    public void verifyDepartedShip(final int id, final int slot,
            final Goods cargo, final int expectedDocked,
            final int expectedOutbound) {
        getRobot().interact(() -> {
            final UnitWithCargo ship = (UnitWithCargo) getModel()
                    .getUnitById(id);
            assertTrue(ship.isAtHighSea());
            assertFalse(((PlaceHighSea) ship.getPlace()).isTravelToEurope());
            new ShipWrapper(ship).verifyCargoGoods(slot, cargo);
            assertEquals(expectedDocked,
                    ((Pane) group("dock").getContent()).getChildren().size());
            assertEquals(expectedOutbound,
                    ((Pane) group("outgoing").getContent()).getChildren()
                            .size());
            assertTrue(getNodeFinder().lookup("#voyage-ship-" + id)
                    .queryAs(ToggleButton.class).isSelected());
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

    public void verifyImmigrationAppearance() {
        getRobot().interact(() -> {
            final Label progress = getNodeFinder()
                    .lookup("#immigration-progress").queryAs(Label.class);
            assertTrue(progress.isVisible());
            assertFalse(progress.getText().isBlank());
            assertEquals(Color.web("#27271f"), progress.getTextFill());
            assertTrue(progress.getBackground().getFills().stream().allMatch(
                    fill -> Color.TRANSPARENT.equals(fill.getFill())));
            final ScrollPane page = getNodeFinder().lookup(".screen-scroll")
                    .queryAs(ScrollPane.class);
            final Node viewport = page.lookup(".viewport");
            assertTrue(viewport.localToScene(viewport.getBoundsInLocal())
                    .contains(progress
                            .localToScene(progress.getBoundsInLocal())));
        });
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
            assertTrue(cargo.getBackground().getFills().stream().allMatch(
                    fill -> Color.TRANSPARENT.equals(fill.getFill())));
            assertTrue(
                    cargo.getBorder() == null || cargo.getBorder().isEmpty());
            final Region market = getNodeFinder().lookup(".goods")
                    .queryAs(Region.class);
            assertEquals(Color.web("#FFF9EC"),
                    market.getBackground().getFills().get(0).getFill());
            for (final String name : List.of("dock", "incoming", "outgoing")) {
                for (final Node node : ((Pane) group(name).getContent())
                        .getChildren()) {
                    final ToggleButton ship = (ToggleButton) node;
                    assertEquals(ContentDisplay.GRAPHIC_ONLY,
                            ship.getContentDisplay());
                    assertTrue(ship.getText().isEmpty());
                    assertTrue(ship.getBackground().getFills().stream()
                            .allMatch(fill -> Color.TRANSPARENT
                                    .equals(fill.getFill())));
                    assertEquals(ship.isSelected(), ship.getBorder() != null
                            && !ship.getBorder().getImages().isEmpty());
                    assertNotNull(ship.getAccessibleText());
                }
            }
            assertTrue(getNodeFinder().lookup(".europe-harbor Button")
                    .queryAll().stream()
                    .noneMatch(node -> node.getId() != null && (node.getId()
                            .startsWith("voyage-reverse-")
                            || node.getId().startsWith("voyage-jettison-"))));
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

    /**
     * Both voyage headings sit below the painted horizon, centered above their
     * own list, with a small visible gap above the ship frames.
     */
    public void verifyVoyageHeadingSpacing() {
        getRobot().interact(() -> {
            final Region scene = getNodeFinder().lookup("#europe-harbor-scene")
                    .queryAs(Region.class);
            // The horizon is painted a quarter down the sea above the quay.
            final double horizon = (scene.getHeight() - 112) * 0.25;
            final List<Double> tops = new ArrayList<>();
            for (final String name : List.of("europe", "new-world")) {
                final Label heading = getNodeFinder()
                        .lookup(".to-" + name + " .screen-title")
                        .queryAs(Label.class);
                final Text text = (Text) heading.lookup(".text");
                final ScrollPane scroll = group(
                        name.equals("europe") ? "incoming" : "outgoing");
                final Bounds title = scene.sceneToLocal(
                        heading.localToScene(heading.getLayoutBounds()));
                final Bounds row = scene.sceneToLocal(
                        scroll.localToScene(scroll.getLayoutBounds()));
                assertEquals(heading.getText(), text.getText(),
                        name + " heading is not truncated");
                assertEquals(Pos.CENTER, heading.getAlignment());
                assertEquals(row.getCenterX(),
                        scene.sceneToLocal(
                                text.localToScene(text.getBoundsInLocal()))
                                .getCenterX(),
                        1, name + " heading is centered above its list");
                assertTrue(title.getMinY() >= horizon + 4,
                        name + " heading is below the horizon: " + title
                                + " horizon: " + horizon);
                final double listGap = row.getMinY() - title.getMaxY();
                assertTrue(listGap >= 2 && listGap <= 12,
                        name + " list gap: " + listGap);
                for (final Node node : ((Pane) scroll.getContent())
                        .getChildren()) {
                    final ToggleButton ship = (ToggleButton) node;
                    final double gap = scene
                            .sceneToLocal(
                                    ship.localToScene(ship.getLayoutBounds()))
                            .getMinY() - title.getMaxY();
                    assertTrue(gap >= 2, ship.getId()
                            + " frame keeps a visible gap: " + gap);
                    if (ship.isSelected()) {
                        assertTrue(gap <= 12, ship.getId()
                                + " selected frame stays compact: " + gap);
                    }
                }
                tops.add(title.getMinY());
            }
            assertEquals(tops.get(0), tops.get(1), 1,
                    "Both voyage groups share one placement");
        });
    }

    public void verifyGoodsContentWidth(final double expectedWidth) {
        getRobot().interact(() -> {
            final Node frame = getNodeFinder().lookup("#europeMarketFrame")
                    .query();
            final Node content = frame.lookup(".game-bar-content");
            final Bounds goods = content
                    .localToScene(content.getLayoutBounds());
            assertEquals(expectedWidth, goods.getWidth(), 1);
            for (final String selector : List.of(".screen-toolbar",
                    "#statusBar")) {
                final Node bar = getNodeFinder().lookup(selector).query();
                final Node inner = bar.lookup(".game-bar-content");
                final Bounds bounds = inner
                        .localToScene(inner.getLayoutBounds());
                assertEquals(bounds.getMinX(), goods.getMinX(), 1,
                        selector + " shares the left goods inset");
                assertEquals(bounds.getMaxX(), goods.getMaxX(), 1,
                        selector + " shares the right goods inset");
            }
        });
    }

    public void verifyContentAlignment() {
        getRobot().interact(() -> {
            final Node frame = getNodeFinder().lookup("#europeMarketFrame")
                    .query();
            final Node content = frame.lookup(".game-bar-content");
            final Bounds edges = content
                    .localToScene(content.getLayoutBounds());
            for (final String selector : List.of("#immigration-progress",
                    ".to-europe .screen-title", "#europe-dock-count",
                    "#europe-cargo-title")) {
                final Label label = getNodeFinder().lookup(selector)
                        .queryAs(Label.class);
                final Bounds bounds = label
                        .localToScene(label.getLayoutBounds());
                assertEquals(edges.getMinX(), bounds.getMinX(), 1, selector);
                assertEquals(0, label.getPadding().getLeft(), 0.01, selector);
            }
            final Node crate = PageComponentPanelDock.of(getContext())
                    .getListOfCrates().get(0);
            assertEquals(edges.getMinX(),
                    crate.localToScene(crate.getLayoutBounds()).getMinX(), 1,
                    "Cargo starts at the common left edge");
            final List<Node> goods = getListOfNodes(".panelGoods");
            assertEquals(16, goods.size());
            double previousRight = 0;
            double expectedGap = 0;
            for (int index = 0; index < goods.size(); index++) {
                final ImageView icon = (ImageView) goods.get(index)
                        .lookup("ImageView");
                final Bounds bounds = icon.localToScene(icon.getLayoutBounds());
                assertEquals(45, bounds.getWidth(), 0.01);
                assertEquals(45, bounds.getHeight(), 0.01);
                if (index == 0) {
                    assertEquals(edges.getMinX(), bounds.getMinX(), 1,
                            "First goods icon starts at the left edge");
                } else if (index == 1) {
                    expectedGap = bounds.getMinX() - previousRight;
                    assertTrue(expectedGap >= 0, "Goods do not overlap");
                } else {
                    assertEquals(expectedGap, bounds.getMinX() - previousRight,
                            1, "Goods have equal gaps");
                }
                previousRight = bounds.getMaxX();
            }
            assertEquals(edges.getMaxX(), previousRight, 1,
                    "Last goods icon ends at the right edge");
            for (final String selector : List.of(".to-new-world .screen-title",
                    "#europe-pier-count")) {
                final Node heading = getNodeFinder().lookup(selector).query();
                assertEquals(edges.getCenterX() + 16,
                        heading.localToScene(heading.getLayoutBounds())
                                .getMinX(),
                        1, "Right headings keep the second column");
            }
        });
    }

    public void verifyGoodsPricesCentered() {
        getRobot().interact(() -> {
            final List<Node> goods = getListOfNodes(".panelGoods");
            assertEquals(16, goods.size());
            for (final Node good : goods) {
                final ImageView icon = (ImageView) good.lookup("ImageView");
                final Label price = (Label) good.lookup(".label");
                final Node text = price.lookup(".text");
                final Bounds imageBounds = icon
                        .localToScene(icon.getBoundsInLocal());
                final Bounds textBounds = text
                        .localToScene(text.getBoundsInLocal());
                assertEquals(imageBounds.getCenterX(), textBounds.getCenterX(),
                        1, good.getId());
                assertTrue(textBounds.getMinY() >= imageBounds.getMaxY(),
                        good.getId() + " price is below its icon");
                assertEquals(TextAlignment.CENTER, price.getTextAlignment(),
                        good.getId() + " price lines are centered");
            }
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
