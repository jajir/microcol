package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.event.model.ForeignStepProbe;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.components.StatusBarView;
import org.microcol.gui.screen.game.gamepanel.GamePanelComponent;
import org.microcol.gui.screen.game.gamepanel.MoveModeSupport;
import org.microcol.model.ColonySighting;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.event.UnitAttackedEvent;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/** Inspect exploration through the actual map and its information controls. */
public final class FogPage extends AbstractScreen {
    private Location previewOrigin;
    private Integer rememberedActionPoints;
    private String rememberedObservation;
    private final org.microcol.gui.screen.game.gamepanel.FogFrameProbe probe = new org.microcol.gui.screen.game.gamepanel.FogFrameProbe();
    private final ForeignStepProbe stepProbe = new ForeignStepProbe();

    public FogPage(final TestContext context) {
        super(context);
    }

    private Point2D screenPoint(final Location location) {
        final Canvas canvas = getContext()
                .getClassFromGuice(GamePanelComponent.class).getCanvas();
        final Point point = getContext().getArea()
                .convertToCanvasPoint(location).add(Tile.TILE_CENTER);
        return canvas.localToScreen(point.getX(), point.getY());
    }

    public void centerFrontier() throws Exception {
        final Location center = Location.of(29, 16);
        getRobot().press(KeyCode.CONTROL).clickOn(screenPoint(center))
                .release(KeyCode.CONTROL);
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> {
            final Canvas canvas = getContext()
                    .getClassFromGuice(GamePanelComponent.class).getCanvas();
            return Math.abs(
                    getContext().getArea().convertToCanvasPoint(center).getX()
                            - canvas.getWidth() / 2) < 3;
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void inspect(final Location location) {
        getRobot().clickOn(screenPoint(location));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void preview(final Location from, final Location target)
            throws Exception {
        previewOrigin = from;
        getRobot().moveTo(screenPoint(from)).press(MouseButton.PRIMARY)
                .moveTo(screenPoint(target));
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getContext().getClassFromGuice(
                        org.microcol.gui.screen.game.gamepanel.MouseOverTileManager.class)
                        .getMouseOverTile().filter(target::equals).isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelPreview() {
        getRobot().moveTo(screenPoint(previewOrigin))
                .release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void move(final Location from, final Location to, final int unitId)
            throws Exception {
        getRobot().moveTo(screenPoint(from)).press(MouseButton.PRIMARY)
                .moveTo(screenPoint(to)).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId).getLocation().equals(to));
        GamePage.of(getContext()).waitWhileMoving();
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyExplored(final Location location,
            final boolean expected) {
        assertEquals(expected,
                getModel().getPlayerByName("Dutch").isVisible(location));
    }

    /** Teleport a unit through the model, as the owner's own turn would. */
    public void placeUnit(final int unitId, final Location location) {
        getRobot().interact(
                () -> getModel().getUnitById(unitId).placeToLocation(location));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** The royal force takes a colony while the player cannot see it. */
    public void captureColonyByKing(final Location colony,
            final int forceUnitId) {
        getRobot().interact(() -> {
            final Unit force = getModel().getUnitById(forceUnitId);
            force.placeToLocation(colony);
            force.takeOver(getModel().getPlayerByName("Dutch's King"));
            getModel().getColonyAt(colony).orElseThrow().captureColony(force);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Erase the player's accumulated exploration to start from nothing. */
    public void forgetExploration() {
        getRobot().interact(() -> {
            try {
                final java.lang.reflect.Field owned = Player.class
                        .getDeclaredField("visibility");
                owned.setAccessible(true);
                final Object visibility = owned
                        .get(getModel().getPlayerByName("Dutch"));
                final java.lang.reflect.Field explored = visibility.getClass()
                        .getDeclaredField("visible");
                explored.setAccessible(true);
                ((java.util.Set<?>) explored.get(visibility)).clear();
            } catch (final ReflectiveOperationException failure) {
                throw new IllegalStateException(failure);
            }
        });
    }

    /** Let the model observe from a unit, as after any completed move. */
    public void observeFromUnit(final int unitId) {
        getRobot().interact(() -> {
            final Unit unit = getModel().getUnitById(unitId);
            unit.getOwner().revealMapForUnit(unit);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void rememberActionPoints(final int unitId) {
        rememberedActionPoints = getModel().getUnitById(unitId)
                .getActionPoints();
    }

    public void rememberColonyObservation(final Location location) {
        rememberedObservation = describeKnownColony(location);
    }

    public void listenForForeignSteps() {
        stepProbe.listen(getContext());
    }

    public void announceForeignStep(final int unitId, final Location from,
            final Location to) {
        stepProbe.announce(getContext(), getModel().getUnitById(unitId), from,
                to);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void announceForeignFight(final int attackerId,
            final int defenderId) {
        final Unit defender = getModel().getUnitById(defenderId);
        stepProbe.announceFight(new UnitAttackedEvent(getModel(),
                getModel().getUnitById(attackerId), defender, defender, false));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyForeignFights(final int expectedFights) {
        assertEquals(expectedFights, stepProbe.getFights());
    }

    private String describeKnownColony(final Location location) {
        return getModel().getPlayerByName("Dutch").getKnownColonyAt(location)
                .map(known -> known.getColonyName() + "|"
                        + known.getOwner().getName() + "|"
                        + known.getObservedTurn() + "|"
                        + known.getObservedYear())
                .orElse("none");
    }

    public void verifyCurrentSight(final Location location,
            final boolean expected) {
        assertEquals(expected, getModel().getPlayerByName("Dutch")
                .isCurrentlyVisible(location));
    }

    public void verifyObservedUnits(final Location location,
            final int expected) {
        assertEquals(expected, getModel().getPlayerByName("Dutch")
                .getObservedUnitsAt(location).size());
    }

    public void verifyKnownColony(final Location location,
            final String expectedName, final String expectedOwner,
            final boolean expectedCurrent) {
        final Player dutch = getModel().getPlayerByName("Dutch");
        final Optional<ColonySighting> known = dutch.getKnownColonyAt(location);
        assertTrue(known.isPresent(), "Known colony at " + location);
        assertEquals(expectedName, known.get().getColonyName());
        assertEquals(expectedOwner, known.get().getOwner().getName());
        assertEquals(expectedCurrent, dutch.isCurrentlyVisible(location));
    }

    public void verifyNoKnownColony(final Location location) {
        assertTrue(getModel().getPlayerByName("Dutch")
                .getKnownColonyAt(location).isEmpty(),
                "No colony should be known at " + location);
    }

    public void verifyColonyObservationUnchanged(final Location location) {
        assertEquals(rememberedObservation, describeKnownColony(location));
    }

    public void verifyColonyObservationRefreshed(final Location location,
            final String expectedOwner) {
        final String actual = describeKnownColony(location);
        assertNotEquals(rememberedObservation, actual);
        assertEquals(expectedOwner, actual.split("\\|")[1]);
    }

    public void verifyAttackRefused(final int unitId, final Location target) {
        final Unit unit = getModel().getUnitById(unitId);
        assertFalse(unit.isPossibleToAttackAt(target));
        assertTrue(unit.prepareAttack(target, true).isEmpty());
        assertEquals(rememberedActionPoints.intValue(), unit.getActionPoints());
    }

    public void verifyStatusBarOmits(final String hiddenText) {
        final Labeled label = getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID);
        assertFalse(label.getText().contains(hiddenText),
                () -> "Status bar discloses '" + hiddenText + "': "
                        + label.getText());
    }

    public void verifyRightPanelContains(final String expectedText) {
        assertTrue(getNodeFinder().lookup("#rightPanel").lookup(
                (java.util.function.Predicate<javafx.scene.Node>) node -> node instanceof Labeled
                        && ((Labeled) node).getText().contains(expectedText))
                .tryQuery().isPresent(), expectedText);
    }

    public void verifyForeignSteps(final int expectedEvents,
            final int expectedAnimations) {
        assertEquals(expectedEvents, stepProbe.getStepEvents());
        assertEquals(expectedAnimations, stepProbe.getAnimations());
    }

    public void verifyUnknownInformation(final String expectedText,
            final String hiddenName) {
        GamePage.of(getContext()).verifyThatStatusBarContains(expectedText);
        GamePage.of(getContext()).verifyNumberOfUnitInRightPanel(0);
        assertFalse(getNodeFinder().lookup("#rightPanel").lookup(
                (java.util.function.Predicate<javafx.scene.Node>) node -> node instanceof Labeled
                        && ((Labeled) node).getText().contains(hiddenName))
                .tryQuery().isPresent());
        assertTrue(getNodeFinder().lookup("#rightPanel").lookup(
                (java.util.function.Predicate<javafx.scene.Node>) node -> node instanceof Labeled
                        && ((Labeled) node).getText().equals("Unknown"))
                .tryQuery().isPresent());
    }

    public void verifyPreviewEmpty() {
        assertTrue(
                getContext().getClassFromGuice(MoveModeSupport.class)
                        .getMoveLocations().isEmpty(),
                () -> "Preview path: "
                        + getContext().getClassFromGuice(MoveModeSupport.class)
                                .getMoveLocations());
    }

    public void verifyPreviewEmpty(final Location hidden) {
        assertTrue(
                getContext().getClassFromGuice(MoveModeSupport.class)
                        .getMoveLocations().isEmpty(),
                () -> "Preview path: "
                        + getContext().getClassFromGuice(
                                MoveModeSupport.class).getMoveLocations()
                        + "; pointer: "
                        + getContext().getClassFromGuice(
                                org.microcol.gui.screen.game.gamepanel.MouseOverTileManager.class)
                                .getMouseOverTile()
                        + "; explored enemy: " + getModel()
                                .getPlayerByName("Dutch").isVisible(hidden));
    }

    public void verifyPreviewIncludes(final Location destination) {
        assertTrue(getContext().getClassFromGuice(MoveModeSupport.class)
                .getMoveLocations().contains(destination));
    }

    public void verifyNoFogBorder(final Location interior) {
        assertNull(
                getContext().getClassFromGuice(
                        org.microcol.gui.screen.game.gamepanel.MapManager.class)
                        .getHiddenImageCoast(interior),
                "An explored interior tile must not keep an obsolete fog border");
    }

    public void verifySightRadius(final int unitId, final int expected) {
        assertEquals(expected, getModel().getUnitById(unitId).getSightRadius());
    }

    public void verifyRevealedPixels(final Location location) throws Exception {
        final Image actual = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Image hidden = getContext().getClassFromGuice(ImageProvider.class)
                .getImage(ImageLoaderTerrain.IMG_TILE_HIDDEN);
        final Point point = getContext().getArea()
                .convertToCanvasPoint(location);
        boolean different = false;
        for (int y = 12; y < Tile.TILE_WIDTH_IN_PX - 12; y += 8) {
            for (int x = 12; x < Tile.TILE_WIDTH_IN_PX - 12; x += 8) {
                different |= hidden.getPixelReader().getArgb(x, y) != actual
                        .getPixelReader()
                        .getArgb(point.getX() + x, point.getY() + y);
            }
        }
        assertTrue(different,
                "Revealed terrain must replace fog at " + location);
    }

    public void verifyUnitLocation(final int unitId, final Location expected) {
        assertEquals(expected, getModel().getUnitById(unitId).getLocation());
    }

    public void drawDiagnosticAnimation(final Location known,
            final Location hidden) {
        getRobot().interact(() -> probe.paint(getContext(), known, hidden));
    }

    public void verifyAnimationCoveredOnlyInFog(final Location known,
            final Location hidden) {
        final Point knownPoint = getContext().getArea()
                .convertToCanvasPoint(known).add(Tile.TILE_CENTER);
        final Point hiddenPoint = getContext().getArea()
                .convertToCanvasPoint(hidden).add(Tile.TILE_CENTER);
        assertTrue(probe.isCompleted(),
                "Hidden animation must still finish and release the turn");
        assertEquals(javafx.scene.paint.Color.MAGENTA,
                probe.getFrame().getPixelReader().getColor(knownPoint.getX(),
                        knownPoint.getY()));
        assertNotEquals(javafx.scene.paint.Color.MAGENTA,
                probe.getFrame().getPixelReader().getColor(hiddenPoint.getX(),
                        hiddenPoint.getY()));
    }

    public void verifyFogPixels(final Location location) throws Exception {
        final Canvas canvas = getContext()
                .getClassFromGuice(GamePanelComponent.class).getCanvas();
        final Image actual = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Image hidden = getContext().getClassFromGuice(ImageProvider.class)
                .getImage(ImageLoaderTerrain.IMG_TILE_HIDDEN);
        final Point point = getContext().getArea()
                .convertToCanvasPoint(location);
        // Interior pixels avoid the independently drawn grid and selection
        // border.
        for (int y = 12; y < Tile.TILE_WIDTH_IN_PX - 12; y += 8) {
            for (int x = 12; x < Tile.TILE_WIDTH_IN_PX - 12; x += 8) {
                assertEquals(hidden.getPixelReader().getArgb(x, y),
                        actual.getPixelReader().getArgb(point.getX() + x,
                                point.getY() + y),
                        "Hidden tile " + location);
            }
        }
    }
}
