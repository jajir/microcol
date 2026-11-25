package org.akropia.gui.screen.game.gamepanel;

import java.util.Optional;
import java.util.function.Supplier;
import org.akropia.gui.Point;
import org.akropia.gui.image.*;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.util.FontService;
import org.akropia.gui.util.PaintService;
import org.akropia.i18n.I18n;
import org.akropia.model.*;
import javafx.scene.canvas.GraphicsContext;

/**
 * Read-only map composition with private map caches and no input controllers.
 */
public final class DemoMapPainter {
    private final Supplier<Model> model;
    private final MapManager maps;
    private final GamePaintService layers;
    private final PaintService paint;
    private final VisibleAreaService visible = new VisibleAreaService(true);

    public DemoMapPainter(Supplier<Model> model, ImageProvider images,
            FontService fonts, I18n texts, GamePreferences preferences) {
        this.model = model;
        Supplier<Player> player = () -> model.get().getPlayers().stream()
                .filter(Player::isHuman).findFirst().orElseThrow();
        maps = new MapManager(new GrassCoastMapGenerator(images),
                new HiddenCoastMapGenerator(images, player), model, player,
                images);
        paint = new PaintService(images, maps, fonts, texts);
        layers = new GamePaintService(model, player, images, paint,
                preferences);
    }

    public void refresh() {
        maps.refresh();
    }

    public void paint(GraphicsContext g, Location center, long tick,
            Unit moving, Location from, Location to, double progress) {
        Model current = model.get();
        visible.setMapSize(Location.of(current.getMap().getMaxLocationX(),
                current.getMap().getMaxLocationY()));
        visible.setCanvasWidth((int) g.getCanvas().getWidth());
        visible.setCanvasHeight((int) g.getCanvas().getHeight());
        Area area = new Area(visible);
        visible.setTopLeftPosionOfCanvas(
                area.getCanvasTopLeftForLocation(center));
        CanvasInMapCoordinates coordinates = CanvasInMapCoordinates
                .make(visible, Location.of(current.getMap().getMaxLocationX(),
                        current.getMap().getMaxLocationY()));
        layers.paintTerrain(g, area, coordinates, tick);
        layers.paintRivers(g, area, coordinates);
        layers.paintTerrainOverlays(g, area, null, coordinates);
        layers.paintColonies(g, current, area);
        ExcludePainting excluded = new ExcludePainting();
        if (moving != null)
            excluded.excludeUnit(moving);
        layers.paintUnits(g, current, area, excluded, Optional.empty());
        if (moving != null) {
            Point a = area.convertToCanvasPoint(from);
            Point b = area.convertToCanvasPoint(to);
            paint.paintUnit(g, Point.of(
                    (int) (a.getX() + (b.getX() - a.getX()) * progress),
                    (int) (a.getY() + (b.getY() - a.getY()) * progress)),
                    moving, Direction.valueOf(to.sub(from)));
        }
        layers.paintFog(g, area, coordinates);
    }
}
