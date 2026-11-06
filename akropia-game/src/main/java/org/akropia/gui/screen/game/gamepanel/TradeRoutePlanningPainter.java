package org.akropia.gui.screen.game.gamepanel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.akropia.gui.Point;
import org.akropia.gui.Tile;
import org.akropia.gui.screen.colony.TradeRouteText;
import org.akropia.i18n.I18n;
import org.akropia.model.Location;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRoutePreview;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Draws the planner's darkened map and every shown route connection as one
 * soft, slightly irregular pen line through the tile centers of the model's
 * path. The bends and wobble come from the connection's transport and squares,
 * so a repaint draws the same line; they never change which squares the path
 * uses.
 */
final class TradeRoutePlanningPainter {

    /** Largest wobble of an inner point from its tile center, in pixels. */
    private static final double WOBBLE = 4.5;

    /** Distance between routes sharing squares, in pixels. */
    private static final double LANE = 3.5;

    private static final Color SHADE = new Color(0.03, 0.05, 0.10, 0.58);

    private static final Color UNDERLINE = new Color(0, 0, 0, 0.7);

    private final TradeRoutePlanning planning;

    private final MouseOverTileManager mouseOverTileManager;

    private final I18n i18n;

    @Inject
    TradeRoutePlanningPainter(final TradeRoutePlanning planning,
            final MouseOverTileManager mouseOverTileManager, final I18n i18n) {
        this.planning = Preconditions.checkNotNull(planning);
        this.mouseOverTileManager = Preconditions
                .checkNotNull(mouseOverTileManager);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    /** Darkens everything painted so far; towns and routes come on top. */
    void darken(final GraphicsContext graphics, final double width,
            final double height) {
        graphics.setFill(SHADE);
        graphics.fillRect(0, 0, width, height);
    }

    /**
     * Draws all routes. The hovered and selected routes are wider, and the
     * selected one is drawn last so it stays on top.
     */
    void paintRoutes(final GraphicsContext graphics, final Area area) {
        final List<TradeRoutePreview> previews = planning.getPreviews();
        final Optional<Integer> selected = planning.getSelectedRouteId();
        final Set<Integer> hovered = mouseOverTileManager.getMouseOverTile()
                .map(planning::getRoutesAt).orElse(List.of()).stream()
                .map(preview -> preview.getRoute().getId())
                .collect(Collectors.toSet());
        final Set<Location> europe = new LinkedHashSet<>();
        graphics.save();
        try {
            graphics.setLineCap(StrokeLineCap.ROUND);
            graphics.setLineJoin(StrokeLineJoin.ROUND);
            for (int lane = 0; lane < previews.size(); lane++) {
                final int id = previews.get(lane).getRoute().getId();
                if (!selected.filter(value -> value == id).isPresent()) {
                    paintRoute(graphics, area, previews.get(lane), lane,
                            hovered.contains(id) ? 4.5 : 3, europe);
                }
            }
            for (int lane = 0; lane < previews.size(); lane++) {
                final int id = previews.get(lane).getRoute().getId();
                if (selected.filter(value -> value == id).isPresent()) {
                    paintRoute(graphics, area, previews.get(lane), lane, 6,
                            europe);
                }
            }
            europe.forEach(at -> paintEurope(graphics, area, at));
        } finally {
            graphics.restore();
        }
    }

    private void paintRoute(final GraphicsContext graphics, final Area area,
            final TradeRoutePreview preview, final int lane, final double width,
            final Set<Location> europe) {
        final Color ink = TradeRouteColors.colorOf(preview);
        // Routes sharing squares run side by side, three lanes apart at most.
        final double offset = (Math.floorMod(lane, 5) - 2) * LANE;
        for (final TradeRoutePreview.Leg leg : preview.getLegs()) {
            if (leg.getTo().isEurope()) {
                leg.getPaths().forEach(path -> europe.add(
                        path.getSquares().get(path.getSquares().size() - 1)));
            }
        }
        final List<TradeRoutePreview.LegPath> connections = preview
                .getConnections();
        final List<double[][]> lines = lines(preview, area, offset);
        for (int index = 0; index < connections.size(); index++) {
            final double[][] line = lines.get(index);
            if (line[0].length < 2) {
                continue;
            }
            // Without a carrier the sea possibility is dashed, so the two grey
            // variants stay distinguishable.
            final boolean dashed = preview.getCarrier().isEmpty()
                    && connections.get(index).getKind() == TradeRouteKind.SEA;
            graphics.setLineDashes(dashed ? new double[] { 11, 7 } : null);
            graphics.setStroke(UNDERLINE);
            graphics.setLineWidth(width + 3);
            stroke(graphics, line);
            graphics.setStroke(ink);
            graphics.setLineWidth(width);
            stroke(graphics, line);
        }
        graphics.setLineDashes(null);
    }

    /**
     * The drawn line of each of the route's connections, in the order of
     * {@link TradeRoutePreview#getConnections()}. A connection that carriers
     * travel both ways is one line. Its bends come from its transport and its
     * squares taken from the end that comes first by column and row, so neither
     * the order of the legs nor their direction changes the line.
     *
     * @param preview required route preview
     * @param area    required visible area
     * @param offset  shift of the route's lane in pixels
     * @return x and y coordinates of each line
     */
    static List<double[][]> lines(final TradeRoutePreview preview,
            final Area area, final double offset) {
        final List<double[][]> lines = new ArrayList<>();
        for (final TradeRoutePreview.LegPath connection : preview
                .getConnections()) {
            final List<Location> squares = canonical(connection.getSquares());
            final int seed = squares.hashCode() * 31
                    + connection.getKind().ordinal();
            lines.add(curve(area, squares, seed, offset));
        }
        return lines;
    }

    /** The squares from the end that is first by column, then row. */
    private static List<Location> canonical(final List<Location> squares) {
        final Location first = squares.get(0);
        final Location last = squares.get(squares.size() - 1);
        final boolean forward = first.getX() < last.getX()
                || first.getX() == last.getX() && first.getY() <= last.getY();
        return forward ? squares : Lists.reverse(squares);
    }

    /**
     * Tile centers of the path, shifted to the route's lane. Inner points
     * wobble by a fixed amount of their own; the ends stay at their towns.
     */
    private static double[][] curve(final Area area,
            final List<Location> squares, final int seed, final double offset) {
        final double[] xs = new double[squares.size()];
        final double[] ys = new double[squares.size()];
        for (int index = 0; index < squares.size(); index++) {
            final Point corner = area.convertToCanvasPoint(squares.get(index));
            final boolean end = index == 0 || index == squares.size() - 1;
            xs[index] = corner.getX() + Tile.TILE_CENTER.getX() + offset
                    + (end ? 0 : wobble(seed, index, 0));
            ys[index] = corner.getY() + Tile.TILE_CENTER.getY() - offset
                    + (end ? 0 : wobble(seed, index, 1));
        }
        return new double[][] { xs, ys };
    }

    /** Fixed pseudo-random wobble in [-WOBBLE, WOBBLE] for one point. */
    private static double wobble(final int seed, final int index,
            final int axis) {
        int hash = seed * 31 + index * 92821 + axis * 68917;
        hash ^= hash >>> 15;
        hash *= 0x2C1B3C6D;
        hash ^= hash >>> 12;
        hash *= 0x297A2D39;
        hash ^= hash >>> 15;
        return ((hash & 0xFFFF) / 65535.0 * 2 - 1) * WOBBLE;
    }

    /**
     * A soft curve: each tile center pulls the line toward it, and the line
     * passes the midpoints between centers, so corners become bends.
     */
    private static void stroke(final GraphicsContext graphics,
            final double[][] line) {
        final double[] xs = line[0];
        final double[] ys = line[1];
        if (xs.length < 2) {
            return;
        }
        graphics.beginPath();
        graphics.moveTo(xs[0], ys[0]);
        for (int index = 1; index < xs.length - 1; index++) {
            graphics.quadraticCurveTo(xs[index], ys[index],
                    (xs[index] + xs[index + 1]) / 2,
                    (ys[index] + ys[index + 1]) / 2);
        }
        graphics.lineTo(xs[xs.length - 1], ys[ys.length - 1]);
        graphics.stroke();
    }

    /** Europe is a stop out on the high sea, where its ships depart. */
    private void paintEurope(final GraphicsContext graphics, final Area area,
            final Location at) {
        final Point corner = area.convertToCanvasPoint(at);
        final double x = corner.getX() + Tile.TILE_CENTER.getX();
        final double y = corner.getY() + Tile.TILE_CENTER.getY();
        graphics.setLineDashes(null);
        graphics.setFill(Color.web("#1B2A44"));
        graphics.setStroke(Color.web("#F5EBD0"));
        graphics.setLineWidth(2.5);
        graphics.fillOval(x - 10, y - 10, 20, 20);
        graphics.strokeOval(x - 10, y - 10, 20, 20);
        graphics.setFill(Color.web("#F5EBD0"));
        graphics.fillOval(x - 3.5, y - 3.5, 7, 7);
        graphics.setFont(Font.font("System", FontWeight.BOLD, 14));
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.BOTTOM);
        final String label = i18n.get(TradeRouteText.europe);
        graphics.setStroke(Color.rgb(0, 0, 0, 0.85));
        graphics.setLineWidth(3);
        graphics.strokeText(label, x, y - 12);
        graphics.fillText(label, x, y - 12);
    }
}
