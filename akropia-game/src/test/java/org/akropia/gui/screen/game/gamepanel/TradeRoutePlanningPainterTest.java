package org.akropia.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;

import org.akropia.gui.Point;
import org.akropia.gui.Tile;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.TradeRoute;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRoutePreview;
import org.akropia.model.TradeRouteStop;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.ModelPo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * The lines {@link TradeRoutePlanningPainter} draws for a route (R-0425), on
 * the F108 scenario with a channel that makes inland New Amsterdam (id 0) at
 * (24,16) a port that ships reach from Delft (id 1) at (28,16).
 */
class TradeRoutePlanningPainterTest {

    private static final File SCENARIO = new File(
            "../akropia-test/src/test/scenarios/F108-trade-route-cargo.akropia");

    private static final int NEW_AMSTERDAM = 0;
    private static final int DELFT = 1;

    private final VisibleAreaService visibleArea = mock(
            VisibleAreaService.class);

    private static Model channelModel() {
        final ModelPo po = new GameModelPoDao().loadFromFile(SCENARIO)
                .getModel();
        final String[] rows = po.getMap().getTiles();
        for (final int x : new int[] { 25, 26, 27 }) {
            final String prefix = rows[17].substring(0, "row-0001:".length());
            final String[] parts = rows[17].substring(prefix.length())
                    .split(",");
            parts[x - 1] = "_";
            rows[17] = prefix + String.join(",", parts);
        }
        return Model.make(po, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static TradeRoutePreview loop(final Model model, final int start,
            final int other, final TradeRouteKind kind) {
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final TradeRoute route = model.createTradeRoute(dutch, "Loop " + start,
                kind, start, List.of(TradeRouteStop.ofColony(other),
                        TradeRouteStop.ofColony(start)));
        return model.getTradeRoutePreview(dutch, route);
    }

    private Area area() {
        when(visibleArea.getTopLeft()).thenReturn(Point.of(0, 0));
        return new Area(visibleArea);
    }

    @ParameterizedTest
    @EnumSource(TradeRouteKind.class)
    void wayThereAndBackIsOneLinePerTransport(final TradeRouteKind kind) {
        final Model model = channelModel();
        final TradeRoutePreview preview = loop(model, NEW_AMSTERDAM, DELFT,
                kind);
        // Two legs with one path of the route kind each.
        assertEquals(2, preview.getLegs().size());
        assertEquals(2, preview.getLegs().stream()
                .mapToInt(leg -> leg.getPaths().size()).sum());

        final List<double[][]> lines = TradeRoutePlanningPainter.lines(preview,
                area(), 0);
        assertEquals(1, lines.size());
        for (int index = 0; index < lines.size(); index++) {
            final List<Location> squares = preview.getConnections().get(index)
                    .getSquares();
            assertEquals(squares.size(), lines.get(index)[0].length);
            // The line ends at the tile centers of its two towns.
            assertEndsAt(lines.get(index), squares.get(0),
                    squares.get(squares.size() - 1));
        }
        // Painting again draws exactly the same lines.
        assertArrayEquals(lines.toArray(),
                TradeRoutePlanningPainter.lines(preview, area(), 0).toArray());
    }

    @ParameterizedTest
    @EnumSource(TradeRouteKind.class)
    void legOrderAndDirectionDoNotChangeTheLine(final TradeRouteKind kind) {
        final Model model = channelModel();
        // One route starts in New Amsterdam, the other in Delft: their legs
        // travel the same connections in the opposite order and direction.
        final TradeRoutePreview fromNewAmsterdam = loop(model, NEW_AMSTERDAM,
                DELFT, kind);
        final TradeRoutePreview fromDelft = loop(model, DELFT, NEW_AMSTERDAM,
                kind);
        assertEquals(fromNewAmsterdam.getLegs().get(0).getFrom(),
                fromDelft.getLegs().get(1).getFrom());

        final List<double[][]> one = TradeRoutePlanningPainter
                .lines(fromNewAmsterdam, area(), 0);
        final List<double[][]> other = TradeRoutePlanningPainter
                .lines(fromDelft, area(), 0);
        assertEquals(1, one.size());
        assertEquals(1, other.size());
        for (int index = 0; index < one.size(); index++) {
            assertArrayEquals(one.get(index)[0], other.get(index)[0]);
            assertArrayEquals(one.get(index)[1], other.get(index)[1]);
        }
    }

    private static void assertEndsAt(final double[][] line,
            final Location... ends) {
        final double[] xs = line[0];
        final double[] ys = line[1];
        final List<Point> expected = List.of(center(ends[0]), center(ends[1]));
        final List<Point> actual = List.of(Point.of((int) xs[0], (int) ys[0]),
                Point.of((int) xs[xs.length - 1], (int) ys[ys.length - 1]));
        assertTrue(
                actual.equals(expected) || actual
                        .equals(List.of(expected.get(1), expected.get(0))),
                () -> actual + " " + expected);
    }

    private static Point center(final Location location) {
        return Tile.ofLocation(location).getTopLeftCorner()
                .add(Tile.TILE_CENTER);
    }
}
