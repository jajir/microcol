package org.microcol.gui.screen.game.components;

import java.util.Arrays;

import org.microcol.gui.Loc;
import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.MinimapProjection;
import org.microcol.gui.screen.game.gamepanel.ScrollingManager;
import org.microcol.gui.screen.game.gamepanel.VisibleAreaService;
import org.microcol.i18n.I18n;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Terrain;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleRole;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * The human player's explored world; navigation never selects a tile or unit.
 */
@Singleton
public final class WorldMinimap extends VBox {
    private static final double WIDTH = 220, HEIGHT = 180;
    private static final Color INK = Color.web("#34332b");
    private static final Color PAPER = Color.web("#fff9ec");
    private static final Color[] COLORS = { Color.web("#9c9585"),
            Color.web("#649b97"), Color.web("#b6c78d"), Color.web("#6f8953"),
            Color.web("#c5c18a"), Color.web("#cbbb76"), Color.web("#b6b77b"),
            Color.web("#899d7b"), Color.web("#718d72"), Color.web("#d5ba76"),
            Color.web("#b2b49a"), Color.web("#e8e6d5"), Color.web("#ad895c"),
            Color.web("#898477") };

    private final GameModelController controller;
    private final VisibleAreaService visibleArea;
    private final ScrollingManager scrolling;
    private final I18n i18n;
    private final Canvas terrain = new Canvas(WIDTH, HEIGHT);
    private final Pane map = new Pane();
    private final Rectangle viewportOutline = new Rectangle();
    private final Rectangle viewport = new Rectangle();
    private final Label hint = new Label();
    private MinimapProjection projection;
    private Location worldSize;
    private byte[] painted;
    private boolean dragging;

    @Inject
    public WorldMinimap(final GameModelController controller,
            final @Named("game") VisibleAreaService visibleArea,
            final ScrollingManager scrolling, final I18n i18n) {
        this.controller = controller;
        this.visibleArea = visibleArea;
        this.scrolling = scrolling;
        this.i18n = i18n;
        setId("minimapPanel");
        setSpacing(3);
        setMinWidth(WIDTH);
        setMaxWidth(WIDTH);
        terrain.setId("minimapTerrain");
        viewport.setId("minimapViewport");
        for (Rectangle rectangle : new Rectangle[] { viewportOutline,
                viewport }) {
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setMouseTransparent(true);
        }
        viewportOutline.setStroke(INK);
        viewportOutline.setStrokeWidth(4);
        viewport.setStroke(PAPER);
        viewport.setStrokeWidth(2);
        map.setId("worldMinimap");
        map.setMinSize(WIDTH, HEIGHT);
        map.setPrefSize(WIDTH, HEIGHT);
        map.setMaxSize(WIDTH, HEIGHT);
        map.setFocusTraversable(true);
        map.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        map.getChildren().addAll(terrain, viewportOutline, viewport);
        map.setOnMousePressed(event -> {
            dragging = event.getButton() == MouseButton.PRIMARY
                    && projection != null && projection.getBounds()
                            .contains(event.getX(), event.getY());
            if (dragging) {
                map.requestFocus();
                scrolling
                        .centerOn(projection.toMap(event.getX(), event.getY()));
                event.consume();
            }
        });
        map.setOnMouseDragged(event -> {
            if (dragging && event.isPrimaryButtonDown()) {
                scrolling
                        .centerOn(projection.toMap(event.getX(), event.getY()));
                event.consume();
            }
        });
        map.setOnMouseReleased(event -> {
            if (dragging && event.getButton() == MouseButton.PRIMARY) {
                dragging = false;
                event.consume();
            }
        });
        map.setOnKeyPressed(this::pan);
        map.setOnKeyReleased(event -> {
            if (event.getCode().isArrowKey()) {
                event.consume();
            }
        });
        map.focusedProperty().addListener(
                (observable, oldValue, newValue) -> painted = null);
        hint.setId("minimapHint");
        hint.setWrapText(true);
        hint.setMaxWidth(WIDTH);
        hint.setMinHeight(Region.USE_PREF_SIZE);
        getChildren().addAll(map, hint);
    }

    private void pan(final KeyEvent event) {
        int dx = 0, dy = 0;
        switch (event.getCode()) {
        case LEFT:
            dx = -Tile.TILE_WIDTH_IN_PX;
            break;
        case RIGHT:
            dx = Tile.TILE_WIDTH_IN_PX;
            break;
        case UP:
            dy = -Tile.TILE_WIDTH_IN_PX;
            break;
        case DOWN:
            dy = Tile.TILE_WIDTH_IN_PX;
            break;
        default:
            return;
        }
        if (projection != null) {
            final Point start = visibleArea.getTopLeft();
            final Point size = visibleArea.getCanvasSize();
            scrolling.centerOn(Point.of(start.getX() + size.getX() / 2 + dx,
                    start.getY() + size.getY() / 2 + dy));
        }
        event.consume();
    }

    /** Called after the detailed map in its existing animation lifecycle. */
    public void paint() {
        if (!controller.isGameModelReady() || !visibleArea.isReady()) {
            return;
        }
        final WorldMap world = controller.getModel().getMap();
        if (!world.getMapSize().equals(worldSize)) {
            worldSize = world.getMapSize();
            projection = new MinimapProjection(worldSize, WIDTH, HEIGHT);
            painted = null;
        }
        final String help = i18n.get(Loc.minimap_hint);
        if (!help.equals(hint.getText())) {
            hint.setText(help);
            map.setAccessibleText(i18n.get(Loc.minimap_title));
            map.setAccessibleHelp(help);
        }
        final Player player = controller.getHumanPlayer();
        final byte[] tiles = new byte[worldSize.getX() * worldSize.getY()];
        for (int y = 0; y < worldSize.getY(); y++) {
            for (int x = 0; x < worldSize.getX(); x++) {
                final Location location = Location.of(x + 1, y + 1);
                // Never inspect terrain behind the unexplored mask.
                if (player.isVisible(location)) {
                    tiles[y * worldSize.getX() + x] = color(
                            world.getTerrainAt(location));
                }
            }
        }
        // Only redraw geography when the permitted terrain changes.
        if (!Arrays.equals(tiles, painted)) {
            drawTerrain(tiles);
            painted = tiles;
        }
        final Rectangle2D view = projection.viewport(visibleArea.getTopLeft(),
                visibleArea.getCanvasSize());
        for (Rectangle rectangle : new Rectangle[] { viewportOutline,
                viewport }) {
            rectangle.setX(view.getMinX());
            rectangle.setY(view.getMinY());
            rectangle.setWidth(view.getWidth());
            rectangle.setHeight(view.getHeight());
        }
    }

    private byte color(final Terrain tile) {
        final TerrainType type = tile.getTerrainType();
        if (type.isSea())
            return 1;
        if (tile.isHasTrees())
            return 3;
        if (type == TerrainType.PRAIRIE)
            return 4;
        if (type == TerrainType.SAVANNAH)
            return 5;
        if (type == TerrainType.PLAINS)
            return 6;
        if (type == TerrainType.MARSH)
            return 7;
        if (type == TerrainType.SWAMP)
            return 8;
        if (type == TerrainType.DESERT)
            return 9;
        if (type == TerrainType.TUNDRA)
            return 10;
        if (type == TerrainType.ARCTIC)
            return 11;
        if (type == TerrainType.HILL)
            return 12;
        if (type == TerrainType.MOUNTAIN)
            return 13;
        return 2;
    }

    private void drawTerrain(final byte[] tiles) {
        final GraphicsContext g = terrain.getGraphicsContext2D();
        final Rectangle2D b = projection.getBounds();
        final double tile = projection.getTileSize();
        g.clearRect(0, 0, WIDTH, HEIGHT);
        g.setFill(COLORS[0]);
        g.fillRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        for (int y = 0; y < worldSize.getY(); y++) {
            for (int x = 0; x < worldSize.getX(); x++) {
                g.setFill(COLORS[tiles[y * worldSize.getX() + x]]);
                // Integer edges keep flat fills free of hairline seams.
                final double left = Math.floor(b.getMinX() + x * tile);
                final double top = Math.floor(b.getMinY() + y * tile);
                g.fillRect(left, top,
                        Math.ceil(b.getMinX() + (x + 1) * tile) - left,
                        Math.ceil(b.getMinY() + (y + 1) * tile) - top);
            }
        }
        g.setStroke(INK);
        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineWidth(Math.max(1.25, Math.min(2, tile * .35)));
        for (int y = 0; y < worldSize.getY(); y++) {
            for (int x = 0; x < worldSize.getX(); x++) {
                final int index = y * worldSize.getX() + x;
                final double px = b.getMinX() + x * tile;
                final double py = b.getMinY() + y * tile;
                if (x > 0 && boundary(tiles[index], tiles[index - 1])) {
                    line(g, px, py, px, py + tile, .35);
                }
                if (y > 0 && boundary(tiles[index],
                        tiles[index - worldSize.getX()])) {
                    line(g, px, py, px + tile, py, -.35);
                }
            }
        }
        g.setLineWidth(2.2);
        line(g, b.getMinX(), b.getMinY(), b.getMaxX(), b.getMinY(), -1);
        line(g, b.getMaxX(), b.getMinY(), b.getMaxX(), b.getMaxY(), 1);
        line(g, b.getMaxX(), b.getMaxY(), b.getMinX(), b.getMaxY(), -.8);
        line(g, b.getMinX(), b.getMaxY(), b.getMinX(), b.getMinY(), 1);
        if (map.isFocused()) {
            g.setStroke(INK);
            g.setLineWidth(1);
            g.setLineDashes(2, 3);
            g.strokeRect(1, 1, WIDTH - 2, HEIGHT - 2);
            g.setLineDashes();
        }
    }

    private boolean boundary(final byte a, final byte b) {
        return (a == 0) != (b == 0) || (a > 0 && b > 0 && (a == 1) != (b == 1));
    }

    private void line(final GraphicsContext g, final double x1, final double y1,
            final double x2, final double y2, final double wobble) {
        g.beginPath();
        g.moveTo(x1, y1);
        g.quadraticCurveTo((x1 + x2) / 2 + (x1 == x2 ? wobble : 0),
                (y1 + y2) / 2 + (y1 == y2 ? wobble : 0), x2, y2);
        g.stroke();
    }
}
