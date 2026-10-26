package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

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
import org.microcol.model.NativeNation;
import org.microcol.model.NativeSettlement;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.event.UnitAttackedEvent;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Point2D;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

/** Inspect exploration through the actual map and its information controls. */
public final class FogPage extends AbstractScreen {
    /** Gold frame that marks a capital on the map. */
    private static final Color CAPITAL_FRAME = Color.web("#FFD60A");

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
        centerOn(Location.of(29, 16));
    }

    public void centerOn(final Location center) throws Exception {
        final Canvas mapCanvas = getContext()
                .getClassFromGuice(GamePanelComponent.class).getCanvas();
        final Point2D destination = mapCanvas.localToScreen(
                mapCanvas.getWidth() / 2 + Tile.TILE_CENTER.getX(),
                mapCanvas.getHeight() / 2 + Tile.TILE_CENTER.getY());
        // Openbox reserves Alt-drag; use the game's ordinary map-pan gesture.
        getRobot().moveTo(screenPoint(center)).press(MouseButton.SECONDARY)
                .moveTo(destination).release(MouseButton.SECONDARY);
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> {
            final Canvas canvas = getContext()
                    .getClassFromGuice(GamePanelComponent.class).getCanvas();
            final Point point = getContext().getArea()
                    .convertToCanvasPoint(center);
            // A pan ignores up to five pixels before crossing its drag
            // threshold.
            return Math.abs(point.getX() - canvas.getWidth() / 2) <= 5
                    && Math.abs(point.getY() - canvas.getHeight() / 2) <= 5;
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void inspect(final Location location) {
        getRobot().clickOn(screenPoint(location));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void preview(final Location from, final Location target)
            throws Exception {
        getRobot().moveTo(screenPoint(from)).press(MouseButton.PRIMARY)
                .moveTo(screenPoint(target));
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getContext().getClassFromGuice(
                        org.microcol.gui.screen.game.gamepanel.MouseOverTileManager.class)
                        .getMouseOverTile().filter(target::equals).isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelPreview() {
        // Releasing over the origin can open its colony instead of canceling.
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().release(MouseButton.PRIMARY);
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

    /**
     * The royal force wins the colony's defense battle while the player cannot
     * see it.
     */
    public void captureColonyByKing(final Location colony,
            final int forceUnitId) {
        new RoyalBattlePage(getContext()).captureColony(colony, forceUnitId);
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

    /** Point the mouse at a square so the status bar describes it. */
    public void hover(final Location location) {
        getRobot().moveTo(screenPoint(location));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * The Dutch player has learned this settlement's nation and capital status
     * by sight, and no other settlement is known there.
     */
    public void verifyNativeSettlementKnown(final Location location,
            final NativeNation expectedNation, final boolean expectedCapital) {
        final NativeSettlement known = getModel()
                .getNativeSettlementKnownTo(getModel().getPlayerByName("Dutch"),
                        location)
                .orElseThrow(() -> new AssertionError(
                        "No native settlement known at " + location));
        assertEquals(expectedNation, known.getNation());
        assertEquals(expectedCapital, known.isCapital());
    }

    public void verifyNoNativeSettlementKnown(final Location location) {
        assertTrue(
                getModel()
                        .getNativeSettlementKnownTo(
                                getModel().getPlayerByName("Dutch"), location)
                        .isEmpty(),
                () -> "Native settlement leaked at " + location);
    }

    public void verifyNativeSettlementCurrentlyVisible(final Location location,
            final boolean expected) {
        assertEquals(expected, getModel().getPlayerByName("Dutch")
                .isCurrentlyVisible(location));
    }

    public void verifyStatusBarContains(final String expectedText) {
        GamePage.of(getContext()).verifyThatStatusBarContains(expectedText);
    }

    /**
     * The drawn settlement has its nation fill at the centre of its square, and
     * a capital also has the gold frame on its left edge; an ordinary one does
     * not.
     */
    public void verifyNativeSettlementPixels(final Location location,
            final Color fill, final boolean capital) throws Exception {
        final Image actual = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Point point = getContext().getArea()
                .convertToCanvasPoint(location);
        final Point center = point.add(Tile.TILE_CENTER);
        final Color body = actual.getPixelReader().getColor(center.getX(),
                center.getY());
        assertTrue(closeTo(fill, body), () -> "Settlement fill at " + location
                + ": expected " + fill + " but was " + body);
        final Color frame = actual.getPixelReader().getColor(point.getX() + 2,
                center.getY());
        if (capital) {
            assertTrue(closeTo(CAPITAL_FRAME, frame),
                    () -> "Capital frame at " + location + ": expected "
                            + CAPITAL_FRAME + " but was " + frame);
        } else {
            assertFalse(closeTo(CAPITAL_FRAME, frame),
                    "Ordinary settlement must not have the capital frame at "
                            + location);
        }
    }

    /**
     * Keep the rendered map as review evidence under target/warehouse-qa. The
     * file is written only; it makes no claim about the map.
     */
    public void saveMapEvidence(final String fileName) throws Exception {
        final Image actual = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final File target = new File("target/warehouse-qa", fileName);
        target.getParentFile().mkdirs();
        ImageIO.write(SwingFXUtils.fromFXImage(actual, null), "png", target);
    }

    private static boolean closeTo(final Color expected, final Color actual) {
        return Math.abs(expected.getRed() - actual.getRed()) < 0.06
                && Math.abs(expected.getGreen() - actual.getGreen()) < 0.06
                && Math.abs(expected.getBlue() - actual.getBlue()) < 0.06;
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

    public void resizeMapWindow(final double width, final double height) {
        getRobot().interact(() -> {
            getContext().getPrimaryStage().setWidth(width);
            getContext().getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyGrid(final Location explored, final Location unknown,
            final boolean enabled) throws Exception {
        for (int scale : new int[] { 1, 2 }) {
            verifyGridAtScale(explored, unknown, enabled, scale);
        }
    }

    private void verifyGridAtScale(final Location explored,
            final Location unknown, final boolean enabled, final int scale)
            throws Exception {
        final Image image = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureGrid(getContext(), scale, true))
                .get(10, TimeUnit.SECONDS);
        final Point edge = getContext().getArea()
                .convertToCanvasPoint(explored);
        final Image withoutGrid = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureGrid(getContext(), scale, false))
                .get(10, TimeUnit.SECONDS);
        int ink = 0;
        int gaps = 0;
        // A frontier side can be fog; inspect all revealed sides of the tile.
        for (int along = 5; along < Tile.TILE_WIDTH_IN_PX - 5; along++) {
            for (int across = -1; across <= 1; across++) {
                for (Point offset : new Point[] { Point.of(along, across),
                        Point.of(across, along),
                        Point.of(along, Tile.TILE_WIDTH_IN_PX + across),
                        Point.of(Tile.TILE_WIDTH_IN_PX + across, along) }) {
                    final int x = (edge.getX() + offset.getX()) * scale;
                    final int y = (edge.getY() + offset.getY()) * scale;
                    if (withoutGrid.getPixelReader().getArgb(x,
                            y) != 0xFFFFFFFF) {
                        continue;
                    }
                    if (image.getPixelReader().getArgb(x, y) != 0xFFFFFFFF) {
                        ink++;
                    } else {
                        gaps++;
                    }
                }
            }
        }
        if (enabled) {
            assertTrue(ink > 0, "Explored edge has visible dashes");
            assertTrue(gaps > 0, "Explored edge has gaps between dashes");
        } else {
            assertEquals(0, ink, "Disabled grid leaves no lines");
        }
        final Point hidden = getContext().getArea()
                .convertToCanvasPoint(unknown);
        for (int y = 0; y < Tile.TILE_WIDTH_IN_PX; y++) {
            for (int x = 0; x < Tile.TILE_WIDTH_IN_PX; x++) {
                assertEquals(0xFF9B9486,
                        image.getPixelReader().getArgb(
                                (hidden.getX() + x) * scale,
                                (hidden.getY() + y) * scale),
                        "Unknown square is uniformly gray, including its edges");
            }
        }
    }

    public void verifyGridUnderRoundedFog() throws Exception {
        for (int scale : new int[] { 1, 2 }) {
            final Image image = WaitForAsyncUtils.asyncFx(
                    () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                            .captureGrid(getContext(), scale, true))
                    .get(10, TimeUnit.SECONDS);
            getRobot().interact(() -> {
                final org.microcol.gui.screen.game.gamepanel.MapManager manager = getContext()
                        .getClassFromGuice(
                                org.microcol.gui.screen.game.gamepanel.MapManager.class);
                final org.microcol.model.Model model = getContext()
                        .getClassFromGuice(
                                org.microcol.gui.event.model.GameModelController.class)
                        .getModel();
                final Player player = model.getPlayerByName("Dutch");
                int checked = 0;
                for (int y = 1; y <= model.getMap().getMaxLocationY(); y++) {
                    for (int x = 1; x <= model.getMap()
                            .getMaxLocationX(); x++) {
                        final Location at = Location.of(x, y);
                        final Image mask = manager.getHiddenImageCoast(at);
                        if (!player.isVisible(at) || mask == null) {
                            continue;
                        }
                        final Point point = getContext().getArea()
                                .convertToCanvasPoint(at);
                        for (int py = 1; py < Tile.TILE_WIDTH_IN_PX - 1; py++) {
                            for (int px = 1; px < Tile.TILE_WIDTH_IN_PX
                                    - 1; px++) {
                                final int ix = (point.getX() + px) * scale;
                                final int iy = (point.getY() + py) * scale;
                                if (ix < 0 || iy < 0 || ix >= image.getWidth()
                                        || iy >= image.getHeight()) {
                                    continue;
                                }
                                boolean opaqueFog = true;
                                for (int dy = -1; dy <= 1; dy++) {
                                    for (int dx = -1; dx <= 1; dx++) {
                                        opaqueFog &= mask.getPixelReader()
                                                .getArgb(px + dx,
                                                        py + dy) == 0xFF9B9486;
                                    }
                                }
                                if (opaqueFog) {
                                    assertEquals(0xFF9B9486,
                                            image.getPixelReader().getArgb(ix,
                                                    iy),
                                            "Rounded fog covers grid inside explored tile "
                                                    + at + " at " + px + ","
                                                    + py + " scale " + scale);
                                    checked++;
                                }
                            }
                        }
                    }
                }
                assertTrue(checked > 0,
                        "Inspect rounded fog inside explored squares");
            });
        }
    }

    public void verifyFogTileJoins() {
        getRobot().interact(() -> {
            final ImageProvider images = getContext()
                    .getClassFromGuice(ImageProvider.class);
            for (String name : new String[] { "hidden-i-shapeNorth-04",
                    "hidden-i-shapeNorth-b4" }) {
                final Image tile = images.getImage(name);
                for (int y = 0; y < 3; y++) {
                    assertEquals(0xFF9B9486,
                            tile.getPixelReader()
                                    .getArgb(Tile.TILE_WIDTH_IN_PX - 1, y),
                            "No ink protrudes along the tile join: " + name);
                }
            }
        });
    }

    public void verifyUnknownMark(final Location location,
            final boolean expected) throws Exception {
        verifyExplored(location, !expected);
        final Image actual = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Image fog = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureGrid(getContext(), 1, false))
                .get(10, TimeUnit.SECONDS);
        final Image reference = WaitForAsyncUtils.asyncFx(() -> {
            final Canvas canvas = new Canvas(Tile.TILE_WIDTH_IN_PX,
                    Tile.TILE_WIDTH_IN_PX);
            final javafx.scene.canvas.GraphicsContext graphics = canvas
                    .getGraphicsContext2D();
            graphics.setFill(Color.web("#9B9486"));
            graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            graphics.setStroke(Color.web("#292824"));
            graphics.setLineWidth(1.6);
            graphics.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            graphics.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            graphics.beginPath();
            graphics.moveTo(17, 17);
            graphics.bezierCurveTo(16.7, 13.5, 20.3, 11.9, 23.6, 12.4);
            graphics.bezierCurveTo(27.6, 12.7, 29, 16.4, 27.1, 19);
            graphics.bezierCurveTo(25.6, 21, 22.8, 21.8, 22.5, 24.2);
            graphics.lineTo(22.4, 26);
            graphics.moveTo(22.2, 31);
            graphics.lineTo(22.3, 31.5);
            graphics.stroke();
            return canvas.snapshot(null, null);
        }).get(10, TimeUnit.SECONDS);
        final Point point = getContext().getArea()
                .convertToCanvasPoint(location);
        int ink = 0;
        for (int y = 4; y < Tile.TILE_WIDTH_IN_PX - 4; y++) {
            for (int x = 4; x < Tile.TILE_WIDTH_IN_PX - 4; x++) {
                final int pixel = reference.getPixelReader().getArgb(x, y);
                if (expected) {
                    assertEquals(pixel,
                            actual.getPixelReader().getArgb(point.getX() + x,
                                    point.getY() + y),
                            "Unknown square contains only gray and its question mark");
                }
                if (pixel != 0xFF9B9486) {
                    ink++;
                    assertEquals(expected ? pixel : 0xFFFFFFFF,
                            fog.getPixelReader().getArgb(point.getX() + x,
                                    point.getY() + y),
                            "Exploration removes the question mark from the fog layer");
                }
            }
        }
        assertTrue(ink > 10, "The reference question mark must be visible");
    }

    public void verifyFogPixels(final Location location) throws Exception {
        final Image actual = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Point point = getContext().getArea()
                .convertToCanvasPoint(location);
        // Interior pixels avoid the selection border.
        for (int y = 12; y < Tile.TILE_WIDTH_IN_PX - 12; y += 8) {
            for (int x = 12; x < Tile.TILE_WIDTH_IN_PX - 12; x += 8) {
                assertEquals(
                        0xFF9B9486, actual.getPixelReader()
                                .getArgb(point.getX() + x, point.getY() + y),
                        "Hidden tile " + location);
            }
        }
    }
}
